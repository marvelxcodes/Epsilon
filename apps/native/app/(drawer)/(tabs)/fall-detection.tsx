import React from "react";
import { View, Text } from "react-native";
import { Container } from "@/components/container";
import { Ionicons } from "@expo/vector-icons";

export default function FallDetection() {
	return (
		<Container>
			<View className="flex-1 items-center justify-center px-6">
				<Ionicons name="fitness" size={80} color="hsl(215.4 16.3% 46.9%)" />
				<Text className="text-2xl font-bold text-foreground mt-6 text-center">
					Fall Detection
				</Text>
				<Text className="text-muted-foreground text-center mt-4 max-w-md">
					This feature is coming soon. It will automatically detect falls and
					alert your emergency contacts.
				</Text>
			</View>
		</Container>
	);
}
