/*
 * Copyright 2015 Mark Michaelis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.mmichaelis.hamcrest.nextdeed.concurrent;

import static com.google.common.base.Optional.fromNullable;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A function that waits before returning its result until a given
 * predicate is met. The predicate will be applied to the return value
 * of delegate function which will be queried continuously for the expected
 * outcome.
 * </p>
 *
 * @since SINCE
 */
public class WaitFunction<T, R> implements Function<T, R> {

  /**
   * Minimum time to sleep between polls.
   *
   * @since SINCE
   */
  public static final int MINIMUM_SLEEP_TIME_MS = 1;
  /**
   * Initial delay to wait if we need to wait. Using 0L as base as a delay
   * can never go below this value and for unit tests we do not want to have
   * high delays.
   *
   * @since SINCE
   */
  private static final long DEFAULT_INITIAL_DELAY_MS = 0L;
  /**
   * Factor by which the polling factor decelerates.
   *
   * @since SINCE
   */
  private static final double DEFAULT_DECELERATION_FACTOR = 1.1;
  /**
   * A grace period for the last poll.
   *
   * @since SINCE
   */
  private static final long DEFAULT_GRACE_PERIOD_MS = 0L;
  /**
   * Function to continuously determine a result until it matches the
   * expectations denoted by {@link #predicate}.
   *
   * @since SINCE
   */
  @NotNull
  private final Function<T, R> delegateFunction;

  /**
   * Predicate to determine if the retrieved result by the given function meets
   * the expectations.
   *
   * @since SINCE
   */
  @NotNull
  private final Predicate<? super R> predicate;

  /**
   * Function to determine what to do on timeout.
   *
   * @since SINCE
   */
  @NotNull
  private final Function<WaitTimeoutEvent<T, R>, R> onTimeoutFunction;
  /**
   * Amount of timeout.
   *
   * @see #timeoutTimeUnit
   * @since SINCE
   */
  private final long timeout;
  /**
   * Unit for timeout.
   *
   * @see #timeout
   * @since SINCE
   */
  @NotNull
  private final TimeUnit timeoutTimeUnit;
  private final long gracePeriod;
  @NotNull
  private final TimeUnit gracePeriodTimeUnit;
  private final long initialDelay;
  @NotNull
  private final TimeUnit initialDelayTimeUnit;
  private final double decelerationFactor;

  protected WaitFunction(@NotNull Function<T, R> delegateFunction,
                         @NotNull Predicate<? super R> predicate,
                         @NotNull Function<WaitTimeoutEvent<T, R>, R> onTimeoutFunction,
                         long timeout,
                         @NotNull TimeUnit timeoutTimeUnit,
                         long gracePeriod,
                         @NotNull TimeUnit gracePeriodTimeUnit,
                         long initialDelay,
                         @NotNull TimeUnit initialDelayTimeUnit,
                         double decelerationFactor) {
    this.delegateFunction = delegateFunction;
    this.predicate = predicate;
    this.onTimeoutFunction = onTimeoutFunction;
    this.timeout = timeout;
    this.timeoutTimeUnit = timeoutTimeUnit;
    this.gracePeriod = gracePeriod;
    this.gracePeriodTimeUnit = gracePeriodTimeUnit;
    this.initialDelay = initialDelay;
    this.initialDelayTimeUnit = initialDelayTimeUnit;
    this.decelerationFactor = decelerationFactor;
  }

  public static <T, R> Builder<T, R> waitFor(Function<T, R> delegateFunction) {
    return new BuilderImpl<>(delegateFunction);
  }

  @Override
  public R apply(T item) {
    long timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeoutTimeUnit);
    long startMs = nowMillis();
    long deadlineTimeMs = startMs + timeoutMs;
    // At first, wait some initial delay between checks.
    long delay = TimeUnit.MILLISECONDS.convert(initialDelay, initialDelayTimeUnit);

