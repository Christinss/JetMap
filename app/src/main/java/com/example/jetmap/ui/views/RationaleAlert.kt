package com.example.jetmap.ui.views

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.jetmap.R

@Composable
fun PermissionRationaleDialog(
    rationaleState: RationaleState
) {
    AlertDialog(
        onDismissRequest = {
            rationaleState.onRationaleReply(false)
        },
        title = {
            Text(text = rationaleState.title)
        },
        text = {
            Text(text = rationaleState.rationale)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    rationaleState.onRationaleReply(true)
                }
            ) {
                Text(text = stringResource(R.string.btn_continue))
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    rationaleState.onRationaleReply(false)
                }
            ) {
                Text(text = stringResource(R.string.btn_cancel))
            }
        }
    )
}

data class RationaleState(
    val title: String,
    val rationale: String,
    val onRationaleReply: (proceed: Boolean) -> Unit
)
