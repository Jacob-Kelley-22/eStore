package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO
import io.github.jacob_kelley22.eStore.dto.payment.PaymentResponseDTO
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.YearMonth
import java.util.UUID

@Service
class PaymentService {

    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    fun processPayment(request: PaymentRequestDTO): PaymentResponseDTO {
        logger.info("Processing simulated payment for card holder {}", request.cardHolderName)

        // Validate the card number
        if(!isValidLuhn(request.cardNumber)) {
            logger.warn("Payment declined: Invalid card number for card holder {}", request.cardHolderName)
            throw BadRequestException("Invalid card number")
        }

        val now = YearMonth.now()
        val expirationDate = YearMonth.of(request.expirationYear, request.expirationMonth)

        if (expirationDate.isBefore(now)) {
            logger.warn("Payment declined: expired card for {}", request.cardHolderName)
            throw BadRequestException("Card is expired")
        }

        val transactionId = UUID.randomUUID().toString()

        /* DO PAYMENT PROCESSING HERE
        * As this is a learning project, an actual payment processing API will not be used
        * */

        logger.info("Payment approved for {} with transaction id {}",
            request.cardHolderName, transactionId)

        return PaymentResponseDTO(
            approved = true,
            message = "Payment approved",
            transactionId = transactionId
        )
    }

    fun isValidLuhn(cardNumber: String): Boolean {

        // Assuming input isn't cleaned to only be digits. Can add filtering instead
        val cleaned = cardNumber.replace(" ", "")
       // Also assuming the need to adhere to ISO standard, which is min 13-digits
        if(cleaned.length <= 12 || cleaned.any { !it.isDigit() } || cleaned.all { it == '0'}) {
            // Logger line for testing
            logger.warn("Invalid card number. Length: {}, Non-Digit Characters: {}, " +
                    "Non-zero Characters: {}",
                cleaned.length, cleaned.any { !it.isDigit() }, cleaned.all { it == '0' })
            return false
        }
        // Processing digits from right to left
        val sum = cleaned.reversed() // Reverse string
            .mapIndexed { index, char ->  // mapIndexed to track current index
                val digit = char.digitToInt() // Create a list of the reversed digits as Ints

                // Double second digits of reversed number
                if (index % 2 == 1) {
                    val doubled = digit * 2
                    if (doubled > 9) doubled - 9 else doubled
                } else {
                    digit
                }
            }
            .sum()

        // The number is valid if the total sum is a multiple of 10
        return sum % 10 == 0
    }
}