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

package com.github.mmichaelis.hamcrest.nextdeed.glue;

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;

import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * A function which can be described.
 * </p>
 * <dl>
 * <dt><strong>Usage:</strong></dt>
 * <dd>
 * <pre>{@code
 * DescribedFunction.describe(fn).as("description");
 * }</pre>
 * </dd>
 * </dl>
 *
 * @param <T> input type of function
 * @param <R> output type of function
 * @since 1.0.0
 */
public class DescribedFunction<T, R> implements Function<T, R> {

  /**
   * The function to describe.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Function<T, R> delegateFunction;
  /**
   * The description of the function.
   *
   * @since 1.0.0
   */
  @NotNull
  private final String description;

  /**
   * Constructor for the described function.
   *
   * @param delegateFunction function to describe (and to forward any requests to)
   * @param description      description to use
   * @see #describe(Function)
   * @since 1.0.0
   */
  protected DescribedFunction(@NotNull Function<T, R> delegateFunction,
                              @NotNull String description) {
    this.delegateFunction = requireNonNull(delegateFunction, "delegateFunction must not be null.");
    this.description = requireNonNull(description, "description must not be null.");
  }

  /**
   * Build described function.
   *
   * @param delegateFunction function to describe
   * @param <T>              input type of function
   * @param <R>              output type of function
   * @return builder for described function
   * @since 1.0.0
   */
  @NotNull
  public static <T, R> Builder<T, R> describe(@NotNull final Function<T, R> delegateFunction) {
    requireNonNull(delegateFunction, "delegateFunction must not be null.");
    return new Builder<T, R>() {
      @NotNull
      @Override
      public Function<T, R> as(@NotNull String description) {
        return new DescribedFunction<>(delegateFunction, description);
      }
    };
  }

  /**
   * {@inheritDoc}
   * <p>
   * Redirects any calls to the delegate function.
   * </p>
   *
   * @since 1.0.0
   */
  @Override
  public R apply(T input) {
    return delegateFunction.apply(input);
  }

  /**
   * Actually provides the description for the delegate function.
   *
   * @return description
   * @since 1.0.0
   */
  @Override
  public String toString() {
    return description;
  }

  /**
   * Fluent builder interface for creating described functions.
   *
   * @param <T> input type of described function
   * @param <R> output type of described function
   * @since 1.0.0
   */
  public interface Builder<T, R> {

    /**
     * Define the description to use.
     *
     * @param description description for the function
     * @return describing function wrapper
     * @since 1.0.0
     */
    @NotNull
    Function<T, R> as(@NotNull String description);
  }
}
