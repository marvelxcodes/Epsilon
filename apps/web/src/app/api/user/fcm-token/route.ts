import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { device } from "@epsilon/db/schema/app";
import { eq } from "drizzle-orm";
import { headers } from "next/headers";

/**
 * Update user's FCM token
 * Called by Android app when FCM token is generated/refreshed
 *
 * POST /api/user/fcm-token
 * Body: { fcmToken: string, deviceType: "android" }
 */
export async function POST(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return Response.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const { fcmToken, deviceName, deviceModel, osVersion, appVersion } = body;

    // biome-ignore lint: development logging
    console.log(
      "[FCM] Received token update request for user:",
      session.user.id
    );
    // biome-ignore lint: development logging
    console.log("[FCM] Token:", fcmToken?.substring(0, 20) + "...");

    if (!fcmToken || typeof fcmToken !== "string") {
      return Response.json({ error: "Invalid FCM token" }, { status: 400 });
    }

    const now = new Date();

    // Check if device already exists for this user
    const existingDevices = await db
      .select()
      .from(device)
      .where(eq(device.userId, session.user.id));

    // biome-ignore lint: development logging
    console.log("[FCM] Found existing devices:", existingDevices.length);

    if (existingDevices.length > 0) {
      // Update existing device
      // biome-ignore lint: development logging
      console.log("[FCM] Updating existing device:", existingDevices[0].id);

      await db
        .update(device)
        .set({
          deviceToken: fcmToken,
          deviceName: deviceName || existingDevices[0].deviceName,
          deviceModel: deviceModel || existingDevices[0].deviceModel,
          osVersion: osVersion || existingDevices[0].osVersion,
          appVersion: appVersion || existingDevices[0].appVersion,
          lastActiveAt: now,
          updatedAt: now,
        })
        .where(eq(device.id, existingDevices[0].id));

      // biome-ignore lint: development logging
      console.log("[FCM] ✅ Device updated successfully");
    } else {
      // Insert new device
      const deviceId = `device_${session.user.id}_${Date.now()}`;
      // biome-ignore lint: development logging
      console.log("[FCM] Creating new device:", deviceId);

      await db.insert(device).values({
        id: deviceId,
        userId: session.user.id,
        deviceToken: fcmToken,
        deviceName: deviceName || "Android Device",
        deviceModel: deviceModel || null,
        osVersion: osVersion || null,
        appVersion: appVersion || null,
        lastActiveAt: now,
        createdAt: now,
        updatedAt: now,
      });

      // biome-ignore lint: development logging
      console.log("[FCM] ✅ Device created successfully");
    }

    return Response.json({
      success: true,
      message: "FCM token updated successfully",
    });
  } catch (error) {
    // biome-ignore lint: development logging
    console.error("[FCM] ❌ Error updating FCM token:", error);
    return Response.json(
      { error: "Failed to update FCM token" },
      { status: 500 }
    );
  }
}

/**
 * Get user's FCM tokens
 * Used internally by /api/report to lookup user's device tokens
 *
 * GET /api/user/fcm-token
 */
export async function GET() {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return Response.json({ error: "Unauthorized" }, { status: 401 });
    }

    const devices = await db
      .select({
        id: device.id,
        deviceToken: device.deviceToken,
        deviceName: device.deviceName,
        deviceModel: device.deviceModel,
        lastActiveAt: device.lastActiveAt,
      })
      .from(device)
      .where(eq(device.userId, session.user.id));

    return Response.json({
      success: true,
      devices,
    });
  } catch {
    return Response.json(
      { error: "Failed to fetch FCM tokens" },
      { status: 500 }
    );
  }
}
