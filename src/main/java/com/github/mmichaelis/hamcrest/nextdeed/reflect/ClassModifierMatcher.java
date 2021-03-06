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
import org.jetbrains.annotations.NotNull;

/**
 * Matcher for modifiers of classes.
 *
 * @param <T> the class whose modifiers shall be checked
 * @see java.lang.reflect.Modifier
 * @since 1.0.0
 */
public class ClassModifierMatcher<T extends Class<?>> extends ModifierMatcherBase<T> {

  /**
   * Creates a matcher for class modifiers.
   *
   * @param expectedModifier modifiers to validate
   * @param strict           if {@code true}, all given modifiers must be set, if {@code false} at
   *                         least these modifiers must be set
   */
  public ClassModifierMatcher(int expectedModifier, boolean strict) {
    super(expectedModifier, "class", strict);
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
  @NotNull
  public static <T extends Class<?>> Matcher<T> classModifierIs(int expectedModifier) {
    return new ClassModifierMatcher<>(expectedModifier, true);
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
  @NotNull
  public static <T extends Class<?>> Matcher<T> classModifierContains(int expectedModifier) {
    return new ClassModifierMatcher<>(expectedModifier, false);
  }

  @Override
  protected int getModifiers(T item) {
    return item.getModifiers();
  }

}
