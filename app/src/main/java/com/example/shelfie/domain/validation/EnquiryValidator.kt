package com.example.shelfie.domain.validation

enum class FieldError { Required, InvalidEmail, InvalidPhone, TooLong }

data class EnquiryForm(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
)

data class EnquiryErrors(
    val name: FieldError? = null,
    val email: FieldError? = null,
    val phone: FieldError? = null,
    val message: FieldError? = null,
) {
    val hasErrors get() = listOf(name, email, phone, message).any { it != null }
}

/** Pure validation rules for the enquiry form; trims input before checking. */
object EnquiryValidator {
    const val MAX_NAME = 100
    const val MAX_EMAIL = 254
    const val MAX_PHONE = 20
    const val MAX_MESSAGE = 1000

    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$")
    private val PHONE = Regex("^\\+?[0-9 ()-]{7,}$")

    fun validate(form: EnquiryForm): EnquiryErrors {
        val name = form.name.trim()
        val email = form.email.trim()
        val phone = form.phone.trim()
        val message = form.message.trim()
        return EnquiryErrors(
            name = when {
                name.isEmpty() -> FieldError.Required
                name.length > MAX_NAME -> FieldError.TooLong
                else -> null
            },
            email = when {
                email.isEmpty() -> FieldError.Required
                email.length > MAX_EMAIL -> FieldError.TooLong
                !EMAIL.matches(email) -> FieldError.InvalidEmail
                else -> null
            },
            phone = when {
                phone.isEmpty() -> null
                phone.length > MAX_PHONE -> FieldError.TooLong
                !PHONE.matches(phone) -> FieldError.InvalidPhone
                else -> null
            },
            message = when {
                message.isEmpty() -> FieldError.Required
                message.length > MAX_MESSAGE -> FieldError.TooLong
                else -> null
            },
        )
    }
}
