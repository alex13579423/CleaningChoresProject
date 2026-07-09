package com.example.myapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myapp.R
import com.example.myapp.qr.data.manager.QrCryptoManager
import com.example.myapp.qr.presentation.components.RunningQR
import com.example.myapp.qr.presentation.util.QrGenerator

@Composable
fun QrSharingDialog(
    data: String,
    onDismiss: () -> Unit,
    qrCryptoManager: QrCryptoManager,
    qrGenerator: QrGenerator
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.generate_qr_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Surface(
                    modifier = Modifier.size(240.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = Color.White,
                    tonalElevation = 4.dp
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        RunningQR(
                            rawJsonData = data,
                            qrCryptoManager = qrCryptoManager,
                            qrGenerator = qrGenerator,
                            qrColor = Color(0xFF1B5E20),
                            qrBackgroundColor = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(R.string.sync_nfc_sharing_subtitle),
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
