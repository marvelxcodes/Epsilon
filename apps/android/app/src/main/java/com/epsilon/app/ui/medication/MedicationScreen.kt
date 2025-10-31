package com.epsilon.app.ui.medication

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.epsilon.app.data.api.MedicationApiClient
import com.epsilon.app.data.model.CreateMedicationRequest
import com.epsilon.app.data.model.Medication
import com.epsilon.app.data.model.UpdateMedicationRequest
import com.epsilon.app.data.session.SessionManager
import com.epsilon.app.utils.AlarmScheduler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(
    sessionManager: SessionManager,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var medications by remember { mutableStateOf<List<Medication>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMedication by remember { mutableStateOf<Medication?>(null) }
    
    val alarmScheduler = remember { AlarmScheduler(context) }
    
    // Load medications
    fun loadMedications() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val token = sessionManager.authToken.first() ?: ""
                val apiClient = MedicationApiClient(token)
                val result = apiClient.getAllMedications(activeOnly = false)
                
                result.onSuccess { meds ->
                    medications = meds
                    isLoading = false
                }.onFailure { e ->
                    error = e.message ?: "Failed to load medications"
                    isLoading = false
                }
            } catch (e: Exception) {
                error = e.message ?: "Failed to load medications"
                isLoading = false
            }
        }
    }
    
    LaunchedEffect(Unit) {
        loadMedications()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Medications",
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
                    containerColor = Color(0xFFD4F1D4),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Add") },
                text = { Text("Add Medication") }
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
                    EmptyMedicationState(
                        onAddClick = { showAddDialog = true },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(medications, key = { it.id }) { medication ->
                            MedicationCard(
                                medication = medication,
                                onEdit = { editingMedication = it },
                                onDelete = { med ->
                                    scope.launch {
                                        try {
                                            val token = sessionManager.authToken.first() ?: ""
                                            val apiClient = MedicationApiClient(token)
                                            apiClient.deleteMedication(med.id).onSuccess {
                                                // Cancel alarms for this medication
                                                alarmScheduler.cancelAlarmsForMedication(med.id)
                                                loadMedications()
                                            }
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                },
                                onToggleActive = { med ->
                                    scope.launch {
                                        try {
                                            val token = sessionManager.authToken.first() ?: ""
                                            val apiClient = MedicationApiClient(token)
                                            val newStatus = med.isActive != "true"
                                            apiClient.updateMedication(
                                                med.id,
                                                UpdateMedicationRequest(isActive = newStatus)
                                            ).onSuccess {
                                                if (newStatus) {
                                                    // Schedule alarms
                                                    alarmScheduler.scheduleMedicationAlarms(it)
                                                } else {
                                                    // Cancel alarms
                                                    alarmScheduler.cancelAlarmsForMedication(it.id)
                                                }
                                                loadMedications()
                                            }
                                        } catch (e: Exception) {
                                            error = e.message
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Add/Edit Dialog
    if (showAddDialog || editingMedication != null) {
        MedicationDialog(
            medication = editingMedication,
            onDismiss = {
                showAddDialog = false
                editingMedication = null
            },
            onSave = { request, isEdit, medicationId ->
                scope.launch {
                    try {
                        val token = sessionManager.authToken.first() ?: ""
                        val apiClient = MedicationApiClient(token)
                        
                        if (isEdit && medicationId != null) {
                            val updateRequest = UpdateMedicationRequest(
                                name = request.name,
                                dosage = request.dosage,
                                frequency = request.frequency,
                                time = request.time,
                                startDate = request.startDate,
                                endDate = request.endDate,
                                notes = request.notes,
                                reminderEnabled = request.reminderEnabled
                            )
                            apiClient.updateMedication(medicationId, updateRequest).onSuccess { med ->
                                if (med.isActive == "true" && med.reminderEnabled == "true") {
                                    alarmScheduler.scheduleMedicationAlarms(med)
                                }
                                loadMedications()
                                editingMedication = null
                            }
                        } else {
                            apiClient.createMedication(request).onSuccess { med ->
                                if (med.isActive == "true" && med.reminderEnabled == "true") {
                                    alarmScheduler.scheduleMedicationAlarms(med)
                                }
                                loadMedications()
                                showAddDialog = false
                            }
                        }
                    } catch (e: Exception) {
                        error = e.message
                    }
                }
            }
        )
    }
}

@Composable
fun MedicationCard(
    medication: Medication,
    onEdit: (Medication) -> Unit,
    onDelete: (Medication) -> Unit,
    onToggleActive: (Medication) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val isActive = medication.isActive == "true"
    val reminderEnabled = medication.reminderEnabled == "true"
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFD4F1D4) else Color(0xFFF5F5F5)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header with icon and switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (isActive) Color(0xFF4CAF50) else Color.Gray,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.MedicalServices,
                            contentDescription = "Medication",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Column {
                        Text(
                            text = medication.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = medication.dosage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black.copy(alpha = 0.6f)
                        )
                    }
                }
                
                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggleActive(medication) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4CAF50),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color.Gray
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Info chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ModernInfoChip(
                    icon = Icons.Outlined.Schedule,
                    text = medication.time.replace(",", ", "),
                    backgroundColor = Color.White
                )
                ModernInfoChip(
                    icon = Icons.Outlined.Repeat,
                    text = medication.frequency.capitalize(),
                    backgroundColor = Color.White
                )
            }
            
            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(8.dp))
                ModernInfoChip(
                    icon = Icons.Outlined.Notifications,
                    text = "Reminders ON",
                    backgroundColor = Color(0xFFFFF9C4)
                )
            }
            
            if (medication.notes?.isNotEmpty() == true) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White
                ) {
                    Text(
                        text = medication.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onEdit(medication) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit")
                }
                
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFE53935)
                    )
                ) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }
            }
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Medication?") },
            text = { Text("Are you sure you want to delete ${medication.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(medication)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ModernInfoChip(
    icon: ImageVector,
    text: String,
    backgroundColor: Color = Color.White
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        modifier = Modifier.height(40.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.Black.copy(alpha = 0.6f)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Black.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun EmptyMedicationState(
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = Color(0xFFD4F1D4),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.MedicalServices,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color(0xFF4CAF50)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Medications Yet",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start managing your medications by\nadding your first one",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black.copy(alpha = 0.6f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White
            ),
            modifier = Modifier.height(56.dp)
        ) {
            Icon(Icons.Default.Add, "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Medication", style = MaterialTheme.typography.titleMedium)
        }
    }
}
