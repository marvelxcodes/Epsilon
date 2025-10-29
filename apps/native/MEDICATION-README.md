# 🏠 Emergency Contact & Fall Detection App

A simple emergency assistance app with quick access to contacts and SOS functionality.

## Features

### Home Screen

- ✅ **Live Clock** - Shows current time and date
- ✅ **Quick Contacts** - One-tap calling for frequently contacted people
- ✅ **Emergency SOS Button** - Send instant alerts to emergency contacts
- ✅ Clean, large UI perfect for quick access

### Settings Screen

- ✅ **Manage Quick Contacts** - Add/remove contacts for the home screen
- ✅ **Manage SOS Contacts** - Configure who receives emergency alerts
- ✅ **Persistent Storage** - All contacts saved locally using AsyncStorage

### Fall Detection Screen

- 🚧 **Coming Soon** - Automatic fall detection with emergency alerts

## Navigation

The app uses **bottom tab navigation** with 3 tabs:

1. **Home** (🏠) - Main screen with clock, contacts, and SOS
2. **Fall Detection** (💓) - Placeholder for future feature
3. **Settings** (⚙️) - Configure contacts

## How to Use

1. **Setup Contacts**:

   - Go to Settings tab
   - Add "Quick Contacts" for easy calling from home
   - Add "SOS Contacts" who will receive emergency alerts

2. **Home Screen**:

   - View current time prominently displayed
   - Tap any quick contact to call them
   - Press the big red SOS button in emergencies

3. **SOS Functionality**:
   - Press the SOS button
   - Confirm the alert
   - All configured SOS contacts will be notified

## Technical Details

### Files Structure

```
app/(drawer)/
  ├── index.tsx              # Home screen (clock, contacts, SOS)
  └── (tabs)/
      ├── _layout.tsx        # Tab navigation setup
      ├── fall-detection.tsx # Placeholder screen
      └── settings.tsx       # Contact management
```

### Data Storage

- Uses `@react-native-async-storage/async-storage`
- Contacts stored locally on device
- Persists between app restarts

### Future Enhancements

- [ ] Actual phone call integration using `react-native-communications`
- [ ] SMS sending for SOS alerts
- [ ] Fall detection using device accelerometer
- [ ] Location sharing in SOS messages
- [ ] Emergency services integration (911/112)
- [ ] Medical information storage
- [ ] Medication reminders
