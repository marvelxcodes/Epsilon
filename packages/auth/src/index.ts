import { expo } from "@better-auth/expo";
import { db } from "@epsilon/db";
import * as schema from "@epsilon/db/schema/auth";
import { type BetterAuthOptions, betterAuth } from "better-auth";
import { drizzleAdapter } from "better-auth/adapters/drizzle";
import { nextCookies } from "better-auth/next-js";

export const auth = betterAuth<BetterAuthOptions>({
  baseURL: process.env.BETTER_AUTH_URL || "http://localhost:3001",
  database: drizzleAdapter(db, {
    provider: "pg",

    schema,
  }),
  trustedOrigins: [
    process.env.CORS_ORIGIN || "",
    "http://localhost:3001",
    "mybettertapp://",
    "exp://",
  ],
  advanced: {
    useSecureCookies: process.env.NODE_ENV === "production",
    crossSubDomainCookies: {
      enabled: true,
    },
    disableCSRFCheck: true,
  },
  emailAndPassword: {
    enabled: true,
    requireEmailVerification: false,
  },
  plugins: [nextCookies(), expo()],
});
