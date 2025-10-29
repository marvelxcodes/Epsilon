import React, { useEffect, useState } from "react";
import { ScrollView, Text, View, Alert, TouchableOpacity } from "react-native";
import { Container } from "@/components/container";
import { AddMedication } from "@/components/add-medication";
import { MedicationList, type Medication } from "@/components/medication-list";
import {
	requestNotificationPermissions,
	scheduleMedicationNotification,
	cancelNotification,
	getAllScheduledNotifications,
} from "@/lib/notifications";
import * as Notifications from "expo-notifications";

export default function Reminders() {
	const [medications, setMedications] = useState<Medication[]>([]);
	const [permissionGranted, setPermissionGranted] = useState(false);

	useEffect(() => {
		const setup = async () => {
			const granted = await requestNotificationPermissions();
			setPermissionGranted(granted);
		};
		setup();
	}, []);

	const testNotification = async () => {
		try {
			await Notifications.scheduleNotificationAsync({
				content: {
					title: "🧪 Test Notification",
					body: "This is a test notification. It should appear in 5 seconds!",
					sound: true,
				},
				trigger: {
					type: Notifications.SchedulableTriggerInputTypes.TIME_INTERVAL,
					seconds: 5,
				},
			});
			Alert.alert(
				"Test Scheduled",
				"You should see a notification in 5 seconds!",
			);
		} catch (error) {
			Alert.alert("Test Failed", `Error: ${error}`);
			console.error(error);
		}
	};

	const viewScheduled = async () => {
		try {
			const scheduled = await getAllScheduledNotifications();
			Alert.alert(
				"Scheduled Notifications",
				`You have ${scheduled.length} scheduled notification(s)`,
			);
			console.log("Scheduled notifications:", scheduled);
		} catch (error) {
			console.error(error);
		}
	};

	const handleAddMedication = async (medication: {
		name: string;
		time: Date;
		notes?: string;
	}) => {
		try {
			const notificationId = await scheduleMedicationNotification(
				medication.name,
				medication.time,
				medication.notes,
			);

			const newMedication: Medication = {
				id: notificationId,
				...medication,
			};

			setMedications([...medications, newMedication]);
			Alert.alert(
				"Success",
				`Reminder set for ${medication.name} at ${medication.time.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })}`,
			);
		} catch (error) {
			Alert.alert("Error", "Failed to schedule notification");
			console.error(error);
		}
	};

	const handleDeleteMedication = async (id: string) => {
		try {
			await cancelNotification(id);
			setMedications(medications.filter((med) => med.id !== id));
			Alert.alert("Success", "Reminder deleted");
		} catch (error) {
			Alert.alert("Error", "Failed to delete notification");
			console.error(error);
		}
	};

	return (
		<Container>
			<ScrollView className="flex-1">
				<View className="px-4 py-6">
					<Text className="font-mono text-foreground text-3xl font-bold mb-2">
						💊 Reminders
					</Text>
					<Text className="text-muted-foreground mb-6">
						Never forget to take your medication
					</Text>

					{!permissionGranted && (
						<View className="mb-4 p-4 bg-destructive/10 border border-destructive rounded-lg">
							<Text className="text-destructive font-medium">
								⚠️ Notification permissions not granted. Please enable
								notifications in your device settings.
							</Text>
						</View>
					)}

					<View className="flex-row gap-2 mb-6">
						<TouchableOpacity
							className="flex-1 bg-primary/20 py-2 px-4 rounded-md border border-primary"
							onPress={testNotification}
						>
							<Text className="text-primary font-medium text-center text-sm">
								🧪 Test (5s)
							</Text>
						</TouchableOpacity>
						<TouchableOpacity
							className="flex-1 bg-muted py-2 px-4 rounded-md"
							onPress={viewScheduled}
						>
							<Text className="text-foreground font-medium text-center text-sm">
								📋 View Scheduled
							</Text>
						</TouchableOpacity>
					</View>

					<AddMedication onAdd={handleAddMedication} />
					<MedicationList
						medications={medications}
						onDelete={handleDeleteMedication}
					/>
				</View>
			</ScrollView>
		</Container>
	);
}
