import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { device } from "@epsilon/db/schema/app";
import { eq } from "drizzle-orm";
import { headers } from "next/headers";
import { sendEmergencyCallNotification } from "@/lib/fcm";

export async function POST(_request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return Response.json({ error: "Unauthorized" }, { status: 401 });
    }

    // Fetch user's device(s) from database to get FCM token(s)
    const userDevices = await db
      .select({
        deviceToken: device.deviceToken,
        deviceName: device.deviceName,
      })
      .from(device)
      .where(eq(device.userId, session.user.id));

    if (userDevices.length === 0) {
      return Response.json(
        { error: "No devices registered for this user" },
        { status: 404 }
      );
    }

    // Send emergency call notification to all user's devices
    const results = await Promise.all(
      userDevices.map((dev) =>
        sendEmergencyCallNotification(dev.deviceToken, {
          userId: session.user.id,
          timestamp: new Date().toISOString(),
        })
      )
    );

    const successCount = results.filter((r) => r).length;

    if (successCount === 0) {
      return Response.json(
        { error: "Failed to send emergency notification to any device" },
        { status: 500 }
      );
    }

    return Response.json({
      success: true,
      message: "Emergency call triggered successfully",
      devicesNotified: successCount,
      totalDevices: userDevices.length,
    });
  } catch {
    return Response.json(
      { error: "Failed to create emergency action" },
      { status: 500 }
    );
  }
}
