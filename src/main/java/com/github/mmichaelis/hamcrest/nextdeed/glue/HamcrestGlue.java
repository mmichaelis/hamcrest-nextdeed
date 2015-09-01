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

import com.google.common.base.Predicate;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

/**
 * Glue to Hamcrest, for example to transform a matcher into a predicate.
 *
 * @since SINCE
 */
public final class HamcrestGlue {

  private HamcrestGlue() {
    // utility class
  }

  /**
   * Wraps the matcher into a predicate.
   *
   * @param delegateMatcher matcher to wrap as predicate
   * @param <T>             input type the matcher accepts
   * @return wrapping predicate
   * @since SINCE
   */
  @NotNull
  public static <T> Predicate<T> asPredicate(@NotNull final Matcher<T> delegateMatcher) {
    return new Predicate<T>() {
      @Override
      public boolean apply(T input) {
        return delegateMatcher.matches(input);
      }
    };
  }
}
