package com.pingpong.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.CannotCreateTransactionException;

import java.lang.reflect.Method;
import java.net.ConnectException;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.SQLTransientConnectionException;

/**
 * Aspect that provides automatic retry with exponential backoff for database
 * operations.
 * Intercepts methods annotated with @RetryOnDbFailure and retries on connection
 * failures.
 */
@Aspect
@Component
@Order(1) // Execute before transaction aspect
@Slf4j
public class DatabaseRetryAspect {

    @Around("@annotation(com.pingpong.aspect.RetryOnDbFailure) || @within(com.pingpong.aspect.RetryOnDbFailure)")
    public Object retryOnDbFailure(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get annotation from method or class
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RetryOnDbFailure annotation = method.getAnnotation(RetryOnDbFailure.class);
        if (annotation == null) {
            annotation = joinPoint.getTarget().getClass().getAnnotation(RetryOnDbFailure.class);
        }

        int maxAttempts = annotation != null ? annotation.maxAttempts() : 3;
        long delayMs = annotation != null ? annotation.initialDelayMs() : 1000;
        double multiplier = annotation != null ? annotation.multiplier() : 2.0;
        long maxDelayMs = annotation != null ? annotation.maxDelayMs() : 30000;

        String methodName = joinPoint.getSignature().toShortString();
        Throwable lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return joinPoint.proceed();
            } catch (Throwable e) {
                if (!isRetryableException(e)) {
                    // Not a connection issue, don't retry
                    throw e;
                }

                lastException = e;

                if (attempt == maxAttempts) {
                    log.error("Database operation {} failed after {} attempts. Giving up.",
                            methodName, maxAttempts, e);
                    throw e;
                }

                log.warn("Database connection failure in {} (attempt {}/{}). Retrying in {}ms... Error: {}",
                        methodName, attempt, maxAttempts, delayMs, e.getMessage());

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw lastException;
                }

                // Exponential backoff with cap
                delayMs = Math.min((long) (delayMs * multiplier), maxDelayMs);
            }
        }

        // Should never reach here, but just in case
        throw lastException;
    }

    /**
     * Determines if the exception is a retryable database connection issue.
     */
    private boolean isRetryableException(Throwable e) {
        // Check the exception and its causes
        Throwable current = e;
        while (current != null) {
            if (isRetryableExceptionType(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean isRetryableExceptionType(Throwable e) {
        // Spring JDBC/Transaction exceptions
        if (e instanceof CannotGetJdbcConnectionException ||
                e instanceof CannotCreateTransactionException ||
                e instanceof TransientDataAccessException ||
                e instanceof DataAccessResourceFailureException) {
            return true;
        }

        // SQL connection/timeout exceptions
        if (e instanceof SQLTransientConnectionException ||
                e instanceof SQLTimeoutException ||
                e instanceof ConnectException) {
            return true;
        }

        // Check for connection-related SQL exceptions
        if (e instanceof SQLException) {
            SQLException sqlEx = (SQLException) e;
            String sqlState = sqlEx.getSQLState();
            // Connection exception class (08xxx) in SQL standard
            if (sqlState != null && sqlState.startsWith("08")) {
                return true;
            }
        }

        // Check exception class name for HikariCP and other pool exceptions
        String className = e.getClass().getName().toLowerCase();
        if (className.contains("hikari") ||
                className.contains("pool") ||
                className.contains("connection")) {
            return true;
        }

        // Check exception message for common connection error patterns
        String message = e.getMessage();
        if (message != null) {
            String lowerMessage = message.toLowerCase();

            // HikariCP specific patterns
            if (lowerMessage.contains("hikaripool") ||
                    lowerMessage.contains("not available") ||
                    lowerMessage.contains("request timed out")) {
                return true;
            }

            // General connection patterns
            if (lowerMessage.contains("connection") &&
                    (lowerMessage.contains("refused") ||
                            lowerMessage.contains("timeout") ||
                            lowerMessage.contains("timed out") ||
                            lowerMessage.contains("unavailable") ||
                            lowerMessage.contains("closed") ||
                            lowerMessage.contains("reset") ||
                            lowerMessage.contains("failed"))) {
                return true;
            }
        }

        return false;
    }
}
