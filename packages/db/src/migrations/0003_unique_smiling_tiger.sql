CREATE TYPE "public"."action_type" AS ENUM('call', 'message', 'alarm');--> statement-breakpoint
CREATE TABLE "device" (
	"id" text PRIMARY KEY NOT NULL,
	"user_id" text NOT NULL,
	"device_name" text NOT NULL,
	"device_token" text NOT NULL,
	"device_model" text,
	"os_version" text,
	"app_version" text,
	"last_active_at" timestamp NOT NULL,
	"created_at" timestamp NOT NULL,
	"updated_at" timestamp NOT NULL
);
--> statement-breakpoint
CREATE TABLE "emergency_action" (
	"id" text PRIMARY KEY NOT NULL,
	"user_id" text NOT NULL,
	"action_type" "action_type" NOT NULL,
	"action_data" text NOT NULL,
	"priority" integer DEFAULT 1 NOT NULL,
	"is_enabled" text DEFAULT 'true' NOT NULL,
	"created_at" timestamp NOT NULL,
	"updated_at" timestamp NOT NULL
);
--> statement-breakpoint
CREATE TABLE "medicine" (
	"id" text PRIMARY KEY NOT NULL,
	"user_id" text NOT NULL,
	"name" text NOT NULL,
	"dosage" text NOT NULL,
	"frequency" text NOT NULL,
	"time" text NOT NULL,
	"start_date" timestamp NOT NULL,
	"end_date" timestamp,
	"notes" text,
	"is_active" text DEFAULT 'true' NOT NULL,
	"reminder_enabled" text DEFAULT 'true' NOT NULL,
	"created_at" timestamp NOT NULL,
	"updated_at" timestamp NOT NULL
);
--> statement-breakpoint
CREATE TABLE "medicine_log" (
	"id" text PRIMARY KEY NOT NULL,
	"medicine_id" text NOT NULL,
	"user_id" text NOT NULL,
	"taken_at" timestamp NOT NULL,
	"scheduled_for" timestamp NOT NULL,
	"status" text NOT NULL,
	"notes" text,
	"created_at" timestamp NOT NULL
);
--> statement-breakpoint
ALTER TABLE "device" ADD CONSTRAINT "device_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "emergency_action" ADD CONSTRAINT "emergency_action_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "medicine" ADD CONSTRAINT "medicine_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "medicine_log" ADD CONSTRAINT "medicine_log_medicine_id_medicine_id_fk" FOREIGN KEY ("medicine_id") REFERENCES "public"."medicine"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "medicine_log" ADD CONSTRAINT "medicine_log_user_id_user_id_fk" FOREIGN KEY ("user_id") REFERENCES "public"."user"("id") ON DELETE cascade ON UPDATE no action;