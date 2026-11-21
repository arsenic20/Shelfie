package com.example.shelfie.ui.enquiry

import androidx.lifecycle.SavedStateHandle
import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Enquiry
import com.example.shelfie.domain.repository.EnquiryRepository
import com.example.shelfie.domain.validation.FieldError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EnquiryViewModelTest {

    private class FakeEnquiries(var result: AppResult<Unit> = AppResult.Success(Unit)) : EnquiryRepository {
        val sent = mutableListOf<Enquiry>()
        override suspend fun submit(enquiry: Enquiry): AppResult<Unit> {
            sent += enquiry
            return result
        }
    }

    @Before fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After fun tearDown() = Dispatchers.resetMain()

    private fun vm(repo: FakeEnquiries) =
        EnquiryViewModel(SavedStateHandle(mapOf("productId" to 7, "productTitle" to "Backpack")), repo)

    private fun EnquiryViewModel.fill() {
        onNameChange(" Ann ")
        onEmailChange("ann@example.com")
        onMessageChange("Hello")
    }

    @Test fun exposesProductTitle() = runTest {
        assertEquals("Backpack", vm(FakeEnquiries()).state.value.productTitle)
    }

    @Test fun validSubmitSendsTrimmedEnquiryAndSucceeds() = runTest {
        val repo = FakeEnquiries()
        val vm = vm(repo)
        vm.fill()
        vm.submit()
        assertTrue(vm.state.value.submitted)
        assertFalse(vm.state.value.isSubmitting)
        assertEquals(Enquiry(7, "Backpack", "Ann", "ann@example.com", null, "Hello"), repo.sent.single())
    }

    @Test fun invalidInputShowsErrorsAndDoesNotSubmit() = runTest {
        val repo = FakeEnquiries()
        val vm = vm(repo)
        vm.onEmailChange("nope")
        vm.submit()
        val s = vm.state.value
        assertEquals(FieldError.Required, s.errors.name)
        assertEquals(FieldError.InvalidEmail, s.errors.email)
        assertEquals(FieldError.Required, s.errors.message)
        assertTrue(repo.sent.isEmpty())
        assertFalse(s.submitted)
    }

    @Test fun editingFieldClearsItsError() = runTest {
        val vm = vm(FakeEnquiries())
        vm.submit()
        vm.onNameChange("Ann")
        assertNull(vm.state.value.errors.name)
        assertEquals(FieldError.Required, vm.state.value.errors.email)
    }

    @Test fun repositoryFailureKeepsFormAndShowsError() = runTest {
        val repo = FakeEnquiries(AppResult.Failure(AppError.NoNetwork))
        val vm = vm(repo)
        vm.fill()
        vm.submit()
        val s = vm.state.value
        assertEquals(AppError.NoNetwork, s.submitError)
        assertFalse(s.submitted)
        assertFalse(s.isSubmitting)
        assertEquals("Hello", s.form.message)
    }
}
