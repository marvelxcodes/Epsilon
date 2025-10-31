package com.epsilon.app.ui.reminder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.epsilon.app.data.api.MedicationApiClient
import com.epsilon.app.data.model.Medication
import com.epsilon.app.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ReminderItem(
    val medication: Medication,
    val time: String,
    val isPast: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Load medications
    fun loadMedications() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val token = sessionManager.authToken.first() ?: ""
                val apiClient = MedicationApiClient(token)
                val result = apiClient.getAllMedications(activeOnly = true)
                
                result.onSuccess { meds ->
                    medications = meds.filter { it.reminderEnabled == "true" }
                    isLoading = false
                }.onFailure { e ->
                    error = e.message ?: "Failed to load reminders"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load reminders"
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadMedications()
    }
    
    // Generate reminders for selected date
    val reminders = remember(medications, selectedDate) {
        generateReminders(medications, selectedDate)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Reminders",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFFFF9C4),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { loadMedications() }) {
                            Text("Retry")
                        }
                    }
                }
                medications.isEmpty() -> {
                    EmptyReminderState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Selector
                        item {
                            DateSelectorCard(
                                selectedDate = selectedDate,
                                onDateChange = { selectedDate = it }
                            )
                        }
                        
                        // Summary
                        item {
                            ReminderSummaryCard(reminders = reminders)
                        }
                        
                        // Reminders List
                        if (reminders.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Outlined.EventAvailable,
                                            null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            "No reminders for this day",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                            }
                        } else {
                            items(reminders, key = { "${it.medication.id}-${it.time}" }) { reminder ->
                                ReminderCard(reminder = reminder)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DateSelectorCard(
    selectedDate: Calendar,
    onDateChange: (Calendar) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.DAY_OF_MONTH, -1)
                    onDateChange(newDate)
                }
            ) {
                Icon(Icons.Default.ChevronLeft, "Previous Day")
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = SimpleDateFormat("EEEE", Locale.getDefault()).format(selectedDate.time),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(selectedDate.time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = {
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.DAY_OF_MONTH, 1)
                    onDateChange(newDate)
                }
            ) {
                Icon(Icons.Default.ChevronRight, "Next Day")
            }
        }
    }
}

@Composable
fun ReminderSummaryCard(reminders: List<ReminderItem>) {
    val totalReminders = reminders.size
    val pastReminders = reminders.count { it.isPast }
    val upcomingReminders = totalReminders - pastReminders
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryItem(
                icon = Icons.Outlined.Schedule,
                count = totalReminders,
                label = "Total",
                color = MaterialTheme.colorScheme.primary
            )
            SummaryItem(
                icon = Icons.Outlined.CheckCircle,
                count = pastReminders,
                label = "Past",
                color = MaterialTheme.colorScheme.tertiary
            )
            SummaryItem(
                icon = Icons.Outlined.Notifications,
                count = upcomingReminders,
                label = "Upcoming",
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ReminderCard(reminder: ReminderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reminder.isPast) 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Time indicator
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        color = if (reminder.isPast) 
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                        else 
                            MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = reminder.time,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (reminder.isPast)
                        MaterialTheme.colorScheme.tertiary
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            // Medication info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = reminder.medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = reminder.medication.dosage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = reminder.medication.frequency,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Status indicator
            Icon(
                imageVector = if (reminder.isPast) 
                    Icons.Outlined.CheckCircle 
                else 
                    Icons.Outlined.Notifications,
                contentDescription = if (reminder.isPast) "Past" else "Upcoming",
                tint = if (reminder.isPast) 
                    MaterialTheme.colorScheme.tertiary
                else 
                    MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun EmptyReminderState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Reminders",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Add medications with reminders enabled",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun generateReminders(medications: List<Medication>, date: Calendar): List<ReminderItem> {
    val reminders = mutableListOf<ReminderItem>()
    val currentTime = Calendar.getInstance()
    val isToday = isSameDay(date, currentTime)
    
    medications.forEach { medication ->
        val times = medication.time.split(",").map { it.trim() }
        times.forEach { time ->
            val timeCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, date.get(Calendar.YEAR))
                set(Calendar.MONTH, date.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, date.get(Calendar.DAY_OF_MONTH))
                
                val timeParts = time.split(":")
                if (timeParts.size == 2) {
                    set(Calendar.HOUR_OF_DAY, timeParts[0].toIntOrNull() ?: 0)
                    set(Calendar.MINUTE, timeParts[1].toIntOrNull() ?: 0)
                    set(Calendar.SECOND, 0)
                }
            }
            
            val isPast = isToday && timeCalendar.before(currentTime)
            
            reminders.add(
                ReminderItem(
                    medication = medication,
                    time = time,
                    isPast = isPast
                )
            )
        }
    }
    
    return reminders.sortedBy { it.time }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
