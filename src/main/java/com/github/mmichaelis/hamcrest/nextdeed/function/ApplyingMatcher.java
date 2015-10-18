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

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;

/**
 * <p>
 * Matcher which first transforms the matched object and hands over the comparison to the
 * delegate matcher.
 * </p>
 * <dl>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * In order not to report a different value on failure than used for comparison the previously
 * transformed value is stored per thread when matching is tried. As a consequence
 * {@link #describeMismatchSafely(Object, Description)} ignores the item expecting that it did
 * not change meanwhile.
 * </dd>
 * </dl>
 *
 * @param <F> type of the actual value in the assertion; typically the component under test
 * @param <T> type of the actual value to compare; typically the state of the component under test
 * @since 0.1.0
 */
public class ApplyingMatcher<F, T> extends TypeSafeMatcher<F> {

  @NotNull
  private final Reference<T> lastValueReference = new ReferenceImpl<>();

  @NotNull
  private final Function<F, T> function;

  @NotNull
  private final Matcher<? super T> delegateMatcher;

  /**
   * <p>
   * Constructor.
   * </p>
   *
   * @param function        the function to apply to convert the matched object to the target value
   * @param delegateMatcher matcher to apply to the transformed value
   * @since 0.1.0
   */
  public ApplyingMatcher(@NotNull Function<F, T> function,
                         @NotNull Matcher<? super T> delegateMatcher) {
    this.function = requireNonNull(function, "function must not be null.");
    this.delegateMatcher = requireNonNull(delegateMatcher, "matcher must not be null.");
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
   * @since 0.1.0
   */
  public static <F, T> Matcher<F> applying(@NotNull Function<F, T> function,
                                           @NotNull Matcher<? super T> delegateMatcher) {
    return new ApplyingMatcher<>(function, delegateMatcher);
  }

  @Override
  public void describeTo(@NotNull Description description) {
    delegateMatcher.describeTo(description);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("super", super.toString())
        .add("delegateMatcher", delegateMatcher)
        .add("function", function)
        .add("lastValueReference", lastValueReference)
        .toString();
  }

  @Override
  protected boolean matchesSafely(F item) {
    synchronized (lastValueReference) {
      return delegateMatcher.matches(lastValueReference.set(function.apply(item)));
    }
  }

  @Override
  protected void describeMismatchSafely(F item, @NotNull Description mismatchDescription) {
    boolean recalculationRequired = false;
    T actualValue = null;
    synchronized (lastValueReference) {
      if (lastValueReference.isSet()) {
        actualValue = lastValueReference.remove();
      } else {
        recalculationRequired = true;
      }
    }
    if (recalculationRequired) {
      actualValue = function.apply(item);
    }

    delegateMatcher.describeMismatch(actualValue, mismatchDescription);
  }
}
