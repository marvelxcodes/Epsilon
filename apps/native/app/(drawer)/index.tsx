import React, { useEffect, useState } from "react";
import { View, Text, TouchableOpacity, Alert, ScrollView } from "react-native";
import { Container } from "@/components/container";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { Ionicons } from "@expo/vector-icons";

interface Contact {
	id: string;
	name: string;
	phone: string;
}

export default function Home() {
	const [currentTime, setCurrentTime] = useState(new Date());
	const [frequentContacts, setFrequentContacts] = useState<Contact[]>([]);
	const [sosContacts, setSosContacts] = useState<Contact[]>([]);

	// Update time every second
	useEffect(() => {
		const timer = setInterval(() => {
			setCurrentTime(new Date());
		}, 1000);
		return () => clearInterval(timer);
	}, []);

	// Load saved contacts
	useEffect(() => {
		loadContacts();
	}, []);

	const loadContacts = async () => {
		try {
			const frequent = await AsyncStorage.getItem("frequentContacts");
			const sos = await AsyncStorage.getItem("sosContacts");

			if (frequent) setFrequentContacts(JSON.parse(frequent));
			if (sos) setSosContacts(JSON.parse(sos));
		} catch (error) {
			console.error("Error loading contacts:", error);
		}
	};

	const handleSOSPress = () => {
		if (sosContacts.length === 0) {
			Alert.alert(
				"No SOS Contacts",
				"Please add SOS contacts in Settings first.",
				[{ text: "OK" }]
			);
			return;
		}

		Alert.alert(
			"Send SOS Alert",
			`Send emergency alert to ${sosContacts.length} contact(s)?`,
			[
				{ text: "Cancel", style: "cancel" },
				{
					text: "Send SOS",
					style: "destructive",
					onPress: () => sendSOSAlert(),
				},
			]
		);
	};

	const sendSOSAlert = () => {
		// TODO: Implement actual SOS functionality (SMS, call, etc.)
		Alert.alert("SOS Alert Sent", "Emergency alert sent to all SOS contacts!");
		console.log("Sending SOS to:", sosContacts);
	};

	const callContact = (contact: Contact) => {
		Alert.alert("Call", `Calling ${contact.name}...`, [{ text: "OK" }]);
		// TODO: Implement actual call functionality
		console.log("Calling:", contact);
	};

	const formatTime = (date: Date) => {
		return date.toLocaleTimeString([], {
			hour: "2-digit",
			minute: "2-digit",
			second: "2-digit",
		});
	};

	const formatDate = (date: Date) => {
		return date.toLocaleDateString([], {
			weekday: "long",
			year: "numeric",
			month: "long",
			day: "numeric",
		});
	};

	return (
		<Container>
			<ScrollView className="flex-1">
				<View className="px-6 py-8">
					{/* Time Display */}
					<View className="mb-8 items-center">
						<Text className="text-6xl font-bold text-foreground mb-2">
							{formatTime(currentTime)}
						</Text>
						<Text className="text-lg text-muted-foreground">
							{formatDate(currentTime)}
						</Text>
					</View>

					{/* SOS Button */}
					<TouchableOpacity
						className="bg-red-600 py-6 px-8 rounded-2xl mb-8 shadow-lg active:bg-red-700"
						onPress={handleSOSPress}
					>
						<View className="items-center">
							<Ionicons name="warning" size={48} color="white" />
							<Text className="text-white font-bold text-2xl mt-2">
								EMERGENCY SOS
							</Text>
							<Text className="text-white/80 text-sm mt-1">
								{sosContacts.length > 0
									? `Send alert to ${sosContacts.length} contact(s)`
									: "Configure in Settings"}
							</Text>
						</View>
					</TouchableOpacity>

					{/* Frequent Contacts */}
					<View className="mb-6">
						<Text className="text-xl font-bold text-foreground mb-4">
							Quick Contacts
						</Text>
						{frequentContacts.length > 0 ? (
							<View className="gap-3">
								{frequentContacts.map((contact) => (
									<TouchableOpacity
										key={contact.id}
										className="bg-card border border-border rounded-lg p-4 flex-row items-center justify-between active:bg-muted"
										onPress={() => callContact(contact)}
									>
										<View className="flex-row items-center gap-3">
											<View className="bg-primary/20 w-12 h-12 rounded-full items-center justify-center">
												<Ionicons name="person" size={24} color="hsl(221.2 83.2% 53.3%)" />
											</View>
											<View>
												<Text className="text-foreground font-medium text-lg">
													{contact.name}
												</Text>
												<Text className="text-muted-foreground text-sm">
													{contact.phone}
												</Text>
											</View>
										</View>
										<Ionicons name="call" size={24} color="hsl(142.1 76.2% 36.3%)" />
									</TouchableOpacity>
								))}
							</View>
						) : (
							<View className="bg-muted rounded-lg p-6 items-center">
								<Ionicons name="people-outline" size={48} color="hsl(215.4 16.3% 46.9%)" />
								<Text className="text-muted-foreground text-center mt-2">
									No quick contacts added yet.
								</Text>
								<Text className="text-muted-foreground text-center text-sm mt-1">
									Add them in Settings
								</Text>
							</View>
						)}
					</View>
				</View>
			</ScrollView>
		</Container>
	);
}
