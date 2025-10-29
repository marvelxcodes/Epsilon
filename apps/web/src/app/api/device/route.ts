import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { device } from "@epsilon/db/schema/app";
import { and, eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

export async function POST(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const { deviceName, deviceToken, deviceModel, osVersion, appVersion } =
      body;

    if (!(deviceName && deviceToken)) {
      return NextResponse.json(
        { error: "Device name and token are required" },
        { status: 400 }
      );
    }

    // Check if device already exists for this user
    const existingDevice = await db
      .select()
      .from(device)
      .where(
        and(
          eq(device.userId, session.user.id),
          eq(device.deviceToken, deviceToken)
        )
      )
      .limit(1);

    if (existingDevice.length > 0) {
      // Update existing device
      const updated = await db
        .update(device)
        .set({
          deviceName,
          deviceModel,
          osVersion,
          appVersion,
          lastActiveAt: new Date(),
          updatedAt: new Date(),
        })
        .where(eq(device.id, existingDevice[0].id))
        .returning();

      return NextResponse.json({
        device: updated[0],
        message: "Device updated successfully",
      });
    }

    // Create new device
    const newDevice = await db
      .insert(device)
      .values({
        id: crypto.randomUUID(),
        userId: session.user.id,
        deviceName,
        deviceToken,
        deviceModel,
        osVersion,
        appVersion,
        lastActiveAt: new Date(),
        createdAt: new Date(),
        updatedAt: new Date(),
      })
      .returning();

    return NextResponse.json({
      device: newDevice[0],
      message: "Device registered successfully",
    });
  } catch (error) {
    console.error("Device registration error:", error);
    return NextResponse.json(
      { error: "Failed to register device" },
      { status: 500 }
    );
  }
}

export async function GET(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const deviceToken = searchParams.get("deviceToken");

    if (!deviceToken) {
      // Get all devices for user
      const devices = await db
        .select()
        .from(device)
        .where(eq(device.userId, session.user.id));

      return NextResponse.json({ devices });
    }

    // Check if specific device exists
    const existingDevice = await db
      .select()
      .from(device)
      .where(
        and(
          eq(device.userId, session.user.id),
          eq(device.deviceToken, deviceToken)
        )
      )
      .limit(1);

    return NextResponse.json({
      exists: existingDevice.length > 0,
      device: existingDevice[0] || null,
    });
  } catch (error) {
    console.error("Device fetch error:", error);
    return NextResponse.json(
      { error: "Failed to fetch device" },
      { status: 500 }
    );
  }
}

export async function PATCH(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const { deviceId, deviceName, deviceModel, osVersion, appVersion } = body;

    if (!deviceId) {
      return NextResponse.json(
        { error: "Device ID is required" },
        { status: 400 }
      );
    }

    const updated = await db
      .update(device)
      .set({
        ...(deviceName && { deviceName }),
        ...(deviceModel && { deviceModel }),
        ...(osVersion && { osVersion }),
        ...(appVersion && { appVersion }),
        lastActiveAt: new Date(),
        updatedAt: new Date(),
      })
      .where(and(eq(device.id, deviceId), eq(device.userId, session.user.id)))
      .returning();

    if (updated.length === 0) {
      return NextResponse.json({ error: "Device not found" }, { status: 404 });
    }

    return NextResponse.json({
      device: updated[0],
      message: "Device updated successfully",
    });
  } catch (error) {
    console.error("Device update error:", error);
    return NextResponse.json(
      { error: "Failed to update device" },
      { status: 500 }
    );
  }
}
