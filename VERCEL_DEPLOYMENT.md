# Vercel Deployment Setup

## Configuration

This project uses a monorepo structure with Turbo and is configured for Vercel deployment.

### Environment Variables

The following environment variables need to be set in Vercel:

#### Required for all builds:
- `BETTER_AUTH_SECRET` - Secret key for Better Auth
- `BETTER_AUTH_URL` - URL for Better Auth (e.g., https://your-domain.vercel.app)
- `CORS_ORIGIN` - CORS origin URL
- `DATABASE_URL` - PostgreSQL database connection string

#### Firebase Admin SDK (required for FCM):
- `FIREBASE_PROJECT_ID` - Your Firebase project ID
- `FIREBASE_PRIVATE_KEY` - Your Firebase private key (from service account JSON)
- `FIREBASE_CLIENT_EMAIL` - Your Firebase client email (from service account JSON)

### Project Settings in Vercel

1. **Root Directory**: Keep as `.` (root)
2. **Build Command**: Automatically detected from `vercel.json`
3. **Install Command**: `bun install`
4. **Output Directory**: `.next` (automatically detected)
5. **Framework Preset**: Next.js

### Build Configuration

The build is configured in:
- `vercel.json` - Vercel-specific settings
- `turbo.json` - Turbo build cache settings
- `apps/web/next.config.ts` - Next.js configuration with monorepo support

### Troubleshooting

If you encounter the error: `The file "/vercel/path0/apps/web/.next/routes-manifest.json" couldn't be found`:

1. Ensure all environment variables are set in Vercel
2. Check that the build completes successfully in the build logs
3. Verify that `outputFileTracingRoot` is set in `next.config.ts`
4. Make sure `transpilePackages` includes all workspace dependencies

### Local Development

```bash
# Install dependencies
bun install

# Run development server
bun dev

# Build for production
bun run build
```
