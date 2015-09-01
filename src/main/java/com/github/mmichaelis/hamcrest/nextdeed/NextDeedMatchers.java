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

package com.github.mmichaelis.hamcrest.nextdeed;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.function.ApplyingMatcher;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Convenience access to all fabric methods for matchers provided by
 * <em>Hamcrest &mdash; Next Deed</em>.
 * </p>
 *
 * @since 0.1.3
 */
public final class NextDeedMatchers {

  private NextDeedMatchers() {
  }

  /**
   * <p>
   * Applies a transformation to the value before comparing the transformed result with the given
   * matcher.
   * </p>
   *
   * @param function        the function to apply to convert the asserted value to the target value
   * @param delegateMatcher matcher to apply to the transformed value; typically the state of the
   *                        component under test
   * @param <F>             type to input into assertion
   * @param <T>             actual value type to compare
   * @return matcher which transforms input before comparison
   * @since 0.1.3
   */
  public static <F, T> Matcher<F> applying(@NotNull Function<F, T> function,
                                           @NotNull Matcher<? super T> delegateMatcher) {
    return ApplyingMatcher.applying(function, delegateMatcher);
  }

}
