package com.example.myapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapp.R
import com.example.myapp.service.NfcSyncManager
import kotlinx.coroutines.delay

@Composable
fun NfcSharingDialog(
    onDismiss: () -> Unit
) {
    var isSuccess by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    
    LaunchedEffect(Unit) {
        NfcSyncManager.syncSuccess.collect {
            isSuccess = true
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(2000)
            onDismiss()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.sync_nfc_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSuccess) {
                        NfcSuccessAnimation()
                    } else {
                        NfcPulseAnimation()
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = if (isSuccess) 
                        stringResource(R.string.sync_success) 
                    else 
                        stringResource(R.string.sync_nfc_sharing_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.close_button))
                }
            }
        }
    }
}
