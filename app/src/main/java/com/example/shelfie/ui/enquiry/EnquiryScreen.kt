package com.example.shelfie.ui.enquiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shelfie.ui.components.BackButton
import com.example.shelfie.core.common.AppError
import com.example.shelfie.domain.validation.EnquiryValidator
import com.example.shelfie.domain.validation.FieldError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnquiryScreen(onBack: () -> Unit, viewModel: EnquiryViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val enabled = !state.isSubmitting

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Enquire") },
                navigationIcon = {
                    BackButton(onBack)
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(state.productTitle, style = MaterialTheme.typography.titleMedium)
            FormField("Name", state.form.name, viewModel::onNameChange, state.errors.name, EnquiryValidator.MAX_NAME, enabled)
            FormField("Email", state.form.email, viewModel::onEmailChange, state.errors.email, EnquiryValidator.MAX_EMAIL, enabled, keyboardType = KeyboardType.Email)
            FormField("Phone (optional)", state.form.phone, viewModel::onPhoneChange, state.errors.phone, EnquiryValidator.MAX_PHONE, enabled, keyboardType = KeyboardType.Phone)
            FormField(
                "Message", state.form.message, viewModel::onMessageChange, state.errors.message,
                EnquiryValidator.MAX_MESSAGE, enabled, singleLine = false, modifier = Modifier.height(160.dp),
            )
            state.submitError?.let {
                Text(
                    if (it == AppError.NoNetwork) "No internet connection. Please try again."
                    else "Couldn't send your enquiry. Please try again.",
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Button(onClick = viewModel::submit, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
                if (state.isSubmitting) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Submit")
            }
        }
    }

    if (state.submitted) {
        AlertDialog(
            onDismissRequest = onBack,
            title = { Text("Thank you!") },
            text = { Text("Your enquiry has been sent. Our sales team will be in touch soon.") },
            confirmButton = { TextButton(onClick = onBack) { Text("OK") } },
        )
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    error: FieldError?,
    maxLength: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { Text(errorText(it, maxLength)) } },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
    )
}

private fun errorText(error: FieldError, maxLength: Int) = when (error) {
    FieldError.Required -> "This field is required"
    FieldError.InvalidEmail -> "Enter a valid email address"
    FieldError.InvalidPhone -> "Enter a valid phone number"
    FieldError.TooLong -> "Maximum $maxLength characters"
}
