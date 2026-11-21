package com.example.shelfie.ui.enquiry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Enquiry
import com.example.shelfie.domain.repository.EnquiryRepository
import com.example.shelfie.domain.validation.EnquiryErrors
import com.example.shelfie.domain.validation.EnquiryForm
import com.example.shelfie.domain.validation.EnquiryValidator
import com.example.shelfie.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EnquiryUiState(
    val productTitle: String = "",
    val form: EnquiryForm = EnquiryForm(),
    val errors: EnquiryErrors = EnquiryErrors(),
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val submitError: AppError? = null,
)

@HiltViewModel
class EnquiryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val enquiryRepository: EnquiryRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Route.Enquiry>()
    private val productId = route.productId
    private val productTitle = route.productTitle
    private val form = MutableStateFlow(EnquiryUiState(productTitle = productTitle))

    // Form state lives here, so it survives rotation.
    val state: StateFlow<EnquiryUiState> = form.asStateFlow()

    fun onNameChange(v: String) = edit({ copy(name = v) }, { copy(name = null) })
    fun onEmailChange(v: String) = edit({ copy(email = v) }, { copy(email = null) })
    fun onPhoneChange(v: String) = edit({ copy(phone = v) }, { copy(phone = null) })
    fun onMessageChange(v: String) = edit({ copy(message = v) }, { copy(message = null) })

    // Editing a field clears only its own error; everything is re-validated on submit.
    private fun edit(change: EnquiryForm.() -> EnquiryForm, clearError: EnquiryErrors.() -> EnquiryErrors) {
        form.update { it.copy(form = it.form.change(), errors = it.errors.clearError(), submitError = null) }
    }

    fun submit() {
        val current = form.value
        if (current.isSubmitting || current.submitted) return
        val errors = EnquiryValidator.validate(current.form)
        if (errors.hasErrors) {
            form.update { it.copy(errors = errors) }
            return
        }
        form.update { it.copy(errors = EnquiryErrors(), isSubmitting = true, submitError = null) }
        viewModelScope.launch {
            val f = current.form
            val result = enquiryRepository.submit(
                Enquiry(
                    productId = productId,
                    productTitle = productTitle,
                    name = f.name.trim(),
                    email = f.email.trim(),
                    phone = f.phone.trim().takeIf { it.isNotEmpty() },
                    message = f.message.trim(),
                )
            )
            form.update {
                when (result) {
                    is AppResult.Success -> it.copy(isSubmitting = false, submitted = true)
                    is AppResult.Failure -> it.copy(isSubmitting = false, submitError = result.error)
                }
            }
        }
    }
}
