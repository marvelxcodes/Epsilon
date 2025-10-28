# 💊 Medication Reminder App

A simple medication reminder app that sends notifications at scheduled times.

## Features

- ✅ Add medication reminders with name, time, and notes
- ✅ Daily recurring notifications at the specified time
- ✅ Delete reminders when no longer needed
- ✅ Clean, simple interface focused on medication management

## How It Works

1. **Add a Medication**: Enter the medication name, select a time, and optionally add notes (e.g., "Take with food")
2. **Set Reminder**: The app schedules a daily notification at the specified time
3. **Receive Notifications**: You'll get a notification every day at the scheduled time
4. **Manage Reminders**: View all your medications and delete them when needed

## Files Created/Modified

- `components/add-medication.tsx` - Form to add new medication reminders
- `components/medication-list.tsx` - Display list of all medications
- `lib/notifications.ts` - Notification scheduling logic
- `app/(drawer)/index.tsx` - Main home screen (updated)
- `app.json` - Added notification permissions

## Next Steps

To test the app:

1. Run the app: `cd apps/native && bun run dev`
2. Add a medication with a time a few minutes in the future
3. Close the app or put it in the background
4. Wait for the notification to appear

## Important Notes

- Notifications repeat daily at the same time
- If you set a time that's already passed today, it will schedule for tomorrow
- Make sure to grant notification permissions when prompted
- The app needs to be rebuilt (`expo prebuild`) if you want to test on a physical device

## Future Enhancements (Optional)

- [ ] Persist medications to storage (AsyncStorage or database)
- [ ] Add multiple times per day for same medication
- [ ] Add dosage information
- [ ] Track medication history (taken/missed)
- [ ] Add refill reminders
- [ ] Custom notification sounds
