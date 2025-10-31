package com.epsilon.app.ui.emergency

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.epsilon.app.data.emergency.EmergencyContactManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyContactScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val emergencyContactManager = remember { EmergencyContactManager(context) }
    
    var phoneNumber by remember { 
        mutableStateOf(emergencyContactManager.getEmergencyContact() ?: "") 
    }
    var contactName by remember { 
        mutableStateOf(emergencyContactManager.getEmergencyContactName() ?: "") 
    }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Permission states
    var hasCallPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var hasContactsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCallPermission = permissions[Manifest.permission.CALL_PHONE] == true
        hasContactsPermission = permissions[Manifest.permission.READ_CONTACTS] == true
    }
    
    // Contact picker launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val cursor: Cursor? = context.contentResolver.query(
                it,
                null,
                null,
                null,
                null
            )
            cursor?.use { c ->
                if (c.moveToFirst()) {
                    val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                    
                    if (nameIndex >= 0) {
                        contactName = c.getString(nameIndex)
                    }
                    
                    if (idIndex >= 0) {
                        val contactId = c.getString(idIndex)
                        
                        // Get phone number
                        val phoneCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                            arrayOf(contactId),
                            null
                        )
                        
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val phoneIndex = pc.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                                if (phoneIndex >= 0) {
                                    phoneNumber = pc.getString(phoneIndex)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Fetch from database on first load
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val (phone, name) = emergencyContactManager.fetchFromDatabase()
            if (!phone.isNullOrBlank()) {
                phoneNumber = phone
                contactName = name ?: ""
                android.util.Log.d("EmergencyContactScreen", "Loaded contact: $phone, $name")
            } else {
                android.util.Log.d("EmergencyContactScreen", "No contact found in database")
            }
        } catch (e: Exception) {
            android.util.Log.e("EmergencyContactScreen", "Error loading contact", e)
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contact") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Column {
                        Text(
                            text = "Emergency Fall Detection",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "This contact will be called automatically if a fall is detected by your wearable device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            // Contact name field
            OutlinedTextField(
                value = contactName,
                onValueChange = { 
                    contactName = it
                    errorMessage = null
                },
                label = { Text("Contact Name (Optional)") },
                placeholder = { Text("e.g., John Doe") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Phone number field with pick contact button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { 
                        phoneNumber = it
                        errorMessage = null
                    },
                    label = { Text("Emergency Phone Number *") },
                    placeholder = { Text("e.g., +1234567890") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = errorMessage != null
                )
                
                // Pick contact button
                FilledTonalButton(
                    onClick = {
                        if (hasContactsPermission) {
                            contactPickerLauncher.launch(null)
                        } else {
                            val permissions = mutableListOf(
                                Manifest.permission.READ_CONTACTS
                            )
                            if (!hasCallPermission) {
                                permissions.add(Manifest.permission.CALL_PHONE)
                            }
                            permissionLauncher.launch(permissions.toTypedArray())
                        }
                    },
                    modifier = Modifier.height(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Contacts,
                        contentDescription = "Pick Contact"
                    )
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            // Permission warning
            if (!hasCallPermission || !hasContactsPermission) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Permission Required",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Text(
                            text = buildString {
                                if (!hasCallPermission) {
                                    append("Call phone permission is required to automatically place emergency calls.")
                                }
                                if (!hasContactsPermission) {
                                    if (!hasCallPermission) append(" ")
                                    append("Contacts permission is required to pick contacts from your phone.")
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Button(
                            onClick = {
                                val permissions = mutableListOf<String>()
                                if (!hasCallPermission) {
                                    permissions.add(Manifest.permission.CALL_PHONE)
                                }
                                if (!hasContactsPermission) {
                                    permissions.add(Manifest.permission.READ_CONTACTS)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                                }
                                permissionLauncher.launch(permissions.toTypedArray())
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Success message
            if (showSuccessMessage) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Emergency contact saved successfully!",
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
            
            // Save button
            Button(
                onClick = {
                    when {
                        phoneNumber.isBlank() -> {
                            errorMessage = "Please enter a phone number"
                        }
                        !hasCallPermission -> {
                            errorMessage = "Please grant call phone permission"
                        }
                        else -> {
                            emergencyContactManager.saveEmergencyContact(
                                phoneNumber = phoneNumber.trim(),
                                contactName = contactName.trim()
                            )
                            showSuccessMessage = true
                            errorMessage = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = phoneNumber.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Emergency Contact")
            }
            
            // Clear button (if contact exists)
            if (emergencyContactManager.hasEmergencyContact()) {
                OutlinedButton(
                    onClick = {
                        emergencyContactManager.clearEmergencyContact()
                        phoneNumber = ""
                        contactName = ""
                        showSuccessMessage = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Emergency Contact")
                }
            }
            }
        }
    }
}
