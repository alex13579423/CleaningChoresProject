package com.example.myapp.ui.components

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.tech.IsoDep
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NfcScannerDialog(
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }
    var isSuccess by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(nfcAdapter) {
        val callback = NfcAdapter.ReaderCallback { tag ->
            val isoDep = IsoDep.get(tag)
            isoDep?.use { 
                it.connect()
                val selectCommand = byteArrayOf(
                    0x00.toByte(), 0xA4.toByte(), 0x04.toByte(), 0x00.toByte(),
                    0x07.toByte(), 0xF0.toByte(), 0x01.toByte(), 0x02.toByte(),
                    0x03.toByte(), 0x04.toByte(), 0x05.toByte(), 0x06.toByte()
                )
                val response = it.transceive(selectCommand)
                if (response.size >= 2 && response[response.size - 2] == 0x90.toByte() && response[response.size - 1] == 0x00.toByte()) {
                    val json = String(response.copyOfRange(0, response.size - 2))
                    
                    scope.launch {
                        isSuccess = true
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        delay(2000)
                        onResult(json)
                    }
                }
            }
        }

        nfcAdapter?.enableReaderMode(
            context as Activity,
            callback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )

        onDispose {
            nfcAdapter?.disableReaderMode(context as Activity)
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
                    text = stringResource(R.string.scan_nfc_sync),
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
                        stringResource(R.string.sync_nfc_scanning_subtitle),
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
