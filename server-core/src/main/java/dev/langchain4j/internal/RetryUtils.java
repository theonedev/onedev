package dev.langchain4j.internal;

import java.util.Random;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.Internal;
import dev.langchain4j.exception.LangChain4jException;
import dev.langchain4j.exception.NonRetriableException;
import io.onedev.server.exception.ExceptionUtils;

/**
 * Utility class for retrying actions.
 */
@Internal
public final class RetryUtils {

    private static final Random RANDOM = new Random();

    private RetryUtils() {}

    private static final Logger log = LoggerFactory.getLogger(RetryUtils.class);

    /**
     * This method returns a RetryPolicy.Builder.
     *
     * @return A RetryPolicy.Builder.
     */
    public static RetryPolicy.Builder retryPolicyBuilder() {
        return new RetryPolicy.Builder();
    }

    /**
     * This class encapsulates a retry policy.
     */
    public static final class RetryPolicy {

        /**
         * This class encapsulates a retry policy builder.
         */
        public static final class Builder {

            private int maxRetries = 2;
            private int delayMillis = 1000;
            private double jitterScale = 0.2;
            private double backoffExp = 1.5;

            /**
             * Construct a RetryPolicy.Builder.
             */
            public Builder() {}

            /**
             * Sets the default maximum number of retries.
             *
             * @param maxRetries The maximum number of retries.
             *                   The action can be executed up to {@code maxRetries + 1} times.
             * @return {@code this}
             */
            public Builder maxRetries(int maxRetries) {
                this.maxRetries = maxRetries;
                return this;
            }

            /**
             * Sets the base delay in milliseconds.
             *
             * <p>The delay is calculated as follows:
             * <ol>
             *     <li>Calculate the raw delay in milliseconds as
             *         {@code delayMillis * Math.pow(backoffExp, retry)}.</li>
             *     <li>Calculate the jitter delay in milliseconds as
             *         {@code rawDelayMs + rand.nextInt((int) (rawDelayMs * jitterScale))}.</li>
             *     <li>Sleep for the jitter delay in milliseconds.</li>
             * </ol>
             *
             * @param delayMillis The delay in milliseconds.
             * @return {@code this}
             */
            public Builder delayMillis(int delayMillis) {
                this.delayMillis = delayMillis;
                return this;
            }

            /**
             * Sets the jitter scale.
             *
             * <p>The jitter delay in milliseconds is calculated as
             * {@code rawDelayMs + rand.nextInt((int) (rawDelayMs * jitterScale))}.
             *
             * @param jitterScale The jitter scale.
             * @return {@code this}
             */
            public Builder jitterScale(double jitterScale) {
                this.jitterScale = jitterScale;
                return this;
            }

            /**
             * Sets the backoff exponent.
             *
             * @param backoffExp The backoff exponent.
             * @return {@code this}
             */
            public Builder backoffExp(double backoffExp) {
                this.backoffExp = backoffExp;
                return this;
            }

            /**
             * Builds a RetryPolicy.
             *
             * @return A RetryPolicy.
             */
            public RetryPolicy build() {
                return new RetryPolicy(maxRetries, delayMillis, jitterScale, backoffExp);
            }
        }

        private final int maxRetries;
        private final int delayMillis;
        private final double jitterScale;
        private final double backoffExp;

        /**
         * Construct a RetryPolicy.
         *
         * @param maxRetries The maximum number of retries.
         *                   The action can be executed up to {@code maxRetries + 1} times.
         * @param delayMillis The delay in milliseconds.
         * @param jitterScale The jitter scale.
         * @param backoffExp  The backoff exponent.
         */
        public RetryPolicy(int maxRetries, int delayMillis, double jitterScale, double backoffExp) {
            this.maxRetries = maxRetries;
            this.delayMillis = delayMillis;
            this.jitterScale = jitterScale;
            this.backoffExp = backoffExp;
        }

        /**
         * This method returns the raw delay in milliseconds after a given retry.
         *
         * @param retry The retry number.
         * @return The raw delay in milliseconds.
         */
        public double rawDelayMs(int retry) {
            return delayMillis * Math.pow(backoffExp, retry);
        }

        /**
         * This method returns the jitter delay in milliseconds after a given retry.
         *
         * @param retry The retry number.
         * @return The jitter delay in milliseconds.
         */
        public int jitterDelayMillis(int retry) {
            double delay = rawDelayMs(retry);
            double jitter = delay * jitterScale;
            return (int) (delay + RANDOM.nextInt((int) jitter));
        }

        /**
         * This method sleeps after a given retry.
         *
         * @param retry The retry number.
         */
        @JacocoIgnoreCoverageGenerated
        public void sleep(int retry) {
            try {
                Thread.sleep(jitterDelayMillis(retry));
            } catch (InterruptedException ignored) {
                // pass
            }
        }

