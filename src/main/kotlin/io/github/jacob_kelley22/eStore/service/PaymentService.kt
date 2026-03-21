package io.github.jacob_kelley22.eStore.service

import io.github.jacob_kelley22.eStore.dto.payment.PaymentRequestDTO
import io.github.jacob_kelley22.eStore.dto.payment.PaymentResponseDTO
import io.github.jacob_kelley22.eStore.entity.Order
import io.github.jacob_kelley22.eStore.entity.Payment
import io.github.jacob_kelley22.eStore.entity.PaymentStatus
import io.github.jacob_kelley22.eStore.exception.BadRequestException
import io.github.jacob_kelley22.eStore.repository.PaymentRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository
) {

    private val logger = LoggerFactory.getLogger(PaymentService::class.java)

    fun processPayment(order: Order, request: PaymentRequestDTO): PaymentResponseDTO {
        logger.info("Processing simulated payment for card holder {}", request.cardHolderName)

        val cleanedCardNumber = request.cardNumber
            .replace(" ", "")
            .replace("-", "")

        val payment = Payment(
            order = order,
            status = PaymentStatus.PENDING,
            transactionId = UUID.randomUUID().toString(),
            cardHolderName = request.cardHolderName,
            last4 = cleanedCardNumber.takeLast(4),
            amount = order.totalPrice
        )

        // Validate the card number
        if (!isValidLuhn(request.cardNumber)) {
            payment.status = PaymentStatus.FAILED
            payment.failureReason = "Invalid card number"
            payment.processedAt = LocalDateTime.now()

            paymentRepository.save(payment)

            logger.warn("Payment declined: Invalid card number for card holder {}", request.cardHolderName)

            throw BadRequestException("Invalid card number")
        }

        val now = YearMonth.now()
        val expirationDate = YearMonth.of(request.expirationYear, request.expirationMonth)

        if (expirationDate.isBefore(now)) {
            payment.status = PaymentStatus.FAILED
            payment.failureReason = "Card is expired"
            payment.processedAt = LocalDateTime.now()

            paymentRepository.save(payment)

            logger.warn("Payment declined: expired card for {}", request.cardHolderName)

            throw BadRequestException("Card is expired")
        }
        payment.status = PaymentStatus.SUCCEEDED
        payment.processedAt = LocalDateTime.now()

        val savedPayment = paymentRepository.save(payment)

        logger.info(
            "Payment approved for order {} with transaction id {}",
            order.id,
            savedPayment.transactionId
        )


        return PaymentResponseDTO(
            paymentId = savedPayment.id!!,
            publicId = savedPayment.publicId,
            orderId = order.id,
            status = savedPayment.status,
            transactionId = savedPayment.transactionId,
            amount = savedPayment.amount.toPlainString(),
            message = "Payment approved"
        )
    }

    fun isValidLuhn(cardNumber: String): Boolean {

        // Assuming input isn't cleaned to only be digits. Can add filtering instead
        val cleaned = cardNumber
            .replace(" ", "")
            .replace("-", "")

        // Also assuming the need to adhere to ISO standard, which is min 13-digits
        if (cleaned.length !in 13..19 || cleaned.any { !it.isDigit() } || cleaned.all { it == '0' }) {
            // Logger line for testing
            logger.warn(
                "Invalid card number. Length: {}, Non-Digit Characters: {}, " +
                    "Zero Characters: {}",
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