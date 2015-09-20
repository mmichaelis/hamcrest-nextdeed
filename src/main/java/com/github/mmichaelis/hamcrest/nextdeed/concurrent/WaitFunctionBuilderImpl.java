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
 * @since SINCE
 */
final class WaitFunctionBuilderImpl<T, R> implements WaitFunctionBuilder<T, R> {

  /**
   * A grace period for the last poll.
   *
   * @since SINCE
   */
  private static final long DEFAULT_GRACE_PERIOD_MS = 0L;
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

  public WaitFunctionBuilderImpl(Function<T, R> delegateFunction) {
    this.delegateFunction = delegateFunction;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> toFulfill(@NotNull Predicate<? super R> predicate) {
    this.predicate = predicate;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> onTimeout(
      @NotNull Function<WaitTimeoutEvent<T, R>, R> timeoutFunction) {
    this.timeoutFunction = timeoutFunction;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> withinMs(long timeoutMs) {
    within(timeoutMs, TimeUnit.MILLISECONDS);
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit) {
    Preconditions.checkArgument(timeout >= 0L, "Timeout value must be positive.");
    this.timeout = timeout;
    timeoutTimeUnit = timeUnit;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs) {
    withFinalGracePeriod(gracePeriodMs, TimeUnit.MILLISECONDS);
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> withFinalGracePeriod(long gracePeriod,
                                                        @NotNull TimeUnit timeUnit) {
    Preconditions.checkArgument(gracePeriod >= 0, "Grace period value must be positive.");
    this.gracePeriod = gracePeriod;
    gracePeriodTimeUnit = timeUnit;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> withInitialDelayMs(long initialDelayMs) {
    withInitialDelay(initialDelayMs, TimeUnit.MILLISECONDS);
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> withInitialDelay(long initialDelay,
                                                    @NotNull TimeUnit timeUnit) {
    Preconditions.checkArgument(initialDelay >= 0, "Initial delay must be positive.");
    this.initialDelay = initialDelay;
    initialDelayTimeUnit = timeUnit;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> deceleratePollingBy(double decelerationFactor) {
    Preconditions
        .checkArgument(decelerationFactor >= 1, "Factor must be greater than or equal to 1.");
    this.decelerationFactor = decelerationFactor;
    return this;
  }

  @NotNull
  @Override
  public WaitFunctionBuilder<T, R> and() {
    return this;
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

  @Override
  public Function<T, R> get() {
    return new WaitFunction<>(
        delegateFunction,
        predicate,
        fromNullable(timeoutFunction).or(new Supplier<Function<WaitTimeoutEvent<T, R>, R>>() {
          @Override
          public Function<WaitTimeoutEvent<T, R>, R> get() {
            return new FailWithTimeoutExceptionFunction<>();
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


}
