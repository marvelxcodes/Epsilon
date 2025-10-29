import { auth } from "@epsilon/auth";
import { db } from "@epsilon/db";
import { medicine } from "@epsilon/db/schema/app";
import { and, eq } from "drizzle-orm";
import { headers } from "next/headers";
import { NextResponse } from "next/server";

// GET all medicines for user
export async function GET(request: Request) {
  try {
    const session = await auth.api.getSession({
      headers: await headers(),
    });

    if (!session?.user) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const activeOnly = searchParams.get("activeOnly") === "true";

    let query = db
      .select()
      .from(medicine)
      .where(eq(medicine.userId, session.user.id));

    if (activeOnly) {
      query = db
        .select()
        .from(medicine)
        .where(
          and(
            eq(medicine.userId, session.user.id),
            eq(medicine.isActive, "true")
          )
        );
    }

    const medicines = await query;

    return NextResponse.json({ medicines });
  } catch (error) {
    return NextResponse.json(
      { error: "Failed to fetch medicines" },
      { status: 500 }
    );
  }
}

// POST create new medicine
export async function POST(request: Request) {
  try {
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
      reminderEnabled,
    } = body;

    if (!(name && dosage && frequency && time && startDate)) {
      return NextResponse.json(
        { error: "Name, dosage, frequency, time, and start date are required" },
        { status: 400 }
      );
    }

    const newMedicine = await db
      .insert(medicine)
      .values({
        id: crypto.randomUUID(),
        userId: session.user.id,
        name,
        dosage,
        frequency,
        time,
        startDate: new Date(startDate),
        endDate: endDate ? new Date(endDate) : null,
        notes: notes || null,
        isActive: "true",
        reminderEnabled: reminderEnabled ? "true" : "false",
        createdAt: new Date(),
        updatedAt: new Date(),
      })
      .returning();

    return NextResponse.json({
      medicine: newMedicine[0],
      message: "Medicine created successfully",
    });
  } catch (error) {
    return NextResponse.json(
      { error: "Failed to create medicine" },
      { status: 500 }
    );
  }
}
