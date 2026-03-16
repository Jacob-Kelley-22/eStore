package io.github.jacob_kelley22.eStore.dto.payment

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class PaymentRequestDTO(

    @field:NotBlank
    @field:Pattern(regexp = "\\d{16}", message = "Card number must be 16 digits")
    val cardNumber: String,

    @field:NotBlank
    val cardHolderName: String,

    @field:Min(1)
    @field:Max(12)
    val expirationMonth: Int,

    @field:Min(2026)
    val expirationYear: Int,

    @field:Pattern(regexp = "\\d{3,4}", message = "CVV must be 3 or 4 digits")
    val cvv: String
    )
