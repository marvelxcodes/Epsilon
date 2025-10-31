#include <Adafruit_MPU6050.h>
#include <Adafruit_Sensor.h>
#include <Wire.h>
// Replace classic BLE with NimBLE:
#include <NimBLEDevice.h>
#include <WiFi.h>
#include <HTTPClient.h>
#include <Preferences.h>
Adafruit_MPU6050 mpu;
Preferences preferences;

// BLE UUIDs - Use these in your Kotlin app
#define SERVICE_UUID "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define JWT_CHAR_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define WIFI_SSID_UUID "beb5483e-36e1-4688-b7f5-ea07361b26a9"
#define WIFI_PASS_UUID "beb5483e-36e1-4688-b7f5-ea07361b26aa"
#define STATUS_CHAR_UUID "beb5483e-36e1-4688-b7f5-ea07361b26ab"

// BLE objects (switch to NimBLE types)
NimBLEServer *pServer = NULL;
NimBLECharacteristic *pJWTCharacteristic = NULL;
NimBLECharacteristic *pStatusCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// Configuration
String jwtToken = "WmlIa94YPZ7SP0TJ9hLCTisoLJFliExA";
String wifiSSID = "Pushkar";
String wifiPassword = "iamtheone";
String serverURL = "https://fallbag-backend.vercel.app/"; // backend url
bool isConfigured = false;

// Toggle BLE behaviour: set to false to skip BLE initialization/pairing while it's not ready
const bool ENABLE_BLE = false;

// Fall detection parameters
const float LOWER_FALL_THRESHOLD = 1.5;
const float UPPER_FALL_THRESHOLD = 2.4;
const float GYRO_THRESHOLD = 4.0;
const unsigned long FALL_WINDOW = 500;

// System variables
bool fallDetected = false;
bool inFallWindow = false;
unsigned long fallWindowStart = 0;
unsigned long alertStart = 0;
const unsigned long ALERT_DURATION = 10000;

// Pin definitions
const int BUZZER_PIN = D2;
const int LED_PIN = 21;
const int CANCEL_BUTTON_PIN = D6;

// Forward declarations
void updateStatusCharacteristic(String status);
void checkConfiguration();
void loadConfiguration();
void connectWiFi();

// BLE Callbacks (switch to NimBLE callbacks)
class MyServerCallbacks : public NimBLEServerCallbacks
{
  void onConnect(NimBLEServer *pServer)
  {
    deviceConnected = true;
    Serial.println("BLE Client Connected");
    updateStatusCharacteristic("Connected");
  }
  void onDisconnect(NimBLEServer *pServer)
  {
    deviceConnected = false;
    Serial.println("BLE Client Disconnected");
  }
};

class JWTCallbacks : public NimBLECharacteristicCallbacks
{
  void onWrite(NimBLECharacteristic *pCharacteristic)
  {
    String value = pCharacteristic->getValue().c_str();
    if (value.length() > 0)
    {
      jwtToken = value;
      preferences.putString("jwt", jwtToken);
      Serial.println("JWT Token received and saved");
      updateStatusCharacteristic("JWT saved");
      checkConfiguration();
    }
  }
};

class WiFiSSIDCallbacks : public NimBLECharacteristicCallbacks
{
  void onWrite(NimBLECharacteristic *pCharacteristic)
  {
    String value = pCharacteristic->getValue().c_str();
    if (value.length() > 0)
    {
      wifiSSID = value;
      preferences.putString("ssid", wifiSSID);
      Serial.println("WiFi SSID received and saved");
      updateStatusCharacteristic("SSID saved");
      checkConfiguration();
    }
  }
};

class WiFiPassCallbacks : public NimBLECharacteristicCallbacks
{
  void onWrite(NimBLECharacteristic *pCharacteristic)
  {
    String value = pCharacteristic->getValue().c_str();
    if (value.length() > 0)
    {
      wifiPassword = value;
      preferences.putString("password", wifiPassword);
      Serial.println("WiFi Password received and saved");
      updateStatusCharacteristic("Password saved");
      checkConfiguration();
    }
  }
};

