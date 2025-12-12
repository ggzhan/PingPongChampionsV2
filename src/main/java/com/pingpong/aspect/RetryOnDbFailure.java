package com.pingpong.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark methods that should be retried on database connection
 * failures.
 * Uses exponential backoff strategy.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface RetryOnDbFailure {

    /**
     * Maximum number of retry attempts (default: 3)
     */
    int maxAttempts() default 3;

    /**
     * Initial delay in milliseconds before first retry (default: 1000ms)
     */
    long initialDelayMs() default 1000;

    /**
     * Multiplier for exponential backoff (default: 2.0)
     */
    double multiplier() default 2.0;

    /**
     * Maximum delay in milliseconds (default: 30000ms)
     */
    long maxDelayMs() default 30000;
}
