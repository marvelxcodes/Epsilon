import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { user } from "@epsilon/db/schema/auth";
import { eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

export async function PUT(req: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session) {
      return NextResponse.json(
        {
          success: false,
          message: "Unauthorized",
        },
        { status: 401 }
      );
    }

    const body = await req.json();
    const { emergencyContactName, emergencyContactPhone } = body;

    if (!emergencyContactPhone || typeof emergencyContactPhone !== "string") {
      return NextResponse.json(
        {
          success: false,
          message: "Emergency contact phone number is required",
        },
        { status: 400 }
      );
    }

    // Update user's emergency contact
    await db
      .update(user)
      .set({
        emergencyContactName: emergencyContactName || null,
        emergencyContactPhone,
        updatedAt: new Date(),
      })
      .where(eq(user.id, session.user.id));

    return NextResponse.json(
      {
        success: true,
        message: "Emergency contact updated successfully",
      },
      { status: 200 }
    );
  } catch {
    return NextResponse.json(
      {
        success: false,
        message: "Internal server error",
      },
      { status: 500 }
    );
  }
}

export async function GET() {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session) {
      return NextResponse.json(
        {
          success: false,
          message: "Unauthorized",
        },
        { status: 401 }
      );
    }

    // Get user's emergency contact
    const [userData] = await db
      .select({
        emergencyContactName: user.emergencyContactName,
        emergencyContactPhone: user.emergencyContactPhone,
      })
      .from(user)
      .where(eq(user.id, session.user.id));

    return NextResponse.json(
      {
        success: true,
        data: {
          emergencyContactName: userData?.emergencyContactName || null,
          emergencyContactPhone: userData?.emergencyContactPhone || null,
        },
      },
      { status: 200 }
    );
  } catch {
    return NextResponse.json(
      {
        success: false,
        message: "Internal server error",
      },
      { status: 500 }
    );
  }
}
