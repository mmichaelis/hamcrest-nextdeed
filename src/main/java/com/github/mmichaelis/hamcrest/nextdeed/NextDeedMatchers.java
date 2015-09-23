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
import com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresConstructor;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassModifierMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.MemberModifierMatcher;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Member;

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

  /**
   * Validates that a declared constructor with the given parameters exists.
   *
   * @param parameterTypes the parameter array
   * @param <T>            the type of the class to check
   * @return matcher
   */
  public static <T extends Class<?>> Matcher<T> declaresConstructor(
      @Nullable Class<?>... parameterTypes) {
    return ClassDeclaresConstructor.declaresConstructor(parameterTypes);
  }

  /**
   * Matcher for modifiers of classes. Modifiers must be exactly as specified.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              the class whose modifiers shall be checked
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @see #classModifierContains(int)
   * @since 1.0.0
   */
  public static <T extends Class<?>> Matcher<T> classModifierIs(int expectedModifier) {
    return ClassModifierMatcher.classModifierIs(expectedModifier);
  }

  /**
   * Matcher for modifiers of classes. All defined modifiers must be set, but there may be more.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              the class whose modifiers shall be checked
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @since 1.0.0
   */
  public static <T extends Class<?>> Matcher<T> classModifierContains(int expectedModifier) {
    return ClassModifierMatcher.classModifierContains(expectedModifier);
  }

  /**
   * Matcher for modifiers of classes. All defined modifiers must be set, but there may be more.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              member type (field, method)
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @since 1.0.0
   */
  public static <T extends Member> Matcher<T> memberModifierContains(int expectedModifier) {
    return MemberModifierMatcher.memberModifierContains(expectedModifier);
  }

  /**
   * Matcher for modifiers of members. Modifiers must be exactly as specified.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              member type (field, method)
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @see #memberModifierContains(int)
   * @since 1.0.0
   */
  public static <T extends Member> Matcher<T> memberModifierIs(int expectedModifier) {
    return MemberModifierMatcher.memberModifierIs(expectedModifier);
  }

}
