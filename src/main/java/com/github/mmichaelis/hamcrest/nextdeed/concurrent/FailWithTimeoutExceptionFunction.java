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

import org.jetbrains.annotations.Nullable;

/**
 * Default strategy for {@link WaitFunction} to fail with {@link WaitTimeoutException} on timeout.
 *
 * @since 1.0.0
 */
final class FailWithTimeoutExceptionFunction<T, R>
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
