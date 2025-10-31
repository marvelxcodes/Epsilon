# Supabase Configuration

## Setup Instructions

1. Copy this file or create your configuration in `SupabaseManager.kt`
2. Replace the placeholder values with your actual Supabase credentials
3. Never commit your actual credentials to version control

## Configuration Template

```kotlin
// File: app/src/main/java/com/epsilon/app/data/supabase/SupabaseManager.kt

companion object {
    private const val TAG = "SupabaseManager"
    
    // TODO: Replace with your Supabase project URL
    // Example: "https://abcdefghijklmnop.supabase.co"
    private const val SUPABASE_URL = "YOUR_SUPABASE_URL"
    
    // TODO: Replace with your Supabase anon/public key
    // Found in: Supabase Dashboard > Settings > API
    private const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
    
    private const val FALLS_TABLE = "falls"
}
```

## Where to Find Your Credentials

1. **SUPABASE_URL**
   - Go to your Supabase Dashboard
   - Navigate to: Settings > API
   - Copy the "Project URL" value
   - Format: `https://your-project-id.supabase.co`

2. **SUPABASE_ANON_KEY**
   - Go to your Supabase Dashboard
   - Navigate to: Settings > API
   - Copy the "anon" key under "Project API keys"
   - This is a long JWT token string

## Database Setup

Run this SQL in your Supabase SQL Editor:

```sql
-- Create the falls table
create table falls (
  id uuid primary key default uuid_generate_v4(),
  user_id uuid references auth.users(id),
  is_fall boolean default false,
  detected_at timestamp default now()
);

-- Enable Row Level Security
alter table falls enable row level security;

-- Policy: Users can only read their own falls
create policy "Users can view own falls"
  on falls for select
  using (auth.uid() = user_id);

-- Policy: Anyone can insert falls (for wearable device)
create policy "Anyone can insert falls"
  on falls for insert
  with check (true);

-- Enable Realtime for the falls table
alter publication supabase_realtime add table falls;
```

## Security Considerations

### For Production

Consider using one of these approaches:

1. **Environment Variables** (Recommended)
   ```kotlin
   private const val SUPABASE_URL = BuildConfig.SUPABASE_URL
   private const val SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY
   ```

   Then in `build.gradle.kts`:
   ```kotlin
   android {
       defaultConfig {
           buildConfigField("String", "SUPABASE_URL", "\"${System.getenv("SUPABASE_URL") ?: ""}\"")
           buildConfigField("String", "SUPABASE_ANON_KEY", "\"${System.getenv("SUPABASE_ANON_KEY") ?: ""}\"")
       }
   }
   ```

2. **local.properties** (Good for local development)
   Add to `local.properties` (never commit this file):
   ```properties
   supabase.url=https://your-project.supabase.co
   supabase.key=your-anon-key-here
   ```

   Then read in `build.gradle.kts`:
   ```kotlin
   val localProperties = Properties()
   localProperties.load(project.rootProject.file("local.properties").inputStream())

   android {
       defaultConfig {
           buildConfigField("String", "SUPABASE_URL", "\"${localProperties["supabase.url"]}\"")
           buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties["supabase.key"]}\"")
       }
   }
   ```

3. **Secrets Management** (Best for teams)
   Use tools like:
   - Google Secret Manager
   - AWS Secrets Manager
   - HashiCorp Vault
   - GitHub Secrets (for CI/CD)

## Testing the Connection

After configuration, test the connection:

1. Build and run the app
2. Login with your user account
3. Check logcat for connection messages:
   ```bash
   adb logcat | grep SupabaseManager
   ```

4. Look for:
   - "Subscribing to falls for user: [user-id]"
   - "Successfully subscribed to falls channel"

5. Test by inserting a fall record manually in Supabase:
   ```javascript
   // From Supabase SQL Editor or your wearable
   insert into falls (user_id, is_fall)
   values ('your-user-id', true);
   ```

## Troubleshooting

### Error: "Invalid API key"
- Double-check your SUPABASE_ANON_KEY
- Make sure you copied the full key (it's very long)
- Ensure there are no extra spaces or newlines

### Error: "Connection failed"
- Verify SUPABASE_URL is correct
- Check network connectivity
- Verify your Supabase project is active

### Error: "Permission denied"
- Check your RLS policies
- Ensure user is authenticated
- Verify user_id matches the logged-in user

### No Realtime Updates Received
- Verify Realtime is enabled for the table:
  ```sql
  alter publication supabase_realtime add table falls;
  ```
- Check if the service is running (notification should be visible)
- Check logcat for errors

## Additional Resources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase Realtime](https://supabase.com/docs/guides/realtime)
- [Row Level Security](https://supabase.com/docs/guides/auth/row-level-security)
