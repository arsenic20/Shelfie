package com.example.shelfie.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class EnquiryValidatorTest {
    private val valid = EnquiryForm("Ann", "ann@example.com", "", "Is it in stock?")

    @Test fun validFormHasNoErrors() = assertFalse(EnquiryValidator.validate(valid).hasErrors)

    @Test fun requiredFieldsFlagged() {
        val e = EnquiryValidator.validate(EnquiryForm("  ", "", "", " "))
        assertEquals(FieldError.Required, e.name)
        assertEquals(FieldError.Required, e.email)
        assertEquals(FieldError.Required, e.message)
        assertNull(e.phone)
    }

    @Test fun invalidEmails() {
        listOf("ann", "ann@", "ann@example", "a b@example.com").forEach {
            assertEquals(it, FieldError.InvalidEmail, EnquiryValidator.validate(valid.copy(email = it)).email)
        }
    }

    @Test fun emailIsTrimmed() = assertNull(EnquiryValidator.validate(valid.copy(email = " ann@example.com ")).email)

    @Test fun phoneOptionalButValidatedWhenPresent() {
        assertNull(EnquiryValidator.validate(valid.copy(phone = "+44 (20) 7946-0000")).phone)
        assertEquals(FieldError.InvalidPhone, EnquiryValidator.validate(valid.copy(phone = "abc123")).phone)
    }

    @Test fun lengthLimits() {
        val e = EnquiryValidator.validate(
            EnquiryForm("a".repeat(101), "a".repeat(250) + "@x.com", "1".repeat(21), "m".repeat(1001))
        )
        assertEquals(FieldError.TooLong, e.name)
        assertEquals(FieldError.TooLong, e.email)
        assertEquals(FieldError.TooLong, e.phone)
        assertEquals(FieldError.TooLong, e.message)
    }
}