void updateStatusCharacteristic(String status)
{
  if (pStatusCharacteristic != NULL)
  {
    pStatusCharacteristic->setValue(status.c_str());
    pStatusCharacteristic->notify();
  }
}

void checkConfiguration()
{
  isConfigured = (jwtToken.length() > 0 &&
                  wifiSSID.length() > 0 &&
                  wifiPassword.length() > 0);

  if (isConfigured)
  {
    Serial.println("Device configured!");
    updateStatusCharacteristic("Configured");
  }
  else
  {
    Serial.println("Waiting for configuration...");
  }
}

void loadConfiguration()
{
  // Only overwrite hardcoded defaults if a non-empty value exists in Preferences
  String stored = preferences.getString("jwt", "");
  if (stored.length() > 0)
    jwtToken = stored;

  stored = preferences.getString("ssid", "");
  if (stored.length() > 0)
    wifiSSID = stored;

  stored = preferences.getString("password", "");
  if (stored.length() > 0)
    wifiPassword = stored;

  stored = preferences.getString("server", "");
  if (stored.length() > 0)
    serverURL = stored;

  checkConfiguration();
}

void connectWiFi()
{
  Serial.print("Connecting to WiFi: ");
  Serial.println(wifiSSID);

  WiFi.begin(wifiSSID.c_str(), wifiPassword.c_str());

  int attempts = 0;
  while (WiFi.status() != WL_CONNECTED && attempts < 20)
  {
    delay(500);
    Serial.print(".");
    attempts++;
  }

  if (WiFi.status() == WL_CONNECTED)
  {
    Serial.println("\nWiFi Connected!");
    Serial.print("IP: ");
    Serial.println(WiFi.localIP());
    updateStatusCharacteristic("WiFi Connected");
  }
  else
  {
    Serial.println("\nWiFi Connection Failed");
    updateStatusCharacteristic("WiFi Failed");
  }
}

void initBLE()
{
  Serial.println("Initializing BLE...");

  NimBLEDevice::init("FallWatch");

  // Max TX power via NimBLE
  NimBLEDevice::setPower(9); // 0..9, where 9 is max

  pServer = NimBLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  NimBLEService *pService = pServer->createService(SERVICE_UUID);

  // JWT Characteristic
  pJWTCharacteristic = pService->createCharacteristic(
      JWT_CHAR_UUID,
      NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::WRITE);
  pJWTCharacteristic->setCallbacks(new JWTCallbacks());

  // WiFi SSID Characteristic
  NimBLECharacteristic *pSSIDCharacteristic = pService->createCharacteristic(
      WIFI_SSID_UUID,
      NIMBLE_PROPERTY::WRITE);
  pSSIDCharacteristic->setCallbacks(new WiFiSSIDCallbacks());

  // WiFi Password Characteristic
  NimBLECharacteristic *pPasswordCharacteristic = pService->createCharacteristic(
      WIFI_PASS_UUID,
      NIMBLE_PROPERTY::WRITE);
  pPasswordCharacteristic->setCallbacks(new WiFiPassCallbacks());

  // Status Characteristic
  pStatusCharacteristic = pService->createCharacteristic(
      STATUS_CHAR_UUID,
      NIMBLE_PROPERTY::READ | NIMBLE_PROPERTY::NOTIFY);
  // NimBLE auto-adds 2902 descriptor for NOTIFY, no need to add manually

  pService->start();

  // Compact, compatible advertising
  NimBLEAdvertising *pAdvertising = NimBLEDevice::getAdvertising();
  NimBLEAdvertisementData advData;
  advData.setName("FallWatch");
  advData.setCompleteServices(NimBLEUUID(SERVICE_UUID));
  pAdvertising->setAdvertisementData(advData);
  pAdvertising->setMinInterval(0x20); // ~20ms
  pAdvertising->setMaxInterval(0x40); // ~40ms

  NimBLEDevice::startAdvertising();

  Serial.println("BLE Service Started - Advertising FallWatch...");
  Serial.print("Device Address: ");
  Serial.println(NimBLEDevice::getAddress().toString().c_str());
}

