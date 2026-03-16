package io.github.jacob_kelley22.eStore.exception

import io.github.jacob_kelley22.eStore.dto.error.APIErrorResponse
import io.github.jacob_kelley22.eStore.dto.error.FieldErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.WebRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handResourceNotFoundException(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<APIErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            APIErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = "Not Found",
                message = ex.message ?: "Resource Not Found",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequests(
        ex: BadRequestException,
        request: HttpServletRequest
    ): ResponseEntity<APIErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            APIErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Bad Request",
                message = ex.message ?: "Bad Request",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(
        ex: ForbiddenException,
        request: HttpServletRequest
    ): ResponseEntity<APIErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            APIErrorResponse(
                status = HttpStatus.FORBIDDEN.value(),
                error = "Forbidden",
                message = ex.message ?: "Access denied",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<APIErrorResponse> {

        val fieldErrors = ex.bindingResult.fieldErrors.map {
            FieldErrorResponse(
                field = it.field,
                message = it.defaultMessage ?: "Invalid value"
            )
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            APIErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = "Validation Failed",
                message = "Request validation failed",
                path = request.requestURI,
                fieldErrors = fieldErrors
            )
        )
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: HttpServletRequest
        ): ResponseEntity<APIErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            APIErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = "Internal Server Error",
                message = ex.message ?: "Internal Server Error",
                path = request.requestURI
            )
        )
    }

}