import React, { useEffect, useState } from 'react';
import { Container } from '@/components/container';
import { ScrollView, View, Text, TouchableOpacity, Alert } from 'react-native';
import ReminderModal from '@/components/reminder-modal';
import * as Notifications from '@/lib/notifications';

type Frequency = {
	type: 'daily' | 'weekly' | 'custom';
	daysOfWeek?: number[];
	times: Date[];
};

type MedicationItem = {
	id: string;
	name: string;
	frequency: Frequency;
	notes?: string;
	notificationIds?: string[];
};

export default function RemindersTab() {
	const [modalVisible, setModalVisible] = useState(false);
	const [medications, setMedications] = useState<MedicationItem[]>([]);

	useEffect(() => {
		// Request permissions when screen mounts
		Notifications.requestNotificationPermissions();
	}, []);

	const handleSave = async (payload: { name: string; frequency: Frequency; notes?: string }) => {
		try {
			const notificationIds: string[] = [];

			const now = new Date();

			const scheduleForDate = async (date: Date) => {
				const nid = await Notifications.scheduleMedicationNotification(payload.name, date, payload.notes);
				if (nid) notificationIds.push(nid);
			};

			// For each time provided, compute next occurrence(s) depending on frequency
			for (const t of payload.frequency.times) {
				const timeDate = new Date(t);

				if (payload.frequency.type === 'daily') {
					// schedule next occurrence for that time
					const scheduled = new Date(now);
					scheduled.setHours(timeDate.getHours(), timeDate.getMinutes(), 0, 0);
					if (scheduled <= now) scheduled.setDate(scheduled.getDate() + 1);
					await scheduleForDate(scheduled);
				} else {
					const days = payload.frequency.daysOfWeek || [now.getDay()];
					for (const day of days) {
						// compute next date for this weekday
						const scheduled = new Date(now);
						scheduled.setHours(timeDate.getHours(), timeDate.getMinutes(), 0, 0);
						// move forward until we hit the desired weekday
						let offset = (day - scheduled.getDay() + 7) % 7;
						if (offset === 0 && scheduled <= now) offset = 7; // next week
						scheduled.setDate(scheduled.getDate() + offset);
						await scheduleForDate(scheduled);
					}
				}
			}

			const newMed: MedicationItem = {
				id: Date.now().toString(),
				name: payload.name,
				frequency: payload.frequency,
				notes: payload.notes,
				notificationIds,
			};

			setMedications(prev => [newMed, ...prev]);
		} catch (error) {
			console.error('Failed to schedule notification:', error);
			Alert.alert('Error', 'Could not schedule reminder.');
		}
	};

	const handleDelete = async (id: string) => {
		const med = medications.find(m => m.id === id);
		if (!med) return;
		try {
			if (med.notificationIds) {
				for (const nid of med.notificationIds) {
					await Notifications.cancelNotification(nid);
				}
			}
			setMedications(prev => prev.filter(m => m.id !== id));
		} catch (error) {
			console.error('Failed to cancel notifications:', error);
			Alert.alert('Error', 'Could not delete reminder.');
		}
	};

	return (
		<Container>
			<ScrollView className="flex-1 p-6">
				<View className="py-4">
					<Text className="text-3xl font-bold text-foreground mb-2">Reminders</Text>
					<Text className="text-muted-foreground">Create medication reminders with time and repeat days.</Text>
				</View>

				{/* Simple list */}
				<View className="mt-6">
					{medications.length === 0 ? (
						<View className="mb-6 rounded-lg border border-border p-6 bg-card">
							<Text className="text-muted-foreground text-center">No reminders yet. Tap + to add one.</Text>
						</View>
					) : (
						medications.map(med => (
							<View key={med.id} className="mb-3 rounded-lg border border-border p-4 bg-card">
								<View className="flex-row justify-between items-start mb-2">
									<View className="flex-1">
										<Text className="text-foreground font-medium text-lg">{med.name}</Text>
										<Text className="text-primary font-bold mt-1">
											{med.frequency.times.map(t => t.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })).join(', ')}
										</Text>
										<Text className="text-muted-foreground mt-1">
											{med.frequency.type === 'daily' ? 'Every day' : med.frequency.daysOfWeek?.map(d => ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'][d]).join(', ')}
										</Text>
										{med.notes ? <Text className="text-muted-foreground mt-2 text-sm">{med.notes}</Text> : null}
									</View>
									<TouchableOpacity className="bg-destructive py-2 px-3 rounded-md" onPress={() => handleDelete(med.id)}>
										<Text className="text-white font-medium">Delete</Text>
									</TouchableOpacity>
								</View>
							</View>
						))
					)}
				</View>

				{/* Debug button */}
				<TouchableOpacity
					onPress={async () => {
						const notifications = await Notifications.getAllScheduledNotifications();
						console.log('Current scheduled notifications:', notifications);
						Alert.alert(
							'Scheduled Notifications',
							`Found ${notifications.length} scheduled notification(s)`
						);
					}}
					className="mb-4 bg-secondary p-4 rounded-lg"
				>
					<Text className="text-center text-secondary-foreground">Check Scheduled Notifications</Text>
				</TouchableOpacity>

				{/* spacer to avoid content under FAB */}
				<View style={{ height: 120 }} />
			</ScrollView>

			{/* Floating + button */}
			<TouchableOpacity
				onPress={() => setModalVisible(true)}
				style={{ position: 'absolute', right: 20, bottom: 30 }}
				className="bg-primary p-4 rounded-full shadow-lg"
				accessibilityLabel="Add reminder"
			>
				<Text className="text-white font-bold text-xl">+</Text>
			</TouchableOpacity>

			<ReminderModal visible={modalVisible} onClose={() => setModalVisible(false)} onSave={handleSave} />
		</Container>
	);
}