void handleBLE()
{
  // Handle disconnection
  if (!deviceConnected && oldDeviceConnected)
  {
    delay(500);
    pServer->startAdvertising();
    Serial.println("Start advertising again");
    oldDeviceConnected = deviceConnected;
  }

  // Handle new connection
  if (deviceConnected && !oldDeviceConnected)
  {
    oldDeviceConnected = deviceConnected;
  }
}

void sendHeartbeat()
{
  if (WiFi.status() != WL_CONNECTED)
  {
    connectWiFi();
    return;
  }

  HTTPClient http;
  http.begin(serverURL + "api/report");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Authorization", "Bearer " + jwtToken);

  String payload = "{\"status\":\"active\",\"timestamp\":" + String(millis()) + "}";

  int httpResponseCode = http.POST(payload);

  if (httpResponseCode > 0)
  {
    Serial.print("Heartbeat sent: ");
    Serial.println(httpResponseCode);
  }
  else
  {
    Serial.print("Heartbeat failed: ");
    Serial.println(httpResponseCode);
  }

  http.end();
}

void sendFallAlert()
{
  if (WiFi.status() != WL_CONNECTED)
  {
    Serial.println("WiFi not connected - cannot send alert");
    return;
  }

  HTTPClient http;
  http.begin(serverURL + "api/report");
  http.addHeader("Content-Type", "application/json");
  http.addHeader("Authorization", "Bearer " + jwtToken);

  String payload = "{\"event\":\"fall_detected\",\"timestamp\":" + String(millis()) + ",\"severity\":\"high\"}";

  int httpResponseCode = http.POST(payload);

  if (httpResponseCode > 0)
  {
    Serial.print("Fall alert sent: ");
    Serial.println(httpResponseCode);
    String response = http.getString();
    Serial.println(response);
  }
  else
  {
    Serial.print("Fall alert failed: ");
    Serial.println(httpResponseCode);
  }

  http.end();
}

void triggerFallAlert()
{
  Serial.println("========================================");
  Serial.println("           FALL ALERT TRIGGERED        ");
  Serial.println("Press button to cancel within 10 seconds");
  Serial.println("========================================");

  digitalWrite(LED_PIN, HIGH);
  tone(BUZZER_PIN, 1000);

  updateStatusCharacteristic("Fall Detected!");
}

void handleFallAlert()
{
  unsigned long alertTime = millis() - alertStart;

  if ((alertTime / 250) % 2 == 0)
  {
    digitalWrite(LED_PIN, HIGH);
  }
  else
  {
    digitalWrite(LED_PIN, LOW);
  }

  if (alertTime > ALERT_DURATION)
  {
    Serial.println("EMERGENCY CONFIRMED - SENDING SOS");
    sendFallAlert();
    sendEmergencyAlert();

    fallDetected = false;
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
  }
}

void cancelAlert()
{
  if (fallDetected)
  {
    Serial.println("Fall alert cancelled by user");
    fallDetected = false;
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
    updateStatusCharacteristic("Alert Cancelled");
    delay(500);
  }
}

void sendEmergencyAlert()
{
  Serial.println("========================================");
  Serial.println("      EMERGENCY ALERT ACTIVATED        ");
  Serial.println("========================================");

  for (int i = 0; i < 30; i++)
  {
    digitalWrite(LED_PIN, HIGH);
    tone(BUZZER_PIN, 2000);
    delay(500);
    digitalWrite(LED_PIN, LOW);
    noTone(BUZZER_PIN);
    delay(500);

    if (digitalRead(CANCEL_BUTTON_PIN) == LOW)
    {
      Serial.println("Emergency alert cancelled");
      return;
    }
  }
}

