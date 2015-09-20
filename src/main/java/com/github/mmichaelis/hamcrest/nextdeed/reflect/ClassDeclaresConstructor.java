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

import com.google.common.base.MoreObjects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * Validates that a class declares a constructor with the given parameters.
 *
 * @param <T> the type of the class to check
 * @since SINCE
 */
public class ClassDeclaresConstructor<T extends Class<?>> extends TypeSafeMatcher<T> {

  @Nullable
  private final Class<?>[] parameterTypes;

  /**
   * Create matcher validating that a declared constructor with the given parameters exist.
   *
   * @param parameterTypes the parameter array
   */
  public ClassDeclaresConstructor(@Nullable Class<?>... parameterTypes) {
    this.parameterTypes = (parameterTypes == null) ? null : parameterTypes.clone();
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
    return new ClassDeclaresConstructor<>(parameterTypes);
  }

  @Override
  public void describeTo(Description description) {
    description.appendText("declares constructors with parameters: ").appendValue(parameterTypes);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("parameterTypes", (parameterTypes == null) ? null : Arrays.asList(parameterTypes))
        .add("super", super.toString())
        .toString();
  }

  @Override
  protected boolean matchesSafely(T item) {
    try {
      item.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException e) {
      return false;
    }
    return true;
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription.appendText("was ")
        .appendValue(item)
        .appendText(" with constructors ")
        .appendValue(item.getDeclaredConstructors());
  }
}
