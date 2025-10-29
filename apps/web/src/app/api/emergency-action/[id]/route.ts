import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { emergencyAction } from "@epsilon/db/schema/app";
import { and, eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

type Params = Promise<{ id: string }>;

// GET single emergency action
export async function GET(_request: Request, { params }: { params: Params }) {
  try {
    const { id } = await params;
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const result = await db
      .select()
      .from(emergencyAction)
      .where(
        and(
          eq(emergencyAction.id, id),
          eq(emergencyAction.userId, session.user.id)
        )
      )
      .limit(1);

    if (result.length === 0) {
      return NextResponse.json(
        { error: "Emergency action not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({ action: result[0] });
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch emergency action" },
      { status: 500 }
    );
  }
}

// PATCH update emergency action
export async function PATCH(request: Request, { params }: { params: Params }) {
  try {
    const { id } = await params;
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const body = await request.json();
    const { actionType, actionData, priority, isEnabled } = body;

    const updates: Record<string, string | number | Date> = {
      updatedAt: new Date(),
    };

    if (actionType !== undefined) {
      if (!["call", "message", "alarm"].includes(actionType)) {
        return NextResponse.json(
          { error: "Invalid action type. Must be call, message, or alarm" },
          { status: 400 }
        );
      }
      updates.actionType = actionType;
    }
    if (actionData !== undefined) {
      updates.actionData = actionData;
    }
    if (priority !== undefined) {
      updates.priority = priority;
    }
    if (isEnabled !== undefined) {
      updates.isEnabled = isEnabled ? "true" : "false";
    }

    const updated = await db
      .update(emergencyAction)
      .set(updates)
      .where(
        and(
          eq(emergencyAction.id, id),
          eq(emergencyAction.userId, session.user.id)
        )
      )
      .returning();

    if (updated.length === 0) {
      return NextResponse.json(
        { error: "Emergency action not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({
      action: updated[0],
      message: "Emergency action updated successfully",
    });
  } catch {
    return NextResponse.json(
      { error: "Failed to update emergency action" },
      { status: 500 }
    );
  }
}

// DELETE emergency action
export async function DELETE(
  _request: Request,
  { params }: { params: Params }
) {
  try {
    const { id } = await params;
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const deleted = await db
      .delete(emergencyAction)
      .where(
        and(
          eq(emergencyAction.id, id),
          eq(emergencyAction.userId, session.user.id)
        )
      )
      .returning();

    if (deleted.length === 0) {
      return NextResponse.json(
        { error: "Emergency action not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({
      message: "Emergency action deleted successfully",
    });
  } catch {
    return NextResponse.json(
      { error: "Failed to delete emergency action" },
      { status: 500 }
    );
  }
}
