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

/**
 * Event when a WaitFunction times out.
 *
 * @since SINCE
 */
public interface WaitTimeoutEvent<T, R> {

  /**
   * Get the source of the timeout event.
   *
   * @return source
   * @since SINCE
   */
  @NotNull
  WaitFunction<T, R> getSource();

  /**
   * Get the consumed milliseconds before the timeout occurred.
   *
   * @return consumed time in milliseconds
   * @since SINCE
   */
  long getConsumedMs();

  /**
   * Get item the delegate function got applied to.
   *
   * @return item; might be {@code null} depending on the input parameters of the function
   * @since SINCE
   */
  T getItem();

  /**
   * Get the last evaluated result which did not match the given
   * predicate.
   *
   * @return result; might be null depending on the delegate function handed over to WaitFunction
   * @since SINCE
   */
  R getLastResult();

  /**
   * Describe the timeout. Convenient for use in exception messages.
   *
   * @return verbose description of the event
   */
  @NotNull
  String describe();
}
