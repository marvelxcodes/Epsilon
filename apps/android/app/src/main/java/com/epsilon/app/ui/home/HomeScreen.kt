package com.epsilon.app.ui.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.data.emergency.EmergencyContactManager
import com.epsilon.app.data.emergency.EmergencyCallManager
import com.epsilon.app.ui.auth.AuthViewModel
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AuthViewModel,
    sessionManager: SessionManager,
    onSignOut: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToBluetooth: () -> Unit = {},
    onNavigateToEmergencyContact: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToMedication: () -> Unit = {},
    onNavigateToReminder: () -> Unit = {}
) {
    var userName by remember { mutableStateOf("User") }
    val context = LocalContext.current
    val emergencyContactManager = remember { EmergencyContactManager(context) }
    val emergencyCallManager = remember { EmergencyCallManager(context) }
    
    LaunchedEffect(Unit) {
        try {
            userName = sessionManager.userName.first() ?: "User"
        } catch (e: Exception) {
            android.util.Log.e("HomeScreen", "Error loading user name", e)
            userName = "User"
        }
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Greeting Header
            item {
                GreetingHeader(
                    userName = userName,
                    onProfileClick = onNavigateToProfile
                )
            }
            
            // Quick Actions Grid
            item {
                QuickActionsGrid(
                    onAddDeviceClick = onNavigateToBluetooth,
                    onEditClick = onNavigateToProfile,
                    onMedicationClick = onNavigateToMedication,
                    onReminderClick = onNavigateToReminder
                )
            }
            
            // Test Emergency Call Button
            item {
                TestEmergencyCallButton(
                    emergencyContactManager = emergencyContactManager,
                    emergencyCallManager = emergencyCallManager
                )
            }
            
            // Paired Devices Section
            item {
                Text(
                    text = "Paired Devices",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Paired Devices List (ESP32 Wearables)
            item {
                PairedDevicesSection(
                    onDeviceClick = { deviceId ->
                        // Navigate to device details/config
                        onNavigateToSetup()
                    }
                )
            }
            
            // Settings Section
            item {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            // Settings Cards 
            item {
                SettingsSection(
                    onDeviceSetupClick = onNavigateToSetup,
                    onEmergencyClick = onNavigateToEmergencyContact
                )
            }
        }
    }
}

@Composable
fun GreetingHeader(
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp)
        ,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hello ${userName.split(" ").first()},",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "What are you looking for?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        IconButton(
            onClick = onProfileClick,
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Outlined.AccountCircle,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun QuickActionsGrid(
    onAddDeviceClick: () -> Unit,
    onEditClick: () -> Unit,
    onMedicationClick: () -> Unit,
    onReminderClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernQuickActionCard(
                title = "Add Device",
                backgroundColor = Color(0xFFD1E7F8),
                icon = Icons.Outlined.BluetoothSearching,
                modifier = Modifier.weight(1f),
                onClick = onAddDeviceClick
            )
            ModernQuickActionCard(
                title = "Edit Profile",
                backgroundColor = Color(0xFFFFF5E1),
                icon = Icons.Outlined.Edit,
                modifier = Modifier.weight(1f),
                onClick = onEditClick
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ModernQuickActionCard(
                title = "Medication",
                backgroundColor = Color(0xFFD4F1D4),
                icon = Icons.Outlined.MedicalServices,
                modifier = Modifier.weight(1f),
                onClick = onMedicationClick
            )
            ModernQuickActionCard(
                title = "Reminder",
                backgroundColor = Color(0xFFFFF9C4),
                icon = Icons.Outlined.Notifications,
                modifier = Modifier.weight(1f),
                onClick = onReminderClick
            )
        }
    }
}

@Composable
fun ModernQuickActionCard(
    title: String,
    backgroundColor: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = Color.Black,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun PairedDevicesSection(
    onDeviceClick: (String) -> Unit
) {
    // TODO: Fetch actual devices from API
    // For now, showing sample data
    val sampleDevices = remember {
        listOf(
            DeviceInfo(
                id = "esp32-001",
                name = "ESP32 Wearable",
                status = "Connected",
                battery = 85
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (sampleDevices.isEmpty()) {
            // No devices paired yet
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 0.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Watch,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No devices paired",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap 'Add Device' to connect your ESP32 wearable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            // Show paired devices
            sampleDevices.forEach { device ->
                DeviceCard(
                    device = device,
                    onClick = { onDeviceClick(device.id) }
                )
            }
        }
    }
}

@Composable
fun DeviceCard(
    device: DeviceInfo,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E9)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Watch,
                    contentDescription = device.name,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = device.status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        text = "•",
                        color = Color.Black.copy(alpha = 0.4f)
                    )
                    Icon(
                        imageVector = Icons.Outlined.BatteryChargingFull,
                        contentDescription = "Battery",
                        modifier = Modifier.size(16.dp),
                        tint = Color.Black.copy(alpha = 0.6f)
                    )
                    Text(
                        text = "${device.battery}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun SettingsSection(
    onDeviceSetupClick: () -> Unit,
    onEmergencyClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MainActionCard(
            title = "Fall Detection Setup",
            subtitle = "Configure sensitivity and alerts",
            icon = Icons.Outlined.Settings,
            backgroundColor = Color(0xFFE0F2F1),
            onClick = onDeviceSetupClick
        )
        MainActionCard(
            title = "Emergency Contacts",
            subtitle = "Manage your emergency contacts",
            icon = Icons.Outlined.Phone,
            backgroundColor = Color(0xFFFFEBEE),
            onClick = onEmergencyClick
        )
    }
}

// Data class for device info
data class DeviceInfo(
    val id: String,
    val name: String,
    val status: String,
    val battery: Int
)

@Composable
fun MainActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        color = Color.Black,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black.copy(alpha = 0.6f)
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.Black.copy(alpha = 0.4f)
            )
        }
    }
}

@Preview
@Composable
fun TestEmergencyCallButton(
    emergencyContactManager: EmergencyContactManager,
    emergencyCallManager: EmergencyCallManager
) {
    var showMessage by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            onClick = {
                val emergencyNumber = emergencyContactManager.getEmergencyContact()
                if (emergencyNumber.isNullOrBlank()) {
                    message = "No emergency contact set. Please configure one first."
                    showMessage = true
                } else if (!emergencyCallManager.hasCallPermission()) {
                    message = "Call permission not granted. Please grant permission in settings."
                    showMessage = true
                } else {
                    val success = emergencyCallManager.placeEmergencyCall(emergencyNumber)
                    message = if (success) {
                        "Emergency call initiated to $emergencyNumber"
                    } else {
                        "Failed to place emergency call"
                    }
                    showMessage = true
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFCDD2) // Light red for emergency
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color(0xFFD32F2F), // Darker red
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Phone,
                        contentDescription = "Test Emergency Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Emergency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = "Tap to perform an emergency call",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.6f)
                    )
                }
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F)
                )
            }
        }
        
        if (showMessage) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { showMessage = false }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close"
                        )
                    }
                }
            }
        }
    }
}

// Helper functions (kept for backward compatibility but not displayed)
private fun formatDate(isoDate: String): String {
    if (isoDate.isEmpty()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(isoDate)
        val outputFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        isoDate
    }
}
