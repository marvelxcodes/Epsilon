import * as Notifications from "expo-notifications";
import { Platform, Alert } from "react-native";

// Configure how notifications should be handled when app is in foreground
Notifications.setNotificationHandler({
	handleNotification: async () => ({
		shouldShowAlert: true,
		shouldPlaySound: true,
		shouldSetBadge: false,
		shouldShowBanner: true,
		shouldShowList: true,
	}),
});

export async function requestNotificationPermissions() {
	try {
		const { status: existingStatus } =
			await Notifications.getPermissionsAsync();
		let finalStatus = existingStatus;

		if (existingStatus !== "granted") {
			const { status } = await Notifications.requestPermissionsAsync();
			finalStatus = status;
		}

		if (finalStatus !== "granted") {
			Alert.alert(
				"Permission Required",
				"Please enable notifications in your device settings to receive medication reminders.",
			);
			return false;
		}

		if (Platform.OS === "android") {
			await Notifications.setNotificationChannelAsync("medication", {
				name: "Medication Reminders",
				importance: Notifications.AndroidImportance.MAX,
				vibrationPattern: [0, 250, 250, 250],
				lightColor: "#FF231F7C",
				sound: "default",
			});
		}

		return true;
	} catch (error) {
		console.error("Error requesting permissions:", error);
		return false;
	}
}

export async function scheduleMedicationNotification(
	medicationName: string,
	time: Date,
	notes?: string,
) {
	try {
		const trigger = new Date(time);
		const now = new Date();

		// If time is in the past today, schedule for tomorrow
		if (trigger < now) {
			trigger.setDate(trigger.getDate() + 1);
		}

		const notificationId = await Notifications.scheduleNotificationAsync({
			content: {
				title: "💊 Medication Reminder",
				body: `Time to take ${medicationName}${notes ? `\n${notes}` : ""}`,
				sound: true,
				priority: Notifications.AndroidNotificationPriority.HIGH,
				...(Platform.OS === "android" && {
					channelId: "medication",
				}),
			},
			trigger: {
				type: Notifications.SchedulableTriggerInputTypes.CALENDAR,
				repeats: true,
				hour: trigger.getHours(),
				minute: trigger.getMinutes(),
			},
		});

		return notificationId;
	} catch (error) {
		console.error("Error scheduling notification:", error);
		throw error;
	}
}

export async function cancelNotification(notificationId: string) {
	await Notifications.cancelScheduledNotificationAsync(notificationId);
}

export async function getAllScheduledNotifications() {
	return await Notifications.getAllScheduledNotificationsAsync();
}
