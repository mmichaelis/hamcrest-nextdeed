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

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Interface for any wait-related builders. Using this interface you might forward
 * any configuration options directly to a {@code WaitFunction}.
 *
 * @since 1.0.0
 */
public interface WaitBuilder {

  /**
   * <p>
   * How much time it might take to fulfill predicate. Otherwise time out.
   * </p>
   * <p>
   * Mind that the timeout is not hard regarding the system response time. If your
   * timeout is 100&nbsp;ms but the system takes 1000&nbsp;ms to answer and assuming
   * the state is reached, the call is still successful. In other words: This is not
   * to guarantee certain time measures &ndash; but to give some grace to the system
   * to reach the wanted state.
   * </p>
   *
   * @param timeoutMs timeout in milliseconds; must be greater than or equal to 0
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder withinMs(long timeoutMs);

  /**
   * <p>
   * How much time it might take to fulfill predicate. Otherwise time out.
   * </p>
   * <p>
   * Mind that the timeout is not hard regarding the system response time. If your
   * timeout is 100&nbsp;ms but the system takes 1000&nbsp;ms to answer and assuming
   * the state is reached, the call is still successful. In other words: This is not
   * to guarantee certain time measures &ndash; but to give some grace to the system
   * to reach the wanted state.
   * </p>
   *
   * @param timeout  timeout amount; must be greater than or equal to 0
   * @param timeUnit timeout time unit
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder within(long timeout, @NotNull TimeUnit timeUnit);

  /**
   * <p>
   * Configures the grace period to grant after the timeout is reached.
   * </p>
   * <dl>
   * <dt><strong>Example:</strong></dt>
   * <dd>
   * <p>
   * Your system responds within 100&nbsp;ms and you start with an initial delay at 0&nbsp;ms.
   * Having a timeout of 140&nbsp;ms the remaining sleep time after first failure is 40&nbsp;ms.
   * But giving a grace of 50&nbsp;ms will cause the last sleep to take 90&nbsp;ms thus giving
   * the system more chances to reach the wanted state.
   * </p>
   * </dd>
   * </dl>
   *
   * @param gracePeriodMs grace period in milliseconds; must be greater than or equal to 0
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder withFinalGracePeriodMs(long gracePeriodMs);

  /**
   * <p>
   * Configures the grace period to grant after the timeout is reached.
   * </p>
   * <dl>
   * <dt><strong>Example:</strong></dt>
   * <dd>
   * <p>
   * Your system responds within 100&nbsp;ms and you start with an initial delay at 0&nbsp;ms.
   * Having a timeout of 140&nbsp;ms the remaining sleep time after first failure is 40&nbsp;ms.
   * But giving a grace of 50&nbsp;ms will cause the last sleep to take 90&nbsp;ms thus giving
   * the system more chances to reach the wanted state.
   * </p>
   * </dd>
   * </dl>
   *
   * @param gracePeriod grace period; must be greater than or equal to 0
   * @param timeUnit    time unit of grace period
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder withFinalGracePeriod(long gracePeriod, @NotNull TimeUnit timeUnit);

  /**
   * <p>
   * The initial delay and also base delay for polling. The default initial delay is
   * 0 milliseconds.
   * </p>
   * <dl>
   * <dt><strong>Note:</strong></dt>
   * <dd>As the polling will never increase but only decrease the initial delay
   * is also the minimum polling interval. It might be used, if you know that
   * your system for example takes at least two seconds to recover from a request.
   * </dd>
   * </dl>
   *
   * @param initialDelayMs initial delay in milliseconds; must be greater than or equal to 0
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder withInitialDelayMs(long initialDelayMs);

  /**
   * <p>
   * The initial delay and also base delay for polling. The default initial delay is
   * 0 milliseconds.
   * </p>
   * <dl>
   * <dt><strong>Note:</strong></dt>
   * <dd>As the polling will never increase but only decrease the initial delay
   * is also the minimum polling interval. It might be used, if you know that
   * your system for example takes at least two seconds to recover from a request.
   * </dd>
   * </dl>
   *
   * @param initialDelay initial delay; must be greater than or equal to 0
   * @param timeUnit     time unit of initial delay
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder withInitialDelay(long initialDelay, @NotNull TimeUnit timeUnit);

  /**
   * Factor by which the polling interval is decelerated. Greater values will decelerate more,
   * a value of 1 won't decelerate at all but keep the polling interval.
   *
   * @param decelerationFactor factor to decelerate; must be greater than or equal to 1
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder deceleratePollingBy(double decelerationFactor);

  /**
   * <p>
   * Syntactic sugar for the builder &mdash; not more, not less.
   * </p>
   *
   * @return self-reference
   * @since 1.0.0
   */
  @NotNull
  WaitBuilder and();
}
