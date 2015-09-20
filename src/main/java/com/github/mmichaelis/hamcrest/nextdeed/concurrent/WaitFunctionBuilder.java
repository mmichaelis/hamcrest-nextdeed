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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * A builder for WaitFunctions.
 * </p>
 *
 * @param <T> input the function will receive
 * @param <R> output the function will provide
 * @since SINCE
 */
public interface WaitFunctionBuilder<T, R> extends Supplier<Function<T, R>>, WaitBuilder {
  /**
   * Predicate which the returned function value must fulfill. Defaults to
   * always true.
   *
   * @param predicate predicate to use
   * @return self-reference
   */
  @NotNull
  WaitFunctionBuilder<T, R> toFulfill(@NotNull Predicate<? super R> predicate);

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
  WaitFunctionBuilder<T, R> onTimeout(@NotNull Function<WaitTimeoutEvent<T, R>, R> timeoutFunction);


  @Override
  @NotNull
  WaitFunctionBuilder<T, R> withinMs(long timeoutMs);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> withFinalGracePeriod(long gracePeriod, @NotNull TimeUnit timeUnit);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> withInitialDelayMs(long initialDelayMs);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> withInitialDelay(long initialDelay, @NotNull TimeUnit timeUnit);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> deceleratePollingBy(double decelerationFactor);

  @Override
  @NotNull
  WaitFunctionBuilder<T, R> and();
}
