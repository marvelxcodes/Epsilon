import React, { useEffect, useState } from "react";
import {
	View,
	Text,
	TextInput,
	TouchableOpacity,
	Alert,
	ScrollView,
} from "react-native";
import { Container } from "@/components/container";
import AsyncStorage from "@react-native-async-storage/async-storage";
import { Ionicons } from "@expo/vector-icons";

interface Contact {
	id: string;
	name: string;
	phone: string;
}

export default function Settings() {
	const [frequentContacts, setFrequentContacts] = useState<Contact[]>([]);
	const [sosContacts, setSosContacts] = useState<Contact[]>([]);
	const [newName, setNewName] = useState("");
	const [newPhone, setNewPhone] = useState("");
	const [addingType, setAddingType] = useState<"frequent" | "sos" | null>(null);

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

	const saveFrequentContacts = async (contacts: Contact[]) => {
		try {
			await AsyncStorage.setItem("frequentContacts", JSON.stringify(contacts));
			setFrequentContacts(contacts);
		} catch (error) {
			Alert.alert("Error", "Failed to save contacts");
		}
	};

	const saveSosContacts = async (contacts: Contact[]) => {
		try {
			await AsyncStorage.setItem("sosContacts", JSON.stringify(contacts));
			setSosContacts(contacts);
		} catch (error) {
			Alert.alert("Error", "Failed to save contacts");
		}
	};

	const addContact = () => {
		if (!newName.trim() || !newPhone.trim()) {
			Alert.alert("Error", "Please fill in both name and phone number");
			return;
		}

		const contact: Contact = {
			id: Date.now().toString(),
			name: newName.trim(),
			phone: newPhone.trim(),
		};

		if (addingType === "frequent") {
			saveFrequentContacts([...frequentContacts, contact]);
		} else if (addingType === "sos") {
			saveSosContacts([...sosContacts, contact]);
		}

		setNewName("");
		setNewPhone("");
		setAddingType(null);
	};

	const deleteFrequentContact = (id: string) => {
		Alert.alert("Delete Contact", "Are you sure you want to delete this contact?", [
			{ text: "Cancel", style: "cancel" },
			{
				text: "Delete",
				style: "destructive",
				onPress: () => {
					saveFrequentContacts(frequentContacts.filter((c) => c.id !== id));
				},
			},
		]);
	};

	const deleteSosContact = (id: string) => {
		Alert.alert("Delete SOS Contact", "Are you sure you want to delete this SOS contact?", [
			{ text: "Cancel", style: "cancel" },
			{
				text: "Delete",
				style: "destructive",
				onPress: () => {
					saveSosContacts(sosContacts.filter((c) => c.id !== id));
				},
			},
		]);
	};

	return (
		<Container>
			<ScrollView className="flex-1">
				<View className="px-6 py-8">
					<Text className="text-3xl font-bold text-foreground mb-6">
						Settings
					</Text>

					{/* Quick Contacts Section */}
					<View className="mb-8">
						<Text className="text-xl font-bold text-foreground mb-4">
							Quick Contacts
						</Text>
						<Text className="text-muted-foreground mb-4 text-sm">
							Add contacts for quick access from the home screen
						</Text>

						{frequentContacts.map((contact) => (
							<View
								key={contact.id}
								className="bg-card border border-border rounded-lg p-4 mb-3 flex-row items-center justify-between"
							>
								<View>
									<Text className="text-foreground font-medium text-lg">
										{contact.name}
									</Text>
									<Text className="text-muted-foreground text-sm">
										{contact.phone}
									</Text>
								</View>
								<TouchableOpacity onPress={() => deleteFrequentContact(contact.id)}>
									<Ionicons name="trash" size={24} color="hsl(0 84.2% 60.2%)" />
								</TouchableOpacity>
							</View>
						))}

						{addingType === "frequent" ? (
							<View className="bg-card border border-border rounded-lg p-4 mb-3">
								<TextInput
									className="bg-background border border-border rounded-md p-3 mb-3 text-foreground"
									placeholder="Name"
									placeholderTextColor="#999"
									value={newName}
									onChangeText={setNewName}
								/>
								<TextInput
									className="bg-background border border-border rounded-md p-3 mb-3 text-foreground"
									placeholder="Phone Number"
									placeholderTextColor="#999"
									keyboardType="phone-pad"
									value={newPhone}
									onChangeText={setNewPhone}
								/>
								<View className="flex-row gap-2">
									<TouchableOpacity
										className="flex-1 bg-primary py-3 rounded-md"
										onPress={addContact}
									>
										<Text className="text-white font-medium text-center">Add</Text>
									</TouchableOpacity>
									<TouchableOpacity
										className="flex-1 bg-muted py-3 rounded-md"
										onPress={() => {
											setAddingType(null);
											setNewName("");
											setNewPhone("");
										}}
									>
										<Text className="text-foreground font-medium text-center">Cancel</Text>
									</TouchableOpacity>
								</View>
							</View>
						) : (
							<TouchableOpacity
								className="bg-primary/10 border-2 border-dashed border-primary py-4 rounded-lg"
								onPress={() => setAddingType("frequent")}
							>
								<Text className="text-primary font-medium text-center">
									+ Add Quick Contact
								</Text>
							</TouchableOpacity>
						)}
					</View>

					{/* SOS Contacts Section */}
					<View className="mb-8">
						<Text className="text-xl font-bold text-foreground mb-4">
							SOS Contacts
						</Text>
						<Text className="text-muted-foreground mb-4 text-sm">
							These contacts will receive emergency alerts when you press the SOS button
						</Text>

						{sosContacts.map((contact) => (
							<View
								key={contact.id}
								className="bg-red-50 dark:bg-red-950 border border-red-200 dark:border-red-900 rounded-lg p-4 mb-3 flex-row items-center justify-between"
							>
								<View>
									<Text className="text-foreground font-medium text-lg">
										{contact.name}
									</Text>
									<Text className="text-muted-foreground text-sm">
										{contact.phone}
									</Text>
								</View>
								<TouchableOpacity onPress={() => deleteSosContact(contact.id)}>
									<Ionicons name="trash" size={24} color="hsl(0 84.2% 60.2%)" />
								</TouchableOpacity>
							</View>
						))}

						{addingType === "sos" ? (
							<View className="bg-card border border-border rounded-lg p-4 mb-3">
								<TextInput
									className="bg-background border border-border rounded-md p-3 mb-3 text-foreground"
									placeholder="Name"
									placeholderTextColor="#999"
									value={newName}
									onChangeText={setNewName}
								/>
								<TextInput
									className="bg-background border border-border rounded-md p-3 mb-3 text-foreground"
									placeholder="Phone Number"
									placeholderTextColor="#999"
									keyboardType="phone-pad"
									value={newPhone}
									onChangeText={setNewPhone}
								/>
								<View className="flex-row gap-2">
									<TouchableOpacity
										className="flex-1 bg-red-600 py-3 rounded-md"
										onPress={addContact}
									>
										<Text className="text-white font-medium text-center">Add</Text>
									</TouchableOpacity>
									<TouchableOpacity
										className="flex-1 bg-muted py-3 rounded-md"
										onPress={() => {
											setAddingType(null);
											setNewName("");
											setNewPhone("");
										}}
									>
										<Text className="text-foreground font-medium text-center">Cancel</Text>
									</TouchableOpacity>
								</View>
							</View>
						) : (
							<TouchableOpacity
								className="bg-red-100 dark:bg-red-950 border-2 border-dashed border-red-600 py-4 rounded-lg"
								onPress={() => setAddingType("sos")}
							>
								<Text className="text-red-600 dark:text-red-400 font-medium text-center">
									+ Add SOS Contact
								</Text>
							</TouchableOpacity>
						)}
					</View>
				</View>
			</ScrollView>
		</Container>
	);
}
