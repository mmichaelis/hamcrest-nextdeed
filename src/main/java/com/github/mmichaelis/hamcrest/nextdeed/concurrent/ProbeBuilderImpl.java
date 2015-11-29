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

import com.github.mmichaelis.hamcrest.nextdeed.glue.Consumer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.MoreObjects;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static com.github.mmichaelis.hamcrest.nextdeed.glue.HamcrestGlue.asPredicate;
import static java.util.Objects.requireNonNull;

/**
 * Implementation of {@link ProbeBuilder}.
 *
 * @since 1.0.0
 */
final class ProbeBuilderImpl<T, R> implements ProbeBuilder<T, R> {

  /**
   * Builder for delegate builder for {@link WaitFunction}.
   *
   * @since 1.0.0
   */
  @NotNull
  private final WaitFunctionBuilder<T, R> waitFunctionBuilder;
  /**
   * Target, typically the system under test, which will be probed.
   *
   * @since 1.0.0
   */
  @NotNull
  private final T target;
  /**
   * Consumers which will be called upon timeout.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Collection<Consumer<WaitTimeoutEvent<T, R>>> onTimeoutConsumers = new ArrayList<>();
  /**
   * Function to retrieve the actual value from the system under test.
   *
   * @since 1.0.0
   */
  private Function<T, R> actualFunction;
  private Function<Function<T, R>, Function<T, R>> waitFunctionPreProcessor =
      Functions.identity();

  ProbeBuilderImpl(@NotNull T target) {
    this.target = requireNonNull(target, "target must not be null.");
    waitFunctionBuilder = WaitFunction.waitFor(new Function<T, R>() {
      @Override
      public R apply(@Nullable T input) {
        return getActualFunction().apply(input);
      }
    });
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> withinMs(long timeoutMs) {
    waitFunctionBuilder.withinMs(timeoutMs);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit) {
    waitFunctionBuilder.within(timeout, timeUnit);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs) {
    waitFunctionBuilder.withFinalGracePeriodMs(gracePeriodMs);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> withFinalGracePeriod(long gracePeriod,
                                                 @NotNull TimeUnit timeUnit) {
    waitFunctionBuilder.withFinalGracePeriod(gracePeriod, timeUnit);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> withInitialDelayMs(long initialDelayMs) {
    waitFunctionBuilder.withInitialDelayMs(initialDelayMs);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> withInitialDelay(long initialDelay, @NotNull TimeUnit timeUnit) {
    waitFunctionBuilder.withInitialDelay(initialDelay, timeUnit);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> deceleratePollingBy(double decelerationFactor) {
    waitFunctionBuilder.deceleratePollingBy(decelerationFactor);
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> and() {
    waitFunctionBuilder.and();
    return this;
  }

  @NotNull
  @Override
  public ProbeBuilder<T, R> onTimeout(
      @NotNull Consumer<WaitTimeoutEvent<T, R>> waitTimeoutEventConsumer
  ) {
    onTimeoutConsumers.add(waitTimeoutEventConsumer);
    return this;
  }

  @Override
  public void assertThat(@NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
    assertThat(null, actualFunction, matcher);
  }

  @Override
  public void assertThat(@Nullable String reason,
                         @NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
    checkThat(
        actualFunction,
        matcher,
        new ThrowAssertionErrorStrategy<T, R>(reason, matcher));
  }

  @Override
  public void assumeThat(@NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
    assumeThat(null, actualFunction, matcher);
  }

  @Override
  public void assumeThat(@Nullable String reason,
                         @NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
    checkThat(
        actualFunction,
        matcher,
        new ThrowAssumptionViolatedExceptionStrategy<T, R>(reason, matcher));
  }

  @Override
  public void requireThat(@NotNull Function<T, R> actualFunction,
                          @NotNull Matcher<? super R> matcher) {
    requireThat(null, actualFunction, matcher);
  }

  @Override
  public void requireThat(@Nullable String reason,
                          @NotNull Function<T, R> actualFunction,
                          @NotNull Matcher<? super R> matcher) {
    checkThat(
        actualFunction,
        matcher,
        new ThrowWaitTimeoutExceptionStrategy<T, R>(reason, matcher));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("hash", Integer.toHexString(System.identityHashCode(this)))
                      .add("actualFunction", actualFunction)
                      .add("target", target)
                      .add("waitFunctionBuilder", waitFunctionBuilder)
                      .toString();
  }

  /**
   * <p>
   * Pre-process wait function. This is only meant for testing to possibly mock the wait
   * function. The default pre-processor is identity, which means that the function is
   * taken as is.
   * </p>
   * <p>
   * The function will retrieve the wait function as argument and may decide to return
   * a completely different function.
   * </p>
   *
   * @param waitFunctionPreProcessor function to map wait function
   * @return self-reference
   * @since 1.0.0
   */
  @VisibleForTesting
  @NotNull
  ProbeBuilder<T, R> preProcessWaitFunction(
      @NotNull Function<Function<T, R>, Function<T, R>> waitFunctionPreProcessor) {
    this.waitFunctionPreProcessor = waitFunctionPreProcessor;
    return this;
  }

  /**
   * Validate and get the actual function (function to retrieve the actual value).
   *
   * @return function
   */
  @NotNull
  private Function<T, R> getActualFunction() {
    return requireNonNull(actualFunction, "actualFunction must be set.");
  }

  private void checkThat(@NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher,
                         @NotNull final Function<WaitTimeoutEvent<T, R>, R> timeoutFunction) {
    this.actualFunction = actualFunction;
    Function<T, R> waitFunction = waitFunctionBuilder
        .toFulfill(asPredicate(matcher))
        .onTimeout(new Function<WaitTimeoutEvent<T, R>, R>() {
          @Override
          public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
            for (Consumer<WaitTimeoutEvent<T, R>> onTimeoutConsumer : onTimeoutConsumers) {
              onTimeoutConsumer.accept(input);
            }
            return timeoutFunction.apply(input);
          }
        })
        .get();
    Function<T, R> preProcessedWaitFunction =
        requireNonNull(waitFunctionPreProcessor.apply(waitFunction),
            "Wait function should not have been preprocessed to null.");
    preProcessedWaitFunction.apply(target);
  }
}
