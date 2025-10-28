import React, { useEffect, useState } from "react";
import { ScrollView, Text, View, Alert } from "react-native";
import { Container } from "@/components/container";
import { AddMedication } from "@/components/add-medication";
import { MedicationList, type Medication } from "@/components/medication-list";
import {
	requestNotificationPermissions,
	scheduleMedicationNotification,
	cancelNotification,
} from "@/lib/notifications";

export default function Home() {
	const [medications, setMedications] = useState<Medication[]>([]);

	useEffect(() => {
		requestNotificationPermissions();
	}, []);

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
						💊 MEDICATION REMINDER
					</Text>
					<Text className="text-muted-foreground mb-6">
						Never forget to take your medication
					</Text>

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
