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
		const hour = time.getHours();
		const minute = time.getMinutes();

		// Calculate seconds until the next occurrence of this time
		const now = new Date();
		let scheduledTime = new Date();
		scheduledTime.setHours(hour, minute, 0, 0);

		// If the time has passed today, schedule for tomorrow
		if (scheduledTime <= now) {
			scheduledTime.setDate(scheduledTime.getDate() + 1);
		}

		const secondsUntilTrigger = Math.floor(
			(scheduledTime.getTime() - now.getTime()) / 1000,
		);

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
				type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
				seconds: secondsUntilTrigger,
				repeats: false,
			},
		});

		console.log(
			`Notification scheduled for ${scheduledTime.toLocaleString()} (in ${secondsUntilTrigger} seconds)`,
		);
		return notificationId;
	} catch (error) {
		console.error("Error scheduling notification:", error);
		Alert.alert(
			"Scheduling Failed",
			`Could not schedule notification. Error: ${error}`,
		);
		throw error;
	}
}

export async function cancelNotification(notificationId: string) {
	try {
		await Notifications.cancelScheduledNotificationAsync(notificationId);
		console.log("Notification cancelled:", notificationId);
	} catch (error) {
		console.error("Error cancelling notification:", error);
		throw error;
	}
}

export async function getAllScheduledNotifications() {
	return await Notifications.getAllScheduledNotificationsAsync();
}

// Set up notification response listener to reschedule daily notifications
export function setupNotificationListener() {
	return Notifications.addNotificationReceivedListener((notification) => {
		console.log("Notification received:", notification);
		// You can add logic here to reschedule the notification for tomorrow
	});
}