        /**
         * This method attempts to execute a given action up to 3 times with an exponential backoff.
         * If the action fails on all attempts, it throws a RuntimeException.
         *
         * @param action The action to be executed.
         * @param <T>    The type of the result of the action.
         * @return The result of the action if it is successful.
         * @throws RuntimeException if the action fails on all attempts.
         */
        public <T> T withRetry(Callable<T> action) {
            return withRetry(action, maxRetries);
        }

        /**
         * This method attempts to execute a given action up to a specified number of times with an exponential backoff.
         * If the action fails on all attempts, it throws a RuntimeException.
         *
         * @param action     The action to be executed.
         * @param maxRetries The maximum number of retries.
         *                   The action can be executed up to {@code maxRetries + 1} times.
         * @param <T>        The type of the result of the action.
         * @return The result of the action if it is successful.
         * @throws RuntimeException if the action fails on all attempts.
         */
        public <T> T withRetry(Callable<T> action, int maxRetries) {
            int retry = 0;
            while (true) {
                try {
                    return action.call();
                } catch (NonRetriableException e) {
                    throw e;
                } catch (Exception e) {
                    if (retry >= maxRetries || ExceptionUtils.find(e, InterruptedException.class) != null) {
                        throw e instanceof RuntimeException re ? re : new LangChain4jException(e);
                    }

                    log.warn(
                            "A retriable exception occurred. Remaining retries: %s of %s"
                                    .formatted(maxRetries - retry, maxRetries),
                            e);

                    sleep(retry);
                }
                retry++;
            }
        }
    }

    /**
     * Default retry policy used by {@link #withRetry(Callable)}.
     */
    public static final RetryPolicy DEFAULT_RETRY_POLICY = retryPolicyBuilder()
            .maxRetries(2)
            .delayMillis(500)
            .jitterScale(0.2)
            .backoffExp(1.5)
            .build();

    /**
     * This method attempts to execute a given action up to 3 times with an exponential backoff.
     * If the action fails on all attempts, it throws a RuntimeException.
     *
     * @param action The action to be executed.
     * @param <T>    The type of the result of the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetry(Callable<T> action) {
        return DEFAULT_RETRY_POLICY.withRetry(action);
    }

    /**
     * This method attempts to execute a given action up to a specified number of times with an exponential backoff.
     * If the action fails on all attempts, it throws a RuntimeException.
     *
     * @param action     The action to be executed.
     * @param maxRetries The maximum number of retries.
     *                   The action can be executed up to {@code maxRetries + 1} times.
     * @param <T>        The type of the result of the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetry(Callable<T> action, int maxRetries) {
        return DEFAULT_RETRY_POLICY.withRetry(action, maxRetries);
    }

    /**
     * This method attempts to execute a given action up to a specified number of times with an exponential backoff.
     * If the action fails on all attempts, it throws a RuntimeException.
     *
     * @param action     The action to be executed.
     * @param maxRetries The maximum number of retries.
     *                   The action can be executed up to {@code maxRetries + 1} times.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static void withRetry(Runnable action, int maxRetries) {
        DEFAULT_RETRY_POLICY.withRetry(
                () -> {
                    action.run();
                    return null;
                },
                maxRetries);
    }

    /**
     * This method attempts to execute a given action up to 3 times with an exponential backoff.
     * If the action fails, the Exception causing the failure will be mapped with the default {@link ExceptionMapper}.
     *
     * @param action The action to be executed.
     * @param <T>    The type of the result of the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetryMappingExceptions(Callable<T> action) {
        return withRetry(() -> ExceptionMapper.DEFAULT.withExceptionMapper(action));
    }

    /**
     * This method attempts to execute a given action up to a specified number of times with an exponential backoff.
     * If the action fails, the Exception causing the failure will be mapped with the default {@link ExceptionMapper}.
     *
     * @param action     The action to be executed.
     * @param maxRetries The maximum number of retries.
     *                   The action can be executed up to {@code maxRetries + 1} times.
     * @param <T>        The type of the result of the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetryMappingExceptions(Callable<T> action, int maxRetries) {
        return withRetryMappingExceptions(action, maxRetries, ExceptionMapper.DEFAULT);
    }

    /**
     * This method attempts to execute a given action up to a specified number of times with an exponential backoff.
     * If the action fails, the Exception causing the failure will be mapped with the provided {@link ExceptionMapper}.
     *
     * @param action          The action to be executed.
     * @param maxRetries      The maximum number of retries.
     *                        The action can be executed up to {@code maxRetries + 1} times.
     * @param exceptionMapper The ExceptionMapper used to translate the exception that caused the failure of the action invocation.
     * @param <T>             The type of the result of the action.
     * @return The result of the action if it is successful.
     * @throws RuntimeException if the action fails on all attempts.
     */
    public static <T> T withRetryMappingExceptions(
            Callable<T> action, int maxRetries, ExceptionMapper exceptionMapper) {
        return withRetry(() -> exceptionMapper.withExceptionMapper(action), maxRetries);
    }
}
