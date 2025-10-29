import { drizzle } from "drizzle-orm/node-postgres";

export const db = drizzle(process.env.DATABASE_URL || "");

export * from "./schema/app";
export * from "./schema/auth";
