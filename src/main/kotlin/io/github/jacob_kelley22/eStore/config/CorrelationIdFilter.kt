package io.github.jacob_kelley22.eStore.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
class CorrelationIdFilter : OncePerRequestFilter() {

    companion object {
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
        private const val MDC_KEY = "correlationId"
    }

    private val logger = LoggerFactory.getLogger(CorrelationIdFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val correlationId = request.getHeader(CORRELATION_ID_HEADER)
            ?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        MDC.put(MDC_KEY, correlationId)
        response.setHeader(CORRELATION_ID_HEADER, correlationId)

        try{
            logger.info("Incoming request: {} {}", request.method, request.requestURI)
            filterChain.doFilter(request, response)
            logger.info("Completed request: {} {} -> {}", request.method, request.requestURI, response.status)
        } finally {
            MDC.remove(MDC_KEY)
        }
    }

}