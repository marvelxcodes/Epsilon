import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  typedRoutes: true,
  reactCompiler: true,
  outputFileTracingRoot: require("node:path").join(__dirname, "../../"),
  transpilePackages: ["@epsilon/auth"],
};

export default nextConfig;
