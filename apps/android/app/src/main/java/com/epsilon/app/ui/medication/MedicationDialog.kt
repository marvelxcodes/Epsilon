package com.epsilon.app.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.epsilon.app.data.model.CreateMedicationRequest
import com.epsilon.app.data.model.Medication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationDialog(
    medication: Medication? = null,
    onDismiss: () -> Unit,
    onSave: (CreateMedicationRequest, Boolean, String?) -> Unit
) {
    var name by remember { mutableStateOf(medication?.name ?: "") }
    var dosage by remember { mutableStateOf(medication?.dosage ?: "") }
    var frequency by remember { mutableStateOf(medication?.frequency ?: "daily") }
    var time by remember { mutableStateOf(medication?.time ?: "08:00") }
    var notes by remember { mutableStateOf(medication?.notes ?: "") }
    var reminderEnabled by remember { mutableStateOf(medication?.reminderEnabled != "false") }
    var startDate by remember { mutableStateOf(medication?.startDate ?: getCurrentDate()) }
    var endDate by remember { mutableStateOf(medication?.endDate ?: "") }
    
    val isEdit = medication != null
    val scrollState = rememberScrollState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(if (isEdit) "Edit Medication" else "Add Medication") 
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Medication Name *") },
                    placeholder = { Text("e.g., Aspirin") },
                    leadingIcon = {
                        Icon(Icons.Outlined.MedicalServices, "Name")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Dosage
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage *") },
                    placeholder = { Text("e.g., 500mg") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Science, "Dosage")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Frequency
                var expandedFrequency by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedFrequency,
                    onExpandedChange = { expandedFrequency = it }
                ) {
                    OutlinedTextField(
                        value = frequency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frequency *") },
                        leadingIcon = {
                            Icon(Icons.Outlined.Repeat, "Frequency")
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expandedFrequency) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedFrequency,
                        onDismissRequest = { expandedFrequency = false }
                    ) {
                        listOf("daily", "twice daily", "three times daily", "weekly", "as needed").forEach { freq ->
                            DropdownMenuItem(
                                text = { Text(freq) },
                                onClick = {
                                    frequency = freq
                                    expandedFrequency = false
                                }
                            )
                        }
                    }
                }
                
                // Time(s)
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Time(s) *") },
                    placeholder = { Text("08:00 or 08:00,14:00,20:00") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Schedule, "Time")
                    },
                    supportingText = {
                        Text("Use 24-hour format. Separate multiple times with commas")
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Start Date
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date *") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = {
                        Icon(Icons.Outlined.CalendarToday, "Start Date")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // End Date (optional)
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("End Date (Optional)") },
                    placeholder = { Text("YYYY-MM-DD") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Event, "End Date")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("Additional instructions...") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Notes, "Notes")
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
                
                // Reminder Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Notifications,
                            "Reminder",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text("Enable Reminders")
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotEmpty() && dosage.isNotEmpty() && 
                        frequency.isNotEmpty() && time.isNotEmpty() && 
                        startDate.isNotEmpty()) {
                        
                        val request = CreateMedicationRequest(
                            name = name,
                            dosage = dosage,
                            frequency = frequency,
                            time = time,
                            startDate = startDate,
                            endDate = if (endDate.isNotEmpty()) endDate else null,
                            notes = if (notes.isNotEmpty()) notes else null,
                            reminderEnabled = reminderEnabled
                        )
                        onSave(request, isEdit, medication?.id)
                        onDismiss()
                    }
                },
                enabled = name.isNotEmpty() && dosage.isNotEmpty() && 
                         frequency.isNotEmpty() && time.isNotEmpty() && 
                         startDate.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}
