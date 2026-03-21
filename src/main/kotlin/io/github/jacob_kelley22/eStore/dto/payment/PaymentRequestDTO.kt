package io.github.jacob_kelley22.eStore.dto.payment

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PaymentRequestDTO(

    @field:NotBlank
    @field:Pattern(
        regexp = "^[a-zA-Z0-9-_]{10,100}$",
        message = "Idempotency key must be 10-100 characters (letters, numbers, hyphens, underscores)"
    )
    val idempotencyKey: String,

    @field:NotBlank
    @field:Pattern(
        regexp = "^[\\d -]{13,25}$",
        message = "Card number must be between 13 and 19 digits and may include spaces or hyphens."
    )
    val cardNumber: String,

    @field:NotBlank
    val cardHolderName: String,

    @field:Min(1)
    @field:Max(12)
    val expirationMonth: Int,

    val expirationYear: Int,

    @field:Pattern(regexp = "^\\d{3,4}$", message = "CVV must be 3 or 4 digits")
    val cvv: String
    )
