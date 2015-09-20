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

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Runs matchers on the result of given functions and throws {@link AssertionError} on
 * failure.
 *
 * @param <T> the type of system you are probing
 * @param <R> the type of state variable you are polling
 * @since SINCE
 */
public interface ProbeAssert<T, R> {
  /**
   * Assert that the result of the function applied to the probed object matches the
   * requirements.
   *
   * @param actualFunction function to get a value from the probed object
   * @param matcher        matcher to apply to result of function
   * @throws AssertionError if there is no match in the given time; thus the test will be marked
   *                        as <strong>Failure</strong>
   */
  void assertThat(@NotNull Function<T, R> actualFunction,
                  @NotNull Matcher<? super R> matcher);

  /**
   * Assert that the result of the function applied to the probed object matches the
   * requirements.
   *
   * @param reason         message on failure
   * @param actualFunction function to get a value from the probed object
   * @param matcher        matcher to apply to result of function
   * @throws AssertionError if there is no match in the given time; thus the test will be marked
   *                        as <strong>Failure</strong>
   */
  void assertThat(@Nullable String reason,
                  @NotNull Function<T, R> actualFunction,
                  @NotNull Matcher<? super R> matcher);

}
