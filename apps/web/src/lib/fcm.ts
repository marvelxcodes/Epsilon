import admin from "firebase-admin";

// Initialize Firebase Admin SDK
let initError: string | null = null;
let isInitialized = false;

if (!admin.apps.length) {
  // Support both individual env variables and JSON string for backward compatibility
  const projectId = process.env.FIREBASE_PROJECT_ID;
  const privateKey = process.env.FIREBASE_PRIVATE_KEY;
  const clientEmail = process.env.FIREBASE_CLIENT_EMAIL;
  const serviceAccountJson = process.env.FIREBASE_SERVICE_ACCOUNT_KEY;

  try {
    if (projectId && privateKey && clientEmail) {
      // Use individual environment variables
      admin.initializeApp({
        credential: admin.credential.cert({
          projectId,
          privateKey: privateKey.replace(/\\n/g, "\n"), // Handle escaped newlines
          clientEmail,
        }),
      });
      isInitialized = true;
      // biome-ignore lint: initialization logging
      console.log(
        "[FCM] Firebase Admin SDK initialized successfully (individual keys)"
      );
    } else if (serviceAccountJson) {
      // Fallback to JSON string for backward compatibility
      const trimmed = serviceAccountJson.trim();
      if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
        const parsed = JSON.parse(trimmed);
        admin.initializeApp({
          credential: admin.credential.cert(parsed),
        });
        isInitialized = true;
        // biome-ignore lint: initialization logging
        console.log(
          "[FCM] Firebase Admin SDK initialized successfully (JSON key)"
        );
      } else {
        initError = "FIREBASE_SERVICE_ACCOUNT_KEY is not a valid JSON object";
        // biome-ignore lint: initialization warning
        console.warn("[FCM] Warning:", initError);
      }
    } else {
      initError =
        "Firebase credentials not configured. Set FIREBASE_PROJECT_ID, FIREBASE_PRIVATE_KEY, and FIREBASE_CLIENT_EMAIL or FIREBASE_SERVICE_ACCOUNT_KEY";
      // biome-ignore lint: initialization warning
      console.warn(
        "[FCM] Warning:",
        initError,
        "- Push notifications will be disabled"
      );
    }
  } catch (error) {
    initError = `Failed to initialize Firebase Admin SDK: ${error instanceof Error ? error.message : "Unknown error"}`;
    // biome-ignore lint: initialization warning
    console.warn("[FCM] Warning:", initError);
  }
}

export type EmergencyNotificationPayload = {
  userId: string;
  reportId?: string;
  timestamp: string;
};

/**
 * Check if FCM is available
 */
export function isFCMAvailable(): boolean {
  return isInitialized && admin.apps.length > 0;
}

/**
 * Send an emergency call trigger notification to a user's device
 * This notification will trigger the emergency call even when the app is closed
 */
export async function sendEmergencyCallNotification(
  fcmToken: string,
  payload: EmergencyNotificationPayload
): Promise<boolean> {
  try {
    if (!isFCMAvailable()) {
      // biome-ignore lint: warning logging
      console.error(
        "[FCM] FCM not available. Error:",
        initError || "Firebase Admin SDK not initialized"
      );
      throw new Error(
        `FCM not available: ${initError || "Firebase Admin SDK not initialized"}`
      );
    }

    const message = {
      token: fcmToken,
      // Data payload - delivered even when app is in background/closed
      data: {
        type: "EMERGENCY_CALL",
        userId: payload.userId,
        reportId: payload.reportId || "",
        timestamp: payload.timestamp,
      },
      // High priority to ensure delivery even in doze mode
      android: {
        priority: "high" as const,
        // FCM will automatically wake up the device
        data: {
          type: "EMERGENCY_CALL",
          userId: payload.userId,
          reportId: payload.reportId || "",
          timestamp: payload.timestamp,
        },
      },
    };

    await admin.messaging().send(message);
    // biome-ignore lint: success logging
    console.log("[FCM] Emergency notification sent successfully");
    return true;
  } catch (error) {
    // Log error but don't expose details to client
    if (process.env.NODE_ENV === "development") {
      // biome-ignore lint: development logging
      console.error("FCM Error:", error);
    }
    return false;
  }
}

/**
 * Send emergency notifications to multiple devices
 */
export async function sendEmergencyCallNotificationMulticast(
  fcmTokens: string[],
  payload: EmergencyNotificationPayload
): Promise<{ successCount: number; failureCount: number }> {
  try {
    if (!isFCMAvailable()) {
      // biome-ignore lint: warning logging
      console.error(
        "[FCM] FCM not available. Error:",
        initError || "Firebase Admin SDK not initialized"
      );
      throw new Error(
        `FCM not available: ${initError || "Firebase Admin SDK not initialized"}`
      );
    }

    const message = {
      tokens: fcmTokens,
      data: {
        type: "EMERGENCY_CALL",
        userId: payload.userId,
        reportId: payload.reportId || "",
        timestamp: payload.timestamp,
      },
      android: {
        priority: "high" as const,
      },
    };

    const response = await admin.messaging().sendEachForMulticast(message);

    // biome-ignore lint: success logging
    console.log(
      `[FCM] Multicast sent: ${response.successCount} successful, ${response.failureCount} failed`
    );

    return {
      successCount: response.successCount,
      failureCount: response.failureCount,
    };
  } catch (error) {
    if (process.env.NODE_ENV === "development") {
      // biome-ignore lint: development logging
      console.error("FCM Multicast Error:", error);
    }
    return { successCount: 0, failureCount: fcmTokens.length };
  }
}