void setup()
{
  Serial.begin(115200);

  // Initialize pins
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  pinMode(CANCEL_BUTTON_PIN, INPUT_PULLUP);

  digitalWrite(BUZZER_PIN, LOW);
  digitalWrite(LED_PIN, LOW);

  Serial.println("ESP32 Fall Detection Starting...");

  // Initialize preferences
  preferences.begin("fallwatch", false);
  loadConfiguration();

  // Initialize BLE only if enabled; skip pairing/advertising if not ready
  if (ENABLE_BLE)
  {
    initBLE();
  }
  else
  {
    Serial.println("BLE initialization skipped (ENABLE_BLE=false)");
  }

  // Initialize MPU6050
  if (!mpu.begin())
  {
    Serial.println("Failed to find MPU6050 chip");
    updateStatusCharacteristic("MPU6050 Error");
    while (1)
    {
      digitalWrite(LED_PIN, HIGH);
      delay(500);
      digitalWrite(LED_PIN, LOW);
      delay(500);
    }
  }

  Serial.println("MPU6050 Found!");

  // Configure sensor ranges
  mpu.setAccelerometerRange(MPU6050_RANGE_8_G);
  mpu.setGyroRange(MPU6050_RANGE_500_DEG);
  mpu.setFilterBandwidth(MPU6050_BAND_21_HZ);

  // Connect to WiFi if configured
  if (isConfigured)
  {
    connectWiFi();
  }

  Serial.println("Fall Detection System Ready");
  updateStatusCharacteristic("Ready");
  delay(2000);
}

void loop()
{
  // Handle BLE connections
  if (ENABLE_BLE)
  {
    handleBLE();
  }

  // Check for button press to cancel alert
  if (digitalRead(CANCEL_BUTTON_PIN) == LOW)
  {
    cancelAlert();
  }

  // Handle active alert
  if (fallDetected)
  {
    handleFallAlert();
    return;
  }

  // Get sensor readings
  sensors_event_t accel, gyro, temp;
  mpu.getEvent(&accel, &gyro, &temp);

  // Calculate acceleration magnitude
  float accMagnitude = sqrt(pow(accel.acceleration.x, 2) +
                            pow(accel.acceleration.y, 2) +
                            pow(accel.acceleration.z, 2));

  // Calculate angular velocity magnitude
  float gyroMagnitude = sqrt(pow(gyro.gyro.x, 2) +
                             pow(gyro.gyro.y, 2) +
                             pow(gyro.gyro.z, 2));

  // Fall detection algorithm
  if (!inFallWindow && accMagnitude < LOWER_FALL_THRESHOLD)
  {
    inFallWindow = true;
    fallWindowStart = millis();
    Serial.println("Free fall detected - Opening fall window");
  }

  if (inFallWindow)
  {
    if (millis() - fallWindowStart > FALL_WINDOW)
    {
      inFallWindow = false;
      Serial.println("Fall window expired - No fall detected");
    }
    else
    {
      if (accMagnitude > UPPER_FALL_THRESHOLD && gyroMagnitude > GYRO_THRESHOLD)
      {
        fallDetected = true;
        alertStart = millis();
        inFallWindow = false;
        triggerFallAlert();
        Serial.println("FALL DETECTED!");
      }
    }
  }

  // Send periodic heartbeat to server (every 30 seconds)
  static unsigned long lastHeartbeat = 0;
  if (isConfigured && WiFi.status() == WL_CONNECTED &&
      millis() - lastHeartbeat > 30000)
  {
    sendHeartbeat();
    lastHeartbeat = millis();
  }

  // Debug output every 2 seconds
  static unsigned long lastPrint = 0;
  if (millis() - lastPrint > 2000)
  {
    Serial.print("Acc: ");
    Serial.print(accMagnitude, 2);
    Serial.print(" g, Gyro: ");
    Serial.print(gyroMagnitude, 2);
    Serial.print(" rad/s");
    if (inFallWindow)
    {
      Serial.print(" [FALL WINDOW ACTIVE]");
    }
    Serial.println();
    lastPrint = millis();
  }

  delay(50);
}