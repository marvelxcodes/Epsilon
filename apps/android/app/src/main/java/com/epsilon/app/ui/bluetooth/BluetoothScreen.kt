package com.epsilon.app.ui.bluetooth

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.utils.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int
)

class BluetoothViewModel(private val context: Context) : ViewModel() {
    var devices by mutableStateOf<List<BleDevice>>(emptyList())
        private set

    var isScanning by mutableStateOf(false)
        private set

    var connectedDevice by mutableStateOf<String?>(null)
        private set

    var statusMessage by mutableStateOf("")
        private set

    // Known UUIDs from the firmware (wearable.ino)
    private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")

    private var bluetoothGatt: BluetoothGatt? = null
    private var configCharacteristic: BluetoothGattCharacteristic? = null
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    private val bluetoothLeScanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private var authToken: String = ""

    fun setAuthToken(token: String) {
        authToken = token
    }

    fun updateStatusMessage(message: String) {
        statusMessage = message
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val deviceName = if (hasBluetoothPermission()) {
                device.name ?: "Unknown"
            } else {
                "Unknown"
            }

            if (device.address.contains("B8", ignoreCase = true) || deviceName.contains("ESP32", ignoreCase = true)) {
                val bleDevice = BleDevice(
                    name = deviceName,
                    address = device.address,
                    rssi = result.rssi
                )

                val existingDevices = devices.toMutableList()
                if (!existingDevices.any { it.address == bleDevice.address }) {
                    existingDevices.add(bleDevice)
                    devices = existingDevices
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            statusMessage = "Scan failed with error: $errorCode"
            isScanning = false
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectedDevice = gatt.device.address
                    statusMessage = "Connected to device"
                    if (hasBluetoothPermission()) {
                        gatt.discoverServices()
                    }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectedDevice = null
                    statusMessage = "Disconnected from device"
                    configCharacteristic = null
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)
                    if (characteristic != null) {
                        configCharacteristic = characteristic
                        statusMessage = "Configuration characteristic found"

                        // Enable notifications if supported
                        if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                            gatt.setCharacteristicNotification(characteristic, true)
                            val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                            descriptor?.let {
                                it.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                                gatt.writeDescriptor(it)
                            }
                        }
                    } else {
                        statusMessage = "Characteristic not found on device"
                    }
                } else {
                    statusMessage = "Service not found on device"
                }
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            val data = String(value, Charsets.UTF_8)
            statusMessage = "Received: $data"
        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                statusMessage = "Configuration sent successfully"
            } else {
                statusMessage = "Failed to send configuration (status=$status)"
            }
        }
    }

    private fun hasBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_SCAN
                ) == PackageManager.PERMISSION_GRANTED
        } else {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH
            ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_ADMIN
                ) == PackageManager.PERMISSION_GRANTED
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (!hasBluetoothPermission()) {
            statusMessage = "Bluetooth permission not granted"
            return
        }

        if (bluetoothAdapter?.isEnabled != true) {
            statusMessage = "Bluetooth is not enabled"
            return
        }

        devices = emptyList()
        isScanning = true
        statusMessage = "Scanning for devices..."

        bluetoothLeScanner?.startScan(scanCallback)

        viewModelScope.launch {
            delay(10000)
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (hasBluetoothPermission()) {
            bluetoothLeScanner?.stopScan(scanCallback)
        }
        isScanning = false
        if (devices.isEmpty()) {
            statusMessage = "No devices found"
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(address: String) {
        if (!hasBluetoothPermission()) {
            statusMessage = "Bluetooth permission not granted"
            return
        }

        val device = bluetoothAdapter?.getRemoteDevice(address)
        statusMessage = "Connecting..."
        bluetoothGatt = device?.connectGatt(context, false, gattCallback)
    }

    @SuppressLint("MissingPermission")
    fun disconnectDevice() {
        if (hasBluetoothPermission()) {
            bluetoothGatt?.disconnect()
            bluetoothGatt?.close()
            bluetoothGatt = null
            configCharacteristic = null
        }
    }

    /**
     * Send auth token + wifi credentials to the device over the configuration characteristic.
     * Payload is a simple JSON string: {"token":"...","ssid":"...","password":"..."}
     */
    @SuppressLint("MissingPermission")
    fun sendConfiguration(wifiSsid: String, wifiPassword: String) {
        val char = configCharacteristic
        val gatt = bluetoothGatt
        if (char == null || gatt == null) {
            statusMessage = "Configuration characteristic not available"
            return
        }

        val payload = "{\"token\":\"$authToken\",\"ssid\":\"${escapeJson(wifiSsid)}\",\"password\":\"${escapeJson(wifiPassword)}\"}"
        char.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
        char.value = payload.toByteArray(Charsets.UTF_8)
        val ok = gatt.writeCharacteristic(char)
        if (!ok) {
            statusMessage = "Failed to initiate write"
        } else {
            statusMessage = "Sending configuration..."
        }
    }

    private fun escapeJson(s: String): String = s.replace("\\", "\\\\").replace("\"", "\\\"")

    override fun onCleared() {
        super.onCleared()
        stopScan()
        disconnectDevice()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothScreen(
    sessionManager: SessionManager,
    onConfigured: () -> Unit,
    viewModel: BluetoothViewModel? = null
) {
    val context = LocalContext.current
    val actualViewModel = viewModel ?: remember { BluetoothViewModel(context) }
    var hasPermissions by remember { mutableStateOf(PermissionUtils.hasBluetoothPermissions(context)) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            actualViewModel.updateStatusMessage("Permissions granted! You can now scan for devices.")
        } else {
            actualViewModel.updateStatusMessage("Bluetooth permissions are required to scan for devices.")
        }
    }

    // load auth token from session and provide to viewModel
    LaunchedEffect(Unit) {
        try {
            val token = sessionManager.token.first() ?: ""
            actualViewModel.setAuthToken(token)
        } catch (e: Exception) {
            android.util.Log.e("BluetoothScreen", "Error loading auth token", e)
        }
    }
    
    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status message
            if (actualViewModel.statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = actualViewModel.statusMessage,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Connected device info + provisioning UI
            var wifiSsid by remember { mutableStateOf("") }
            var wifiPassword by remember { mutableStateOf("") }
            var passwordVisible by remember { mutableStateOf(false) }

            actualViewModel.connectedDevice?.let { address ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Connected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = address,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // WiFi credential inputs for provisioning
                        OutlinedTextField(
                            value = wifiSsid,
                            onValueChange = { wifiSsid = it },
                            label = { Text("WiFi SSID") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Next
                            )
                        )

                        OutlinedTextField(
                            value = wifiPassword,
                            onValueChange = { wifiPassword = it },
                            label = { Text("WiFi Password") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                imeAction = ImeAction.Done,
                                keyboardType = KeyboardType.Password
                            ),
                            keyboardActions = KeyboardActions(onDone = {
                                // nothing here; user can press Send
                            })
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { actualViewModel.sendConfiguration(wifiSsid, wifiPassword) },
                                modifier = Modifier.weight(1f),
                                enabled = wifiSsid.isNotBlank() && wifiPassword.isNotBlank()
                            ) {
                                Text("Send Configuration")
                            }

                            Button(
                                onClick = { actualViewModel.disconnectDevice() },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Disconnect")
                            }
                        }
                    }
                }
            }

            // If configuration was successful, call back to the setup flow
            LaunchedEffect(actualViewModel.statusMessage) {
                if (actualViewModel.statusMessage.contains("Configuration sent successfully")) {
                    onConfigured()
                }
            }
            
            // Scan button
            Button(
                onClick = {
                    if (!hasPermissions) {
                        permissionLauncher.launch(PermissionUtils.getRequiredBluetoothPermissions())
                    } else if (actualViewModel.isScanning) {
                        actualViewModel.stopScan()
                    } else {
                        actualViewModel.startScan()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = actualViewModel.connectedDevice == null
            ) {
                Text(
                    if (!hasPermissions) "Grant Bluetooth Permissions"
                    else if (actualViewModel.isScanning) "Stop Scanning" 
                    else "Start Scanning"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Loading indicator
            if (actualViewModel.isScanning) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Device list
            if (actualViewModel.devices.isNotEmpty()) {
                Text(
                    text = "Found Devices (${actualViewModel.devices.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(actualViewModel.devices) { device ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { 
                                if (actualViewModel.connectedDevice == null) {
                                    actualViewModel.connectToDevice(device.address)
                                }
                            }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = device.name,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = device.address,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Signal: ${device.rssi} dBm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
