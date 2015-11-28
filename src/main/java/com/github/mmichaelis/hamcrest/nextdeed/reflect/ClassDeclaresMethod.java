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

import static com.github.mmichaelis.hamcrest.nextdeed.reflect.Messages.messages;

import com.google.common.base.MoreObjects;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Validates that a class declares a given method possibly with given parameters.
 *
 * @param <T> the type of the class to check
 * @since SINCE
 */
public class ClassDeclaresMethod<T extends Class<?>> extends TypeSafeMatcher<T> {
  private static final Logger LOG = LoggerFactory.getLogger(ClassDeclaresMethod.class);

  @Nullable
  private final Class<?>[] parameterTypes;
  @NotNull
  private final String methodName;

  /**
   * Create matcher validating that a declared constructor with the given parameters exist.
   *
   * @param parameterTypes the parameter array
   * @since SINCE
   */
  public ClassDeclaresMethod(@NotNull String methodName, @Nullable Class<?>... parameterTypes) {
    this.methodName = methodName;
    this.parameterTypes = (parameterTypes == null) ? null : parameterTypes.clone();
  }

  /**
   * Validates that a declared method with the possibly given parameters exists.
   *
   * @param methodName     name of the method
   * @param parameterTypes the parameter array; {@code null} for ignoring parameters and just
   *                       search
   *                       for name
   * @param <T>            the type of the class to check
   * @return matcher
   * @see #declaresMethodWithName(String)
   * @see #declaresNoArgumentsMethod(String)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresMethod(
      @NotNull String methodName,
      @Nullable Class<?>... parameterTypes) {
    return new ClassDeclaresMethod<>(methodName, parameterTypes);
  }

  /**
   * Validates that a declared method with no arguments exists.
   *
   * @param methodName name of the method
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #declaresMethodWithName(String)
   * @see #declaresMethod(String, Class[])
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresNoArgumentsMethod(
      @NotNull String methodName) {
    return new ClassDeclaresMethod<>(methodName);
  }

  /**
   * Validates that a declared method with the given name exists, no matter what parameters the
   * method has.
   *
   * @param methodName name of the method
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #declaresMethod(String, Class[])
   * @see #declaresNoArgumentsMethod(String)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresMethodWithName(
      @NotNull String methodName) {
    return new ClassDeclaresMethod<>(methodName, null);
  }

  @Override
  public void describeTo(Description description) {
    if (parameterTypes == null) {
      description.appendText(messages().declaresMethod(methodName));
    } else {
      description.appendText(messages().declaresMethodWithParameters(methodName, parameterTypes));
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("methodName", methodName)
        .add("parameterTypes", parameterTypes)
        .add("super", super.toString())
        .toString();
  }

  @Override
  protected boolean matchesSafely(T item) {
    if (parameterTypes == null) {
      Method[] declaredMethods = item.getDeclaredMethods();
      for (Method declaredMethod : declaredMethods) {
        if (methodName.equals(declaredMethod.getName())) {
          return true;
        }
      }
      return false;
    } else {
      try {
        item.getDeclaredMethod(methodName, parameterTypes);
      } catch (NoSuchMethodException e) {
        LOG.trace("No match: Class does not declare method {} with parameters of types: {}.", methodName, parameterTypes, e);
        return false;
      }
    }
    return true;
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription
        .appendText(messages().wasClassWithMethods(item, item.getDeclaredMethods()));
  }
}
