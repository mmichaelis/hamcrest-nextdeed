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

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A function that waits before returning its result until a given
 * predicate is met. The predicate will be applied to the return value
 * of delegate function which will be queried continuously for the expected
 * outcome.
 * </p>
 *
 * @since 1.0.0
 */
public class WaitFunction<T, R> implements Function<T, R> {

  /**
   * Minimum time to sleep between polls.
   *
   * @since 1.0.0
   */
  private static final int MINIMUM_SLEEP_TIME_MS = 1;
  /**
   * Function to continuously determine a result until it matches the
   * expectations denoted by {@link #predicate}.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Function<T, R> delegateFunction;

  /**
   * Predicate to determine if the retrieved result by the given function meets
   * the expectations.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Predicate<? super R> predicate;

  /**
   * Function to determine what to do on timeout.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Function<WaitTimeoutEvent<T, R>, R> onTimeoutFunction;
  /**
   * Amount of timeout.
   *
   * @see #timeoutTimeUnit
   * @since 1.0.0
   */
  private final long timeout;
  /**
   * Unit for timeout.
   *
   * @see #timeout
   * @since 1.0.0
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

  @SuppressWarnings(
      "squid:S00107" // Complexity of constructor arguments hidden by builder pattern.
  )
  WaitFunction(@NotNull Function<T, R> delegateFunction,
               @NotNull Predicate<? super R> predicate,
               @NotNull Function<WaitTimeoutEvent<T, R>, R> onTimeoutFunction,
               long timeout,
               @NotNull TimeUnit timeoutTimeUnit,
               long gracePeriod,
               @NotNull TimeUnit gracePeriodTimeUnit,
               long initialDelay,
               @NotNull TimeUnit initialDelayTimeUnit,
               double decelerationFactor) {
    this.delegateFunction = requireNonNull(delegateFunction, "delegateFunction must not be null.");
    this.predicate = requireNonNull(predicate, "predicate must not be null.");
    this.onTimeoutFunction = requireNonNull(onTimeoutFunction,
                                            "onTimeoutFunction must not be null.");
    this.timeout = timeout;
    this.timeoutTimeUnit = requireNonNull(timeoutTimeUnit, "timeoutTimeUnit must not be null.");
    this.gracePeriod = gracePeriod;
    this.gracePeriodTimeUnit = requireNonNull(gracePeriodTimeUnit,
                                              "gracePeriodTimeUnit must not be null.");
    this.initialDelay = initialDelay;
    this.initialDelayTimeUnit = requireNonNull(initialDelayTimeUnit,
                                               "initialDelayTimeUnit must not be null.");
    this.decelerationFactor = decelerationFactor;
  }

  public static <T, R> WaitFunctionBuilder<T, R> waitFor(@NotNull Function<T, R> delegateFunction) {
    return new WaitFunctionBuilderImpl<>(delegateFunction);
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
   * @since 1.0.0
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
   * @since 1.0.0
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
}
