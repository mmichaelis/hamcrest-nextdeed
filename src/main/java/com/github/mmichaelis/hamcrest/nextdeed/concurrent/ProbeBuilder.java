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

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Builder for Probes. In contrast {@link WaitBuilder} which only contains any wait-specific
 * methods the {@code ProbeBuilder} contains methods for specifying the check and fail
 * strategies to apply. In addition you can use {@link #onTimeout(Consumer)} to get informed
 * in timeouts and possibly create more debugging information like database dumps, screenshots, ...
 *
 * @param <T> the type of system you are probing
 * @param <R> the type of state variable you are polling
 */
public interface ProbeBuilder<T, R> extends WaitBuilder, ProbeAssert<T,R>, ProbeAssume<T, R>,
                                            ProbeRequire<T, R> {

  @NotNull
  @Override
  ProbeBuilder<T, R> withinMs(long timeoutMs);

  @NotNull
  @Override
  ProbeBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit);

  @NotNull
  @Override
  ProbeBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs);

  @NotNull
  @Override
  ProbeBuilder<T, R> withFinalGracePeriod(long gracePeriod,
                                          @NotNull TimeUnit timeUnit);

  @NotNull
  @Override
  ProbeBuilder<T, R> withInitialDelayMs(long initialDelayMs);

  @NotNull
  @Override
  ProbeBuilder<T, R> withInitialDelay(long initialDelay,
                                      @NotNull TimeUnit timeUnit);

  @NotNull
  @Override
  ProbeBuilder<T, R> deceleratePollingBy(double decelerationFactor);

  @NotNull
  @Override
  ProbeBuilder<T, R> and();

  /**
   * <p>
   * On timeout specify a consumer which will be called upon timeout. The consumer is just called
   * for information and must not mangle with the wait-lifecycle. Thus it can neither prevent the
   * failure at the end nor should it throw exceptions on its own.
   * </p>
   * <p>
   * The timeout event should provide enough information for example to dump the current
   * system state, take screenshots, etc.
   * </p>
   * <p>
   * Multiple event consumers can be registered which will be informed without any granted order.
   * </p>
   *
   * @param eventConsumer consumer for the timeout event
   */
  @NotNull
  ProbeBuilder<T, R> onTimeout(@NotNull Consumer<WaitTimeoutEvent<T, R>> eventConsumer);

}
