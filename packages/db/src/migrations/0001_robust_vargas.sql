DROP TABLE IF EXISTS "verification" CASCADE;--> statement-breakpoint
ALTER TABLE "user" DROP COLUMN IF EXISTS "email_verified";--> statement-breakpoint
ALTER TABLE "user" DROP COLUMN IF EXISTS "image";