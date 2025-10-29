import { integer, pgEnum, pgTable, text, timestamp } from "drizzle-orm/pg-core";
import { user } from "./auth";

export const actionTypeEnum = pgEnum("action_type", [
  "call",
  "message",
  "alarm",
]);

export const device = pgTable("device", {
  id: text("id").primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
  deviceName: text("device_name").notNull(),
  deviceToken: text("device_token").notNull(),
  deviceModel: text("device_model"),
  osVersion: text("os_version"),
  appVersion: text("app_version"),
  lastActiveAt: timestamp("last_active_at").notNull(),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
});

export const medicine = pgTable("medicine", {
  id: text("id").primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
  name: text("name").notNull(),
  dosage: text("dosage").notNull(),
  frequency: text("frequency").notNull(), // e.g., "daily", "twice daily", "weekly"
  time: text("time").notNull(), // e.g., "08:00", "14:00,20:00" for multiple times
  startDate: timestamp("start_date").notNull(),
  endDate: timestamp("end_date"),
  notes: text("notes"),
  isActive: text("is_active").notNull().default("true"),
  reminderEnabled: text("reminder_enabled").notNull().default("true"),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
});

export const emergencyAction = pgTable("emergency_action", {
  id: text("id").primaryKey(),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
  actionType: actionTypeEnum("action_type").notNull(),
  actionData: text("action_data").notNull(), // phone number for call/message, alarm sound for alarm
  priority: integer("priority").notNull().default(1), // 1 is highest
  isEnabled: text("is_enabled").notNull().default("true"),
  createdAt: timestamp("created_at").notNull(),
  updatedAt: timestamp("updated_at").notNull(),
});

export const medicineLog = pgTable("medicine_log", {
  id: text("id").primaryKey(),
  medicineId: text("medicine_id")
    .notNull()
    .references(() => medicine.id, { onDelete: "cascade" }),
  userId: text("user_id")
    .notNull()
    .references(() => user.id, { onDelete: "cascade" }),
  takenAt: timestamp("taken_at").notNull(),
  scheduledFor: timestamp("scheduled_for").notNull(),
  status: text("status").notNull(), // "taken", "missed", "skipped"
  notes: text("notes"),
  createdAt: timestamp("created_at").notNull(),
});
