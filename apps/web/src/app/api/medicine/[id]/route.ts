import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { medicine } from "@epsilon/db/schema/app";
import { and, eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

type Params = Promise<{ id: string }>;

// GET single medicine
export async function GET(request: Request, { params }: { params: Params }) {
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
      .from(medicine)
      .where(and(eq(medicine.id, id), eq(medicine.userId, session.user.id)))
      .limit(1);

    if (result.length === 0) {
      return NextResponse.json(
        { error: "Medicine not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({ medicine: result[0] });
  } catch {
    return NextResponse.json(
      { error: "Failed to fetch medicine" },
      { status: 500 }
    );
  }
}

// PUT update medicine (alias for PATCH for compatibility)
export async function PUT(request: Request, { params }: { params: Params }) {
  return PATCH(request, { params });
}

// PATCH update medicine
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
    const {
      name,
      dosage,
      frequency,
      time,
      startDate,
      endDate,
      notes,
      isActive,
      reminderEnabled,
    } = body;

    // biome-ignore lint: complex type needed for updates
    const updates: Record<string, any> = {
      updatedAt: new Date(),
    };

    if (name !== undefined && name !== null && name !== "") {
      updates.name = name;
    }
    if (dosage !== undefined && dosage !== null && dosage !== "") {
      updates.dosage = dosage;
    }
    if (frequency !== undefined && frequency !== null && frequency !== "") {
      updates.frequency = frequency;
    }
    if (time !== undefined && time !== null && time !== "") {
      updates.time = time;
    }
    if (startDate !== undefined && startDate !== null && startDate !== "") {
      updates.startDate = new Date(startDate);
    }
    if (endDate !== undefined && endDate !== null) {
      updates.endDate = endDate && endDate !== "" ? new Date(endDate) : null;
    }
    if (notes !== undefined && notes !== null) {
      updates.notes = notes;
    }
    if (isActive !== undefined && isActive !== null) {
      updates.isActive = isActive ? "true" : "false";
    }
    if (reminderEnabled !== undefined && reminderEnabled !== null) {
      updates.reminderEnabled = reminderEnabled ? "true" : "false";
    }

    const updated = await db
      .update(medicine)
      .set(updates)
      .where(and(eq(medicine.id, id), eq(medicine.userId, session.user.id)))
      .returning();

    if (updated.length === 0) {
      return NextResponse.json(
        { error: "Medicine not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({
      medicine: updated[0],
      message: "Medicine updated successfully",
    });
  } catch (error) {
    console.error("[Medicine Update Error]", error);
    return NextResponse.json(
      {
        error: "Failed to update medicine",
        details: error instanceof Error ? error.message : "Unknown error",
      },
      { status: 500 }
    );
  }
}

// DELETE medicine
export async function DELETE(request: Request, { params }: { params: Params }) {
  try {
    const { id } = await params;
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const deleted = await db
      .delete(medicine)
      .where(and(eq(medicine.id, id), eq(medicine.userId, session.user.id)))
      .returning();

    if (deleted.length === 0) {
      return NextResponse.json(
        { error: "Medicine not found" },
        { status: 404 }
      );
    }

    return NextResponse.json({ message: "Medicine deleted successfully" });
  } catch {
    return NextResponse.json(
      { error: "Failed to delete medicine" },
      { status: 500 }
    );
  }
}
