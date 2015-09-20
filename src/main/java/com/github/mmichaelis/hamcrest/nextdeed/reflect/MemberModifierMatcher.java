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

package com.github.mmichaelis.hamcrest.nextdeed.reflect;

import org.hamcrest.Matcher;

import java.lang.reflect.Member;

/**
 * Matcher for modifiers of class members such as constructors, methods and fields.
 *
 * @see java.lang.reflect.Modifier
 * @since SINCE
 */
public class MemberModifierMatcher<T extends Member> extends ModifierMatcherBase<T> {

  /**
   * Creates a matcher for member modifiers.
   *
   * @param expectedModifier modifiers to validate
   * @param strict           if {@code true}, all given modifiers must be set, if {@code false} at
   *                         least these modifiers must be set
   */
  public MemberModifierMatcher(int expectedModifier, boolean strict) {
    super(expectedModifier, "member", strict);
  }

  /**
   * Matcher for modifiers of members. Modifiers must be exactly as specified.
   *
   * @see java.lang.reflect.Modifier
   * @see #memberModifierContains(int)
   * @since SINCE
   */
  public static <T extends Member> Matcher<T> memberModifierIs(int expectedModifier) {
    return new MemberModifierMatcher<>(expectedModifier, true);
  }

  /**
   * Matcher for modifiers of classes. All defined modifiers must be set, but there may be more.
   *
   * @see java.lang.reflect.Modifier
   * @since SINCE
   */
  public static <T extends Member> Matcher<T> memberModifierContains(int expectedModifier) {
    return new MemberModifierMatcher<>(expectedModifier, false);
  }

  @Override
  protected int getModifiers(T item) {
    return item.getModifiers();
  }

}