    R result;
    while (true) {
      long beforeEvaluationTimeMs = nowMillis();
      result = delegateFunction.apply(item);
      long afterEvaluationTimeMs = nowMillis();
      if (predicate.apply(result)) {
        break;
      }
      if (afterEvaluationTimeMs > deadlineTimeMs) {
        return onTimeoutFunction.apply(
            new WaitTimeoutEventImpl<>(this,
                                       afterEvaluationTimeMs - startMs,
                                       item,
                                       result
            )
        );
      }
      delay =
          sleepAndRecalculateDelay(delay, deadlineTimeMs, beforeEvaluationTimeMs,
                                   afterEvaluationTimeMs);
    }
    return result;
  }

  @NotNull
  public Function<T, R> getDelegateFunction() {
    return delegateFunction;
  }

  @NotNull
  public Predicate<? super R> getPredicate() {
    return predicate;
  }

  public long getTimeout() {
    return timeout;
  }

  @NotNull
  public TimeUnit getTimeoutTimeUnit() {
    return timeoutTimeUnit;
  }

  /**
   * Sleep the given number of milliseconds.
   *
   * @param millis how long to sleep
   * @throws InterruptedException if the current thread has been interrupted
   * @since 0.1.0
   */
  @VisibleForTesting
  void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  /**
   * Retrieve the current time in milliseconds. Especially allows to override this behavior for
   * testing purpose.
   *
   * @return time in milliseconds
   * @since SINCE
   */
  @VisibleForTesting
  long nowMillis() {
    return System.currentTimeMillis();
  }

  /**
   * <p>
   * Decelerating wait. Decreases the polling interval over time to give the system under test a
   * chance to
   * actually reach the desired state.
   * </p>
   *
   * @since SINCE
   */
  private long sleepAndRecalculateDelay(long previousDelay,
                                        long deadlineTimeMs,
                                        long beforeEvaluationTimeMs,
                                        long afterEvaluationTimeMs) {
    long newDelay = previousDelay;
    // Leave at least as much time between two checks as the check itself took.
    long lastDuration = afterEvaluationTimeMs - beforeEvaluationTimeMs;
    if (lastDuration > newDelay) {
      newDelay = lastDuration;
    }

    // Wait, but not much longer than until the deadlineTimeMillis and at least a millisecond.
    try {
      long gracePeriodMs = TimeUnit.MILLISECONDS.convert(gracePeriod, gracePeriodTimeUnit);
      long timeLeftMs = deadlineTimeMs - afterEvaluationTimeMs;
      long sleepTimeMs = Math.max(MINIMUM_SLEEP_TIME_MS,
                                  Math.min(
                                      newDelay,
                                      timeLeftMs + gracePeriodMs
                                  )
      );
      sleep(sleepTimeMs);
    } catch (InterruptedException e) {
      throw new IllegalStateException("Unexpected interruption.", e);
    }

    // Make checks less and less frequently.
    // Increase the wait period using the deceleration factor, but
    // wait at least one millisecond longer next time.
    newDelay = Math.max(newDelay + 1, (long) (newDelay * decelerationFactor));
    return newDelay;
  }

  /**
   * <p>
   * A builder for WaitFunctions.
   * </p>
   *
   * @param <T> input the function will receive
   * @param <R> output the function will provide
   * @since SINCE
   */
  public interface Builder<T, R> extends Supplier<Function<T, R>>, WaitBuilder {
    /**
     * Predicate which the returned function value must fulfill. Defaults to
     * always true.
     *
     * @param predicate predicate to use
     * @return self-reference
     */
    @NotNull
    Builder<T, R> toFulfill(@NotNull Predicate<? super R> predicate);

    /**
     * <p>
     * Function to apply to timeout event on timeout. The function might consider to throw an
     * exception for example or ignore the actual timeout but return some default result
     * instead.
     * </p>
     *
     * @param timeoutFunction function to call on timeout
     * @return self-reference
     */
    @NotNull
    Builder<T, R> onTimeout(@NotNull Function<WaitTimeoutEvent<T, R>, R> timeoutFunction);


    @Override
    @NotNull
    Builder<T, R> withinMs(long timeoutMs);

    @Override
    @NotNull
    Builder<T, R> within(long timeout, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull
    Builder<T, R> withFinalGracePeriodMs(long gracePeriodMs);

    @Override
    @NotNull
    Builder<T, R> withFinalGracePeriod(long gracePeriod, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull
    Builder<T, R> withInitialDelayMs(long initialDelayMs);

    @Override
    @NotNull
    Builder<T, R> withInitialDelay(long initialDelay, @NotNull TimeUnit timeUnit);

    @Override
    @NotNull
    Builder<T, R> deceleratePollingBy(double decelerationFactor);

    @Override
    @NotNull
    Builder<T, R> and();
  }

  private static final class BuilderImpl<T, R> implements Builder<T, R> {

    private final Function<T, R> delegateFunction;
    @NotNull
    private Predicate<? super R> predicate = Predicates.alwaysTrue();
    private long timeout;
    @NotNull
    private TimeUnit timeoutTimeUnit = TimeUnit.MILLISECONDS;
    private long gracePeriod = DEFAULT_GRACE_PERIOD_MS;
    @NotNull
    private TimeUnit gracePeriodTimeUnit = TimeUnit.MILLISECONDS;
    private long initialDelay = DEFAULT_INITIAL_DELAY_MS;
    @NotNull
    private TimeUnit initialDelayTimeUnit = TimeUnit.MILLISECONDS;
    private double decelerationFactor = DEFAULT_DECELERATION_FACTOR;
    @Nullable
    private Function<WaitTimeoutEvent<T, R>, R> timeoutFunction;

    public BuilderImpl(Function<T, R> delegateFunction) {
      this.delegateFunction = delegateFunction;
    }

    @NotNull
    @Override
    public Builder<T, R> toFulfill(@NotNull Predicate<? super R> predicate) {
      this.predicate = predicate;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> withinMs(long timeoutMs) {
      within(timeoutMs, TimeUnit.MILLISECONDS);
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> within(long timeout, @NotNull TimeUnit timeUnit) {
      Preconditions.checkArgument(timeout >= 0L, "Timeout value must be positive.");
      this.timeout = timeout;
      timeoutTimeUnit = timeUnit;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> withFinalGracePeriodMs(long gracePeriodMs) {
      withFinalGracePeriod(gracePeriodMs, TimeUnit.MILLISECONDS);
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> withFinalGracePeriod(long gracePeriod,
                                              @NotNull TimeUnit timeUnit) {
      Preconditions.checkArgument(gracePeriod >= 0, "Grace period value must be positive.");
      this.gracePeriod = gracePeriod;
      gracePeriodTimeUnit = timeUnit;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> withInitialDelayMs(long initialDelayMs) {
      withInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS);
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> withInitialDelay(long initialDelay,
                                          @NotNull TimeUnit timeUnit) {
      Preconditions.checkArgument(initialDelay >= 0, "Initial delay must be positive.");
      this.initialDelay = initialDelay;
      initialDelayTimeUnit = timeUnit;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> deceleratePollingBy(double decelerationFactor) {
      Preconditions
          .checkArgument(decelerationFactor >= 1, "Factor must be greater than or equal to 1.");
      this.decelerationFactor = decelerationFactor;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> onTimeout(@NotNull Function<WaitTimeoutEvent<T, R>, R> timeoutFunction) {
      this.timeoutFunction = timeoutFunction;
      return this;
    }

    @NotNull
    @Override
    public Builder<T, R> and() {
      return this;
    }

    @Override
    public Function<T, R> get() {
      return new WaitFunction<>(
          delegateFunction,
          predicate,
          fromNullable(timeoutFunction).or(new Supplier<Function<WaitTimeoutEvent<T, R>, R>>() {
            @Override
            public Function<WaitTimeoutEvent<T, R>, R> get() {
              return new FailWithTimeoutException<>();
            }
          }),
          timeout,
          timeoutTimeUnit,
          gracePeriod,
          gracePeriodTimeUnit,
          initialDelay,
          initialDelayTimeUnit,
          decelerationFactor
      );
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("hash", Integer.toHexString(System.identityHashCode(this)))
          .add("decelerationFactor", decelerationFactor)
          .add("delegateFunction", delegateFunction)
          .add("gracePeriod", gracePeriod)
          .add("gracePeriodTimeUnit", gracePeriodTimeUnit)
          .add("initialDelay", initialDelay)
          .add("initialDelayTimeUnit", initialDelayTimeUnit)
          .add("predicate", predicate)
          .add("timeout", timeout)
          .add("timeoutFunction", timeoutFunction)
          .add("timeoutTimeUnit", timeoutTimeUnit)
          .toString();
    }
  }

  private static class FailWithTimeoutException<T, R>
      implements Function<WaitTimeoutEvent<T, R>, R> {

    @Override
    public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
      if (input == null) {
        // In contrast to Java 8 Function Guava enforces functions to handle null values.
        throw new WaitTimeoutException();
      }
      throw new WaitTimeoutException(input.describe());
    }

  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("decelerationFactor", decelerationFactor)
        .add("delegateFunction", delegateFunction)
        .add("gracePeriod", gracePeriod)
        .add("gracePeriodTimeUnit", gracePeriodTimeUnit)
        .add("initialDelay", initialDelay)
        .add("initialDelayTimeUnit", initialDelayTimeUnit)
        .add("onTimeoutFunction", onTimeoutFunction)
        .add("predicate", predicate)
        .add("timeout", timeout)
        .add("timeoutTimeUnit", timeoutTimeUnit)
        .toString();
  }
}
