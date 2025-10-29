import React from "react";
import { Text, TouchableOpacity, View, ScrollView } from "react-native";

export interface Medication {
	id: string;
	name: string;
	time: Date;
	notes?: string;
}

interface MedicationListProps {
	medications: Medication[];
	onDelete: (id: string) => void;
}

export function MedicationList({ medications, onDelete }: MedicationListProps) {
	if (medications.length === 0) {
		return (
			<View className="mb-6 rounded-lg border border-border p-6 bg-card">
				<Text className="text-muted-foreground text-center">
					No medication reminders yet. Add one above!
				</Text>
			</View>
		);
	}

	return (
		<View className="mb-6">
			<Text className="mb-3 font-bold text-foreground text-xl">
				Your Medications
			</Text>
			{medications.map((med) => (
				<View
					key={med.id}
					className="mb-3 rounded-lg border border-border p-4 bg-card"
				>
					<View className="flex-row justify-between items-start mb-2">
						<View className="flex-1">
							<Text className="text-foreground font-medium text-lg">
								{med.name}
							</Text>
							<Text className="text-primary font-bold mt-1">
								{med.time.toLocaleTimeString([], {
									hour: "2-digit",
									minute: "2-digit",
								})}
							</Text>
							{med.notes ? (
								<Text className="text-muted-foreground mt-2 text-sm">
									{med.notes}
								</Text>
							) : null}
						</View>
						<TouchableOpacity
							className="bg-destructive py-2 px-3 rounded-md"
							onPress={() => onDelete(med.id)}
						>
							<Text className="text-white font-medium">Delete</Text>
						</TouchableOpacity>
					</View>
				</View>
			))}
		</View>
	);
}
