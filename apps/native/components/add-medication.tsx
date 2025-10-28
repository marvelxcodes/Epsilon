import React, { useState } from "react";
import {
	Text,
	TextInput,
	TouchableOpacity,
	View,
	Platform,
} from "react-native";
import DateTimePicker from "@react-native-community/datetimepicker";

interface AddMedicationProps {
	onAdd: (medication: {
		name: string;
		time: Date;
		notes?: string;
	}) => void;
}

export function AddMedication({ onAdd }: AddMedicationProps) {
	const [name, setName] = useState("");
	const [time, setTime] = useState(new Date());
	const [notes, setNotes] = useState("");
	const [showTimePicker, setShowTimePicker] = useState(false);

	const handleAdd = () => {
		if (name.trim()) {
			onAdd({ name: name.trim(), time, notes: notes.trim() });
			setName("");
			setNotes("");
			setTime(new Date());
		}
	};

	const onTimeChange = (event: any, selectedTime?: Date) => {
		setShowTimePicker(Platform.OS === "ios");
		if (selectedTime) {
			setTime(selectedTime);
		}
	};

	return (
		<View className="mb-6 rounded-lg border border-border p-4 bg-card">
			<Text className="mb-4 font-bold text-foreground text-xl">
				Add Medication Reminder
			</Text>

			<Text className="text-foreground mb-2">Medication Name</Text>
			<TextInput
				className="bg-background border border-border rounded-md p-3 mb-4 text-foreground"
				placeholder="e.g., Aspirin"
				placeholderTextColor="#999"
				value={name}
				onChangeText={setName}
			/>

			<Text className="text-foreground mb-2">Time</Text>
			<TouchableOpacity
				className="bg-background border border-border rounded-md p-3 mb-4"
				onPress={() => setShowTimePicker(true)}
			>
				<Text className="text-foreground">
					{time.toLocaleTimeString([], {
						hour: "2-digit",
						minute: "2-digit",
					})}
				</Text>
			</TouchableOpacity>

			{showTimePicker && (
				<DateTimePicker
					value={time}
					mode="time"
					is24Hour={false}
					display="default"
					onChange={onTimeChange}
				/>
			)}

			<Text className="text-foreground mb-2">Notes (Optional)</Text>
			<TextInput
				className="bg-background border border-border rounded-md p-3 mb-4 text-foreground"
				placeholder="e.g., Take with food"
				placeholderTextColor="#999"
				value={notes}
				onChangeText={setNotes}
				multiline
			/>

			<TouchableOpacity
				className="bg-primary py-3 px-4 rounded-md"
				onPress={handleAdd}
			>
				<Text className="text-white font-medium text-center">
					Add Reminder
				</Text>
			</TouchableOpacity>
		</View>
	);
}
