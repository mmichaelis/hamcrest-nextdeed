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

package com.github.mmichaelis.hamcrest.nextdeed.function;

/**
 * <p>
 * Anticipated function from Java 8+/Guava used in Hamcrest context to access an aspect of the
 * matched object
 * for comparison.
 * </p>
 *
 * @param <T> type of value to transform; typically the matched object
 * @param <R> type of the target value to transform to
 * @since SINCE
 */
public interface Function<T, R> {

  /**
   * Applies this function to the given argument.
   *
   * @param input the function argument
   * @return the function result
   * @since SINCE
   */
  R apply(T input);
}
