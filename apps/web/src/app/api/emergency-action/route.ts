import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { emergencyAction } from "@epsilon/db/schema/app";
import { asc, eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

// GET all emergency actions for user
export async function GET() {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const actions = await db
      .select()
      .from(emergencyAction)
      .where(eq(emergencyAction.userId, session.user.id))
      .orderBy(asc(emergencyAction.priority));

    return NextResponse.json({ actions });
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch emergency actions" },
      { status: 500 }
    );
  }
}

// POST create new emergency action
export async function POST(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const { actionType, actionData, priority, isEnabled } = body;

    if (!(actionType && actionData)) {
      return NextResponse.json(
        { error: "Action type and action data are required" },
        { status: 400 }
      );
    }

    if (!["call", "message", "alarm"].includes(actionType)) {
      return NextResponse.json(
        { error: "Invalid action type. Must be call, message, or alarm" },
        { status: 400 }
      );
    }

    const newAction = await db
      .insert(emergencyAction)
      .values({
        id: crypto.randomUUID(),
        userId: session.user.id,
        actionType,
        actionData,
        priority: priority || 1,
        isEnabled: isEnabled === false ? "false" : "true",
        createdAt: new Date(),
        updatedAt: new Date(),
      })
      .returning();

    return NextResponse.json({
      action: newAction[0],
      message: "Emergency action created successfully",
    });
  } catch {
    return NextResponse.json(
      { error: "Failed to create emergency action" },
      { status: 500 }
    );
  }
}
