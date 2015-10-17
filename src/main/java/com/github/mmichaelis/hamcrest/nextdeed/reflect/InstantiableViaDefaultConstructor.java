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

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Validates that the given class is instantiable via default constructor. This means that
 * the default constructor must exist and that it must be able to be called without exceptions.
 * Depending on the security manager protected as well as private default constructors will be
 * found.
 *
 * @param <T> class to validate
 * @since 1.0.0
 * @deprecated Use {@link InstantiableViaConstructor} instead.
 */
@SuppressWarnings({"ThrowableResultOfMethodCallIgnored", "unused", "deprecation"})
@Deprecated
public class InstantiableViaDefaultConstructor<T extends Class<?>>
    extends CustomTypeSafeMatcher<T> {

  private static final ThreadLocal<Exception> INSTANTIATION_EXCEPTION = new ThreadLocal<>();

  public InstantiableViaDefaultConstructor() {
    super("is instantiable via default constructor");
  }

  /**
   * Validates that the given class is instantiable via default constructor. This means that
   * the default constructor must exist and that it must be able to be called without exceptions.
   * Depending on the security manager protected as well as private default constructors will be
   * found.
   *
   * @param <T> class to validate
   * @return matcher
   * @since 1.0.0
   * @deprecated Use {@link InstantiableViaConstructor#isInstantiableWithNoArguments()} instead.
   */
  @NotNull
  @Deprecated
  public static <T extends Class<?>> Matcher<T> isInstantiableViaDefaultConstructor() {
    return new InstantiableViaDefaultConstructor<>();
  }

  @Override
  protected boolean matchesSafely(T item) {
    INSTANTIATION_EXCEPTION.set(null);
    Constructor<?> defaultConstructor;
    try {
      defaultConstructor = item.getDeclaredConstructor();
    } catch (NoSuchMethodException e) {
      INSTANTIATION_EXCEPTION.set(e);
      return false;
    }
    return tryInstantiate(defaultConstructor);
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    mismatchDescription.appendText("instantiation failed");
    Exception exception = INSTANTIATION_EXCEPTION.get();
    if (exception != null) {
      mismatchDescription
          .appendText(" with ")
          .appendText(exception.getClass().getSimpleName())
          .appendText(": ")
          .appendText(exception.getMessage());
    }
  }

  private boolean tryInstantiate(Constructor<?> defaultConstructor) {
    boolean result = true;
    try {
      defaultConstructor.newInstance();
    } catch (InstantiationException | InvocationTargetException e) {
      INSTANTIATION_EXCEPTION.set(e);
      result = false;
    } catch (IllegalAccessException e) {
      INSTANTIATION_EXCEPTION.set(e);
      result = tryInstantiateSettingAccessible(defaultConstructor);
    }

    return result;
  }

  private boolean tryInstantiateSettingAccessible(Constructor<?> defaultConstructor) {
    boolean result = true;
    try {
      defaultConstructor.setAccessible(true);
      try {
        defaultConstructor.newInstance();
        INSTANTIATION_EXCEPTION.set(null);
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
        INSTANTIATION_EXCEPTION.set(e);
        result = false;
      } finally {
        defaultConstructor.setAccessible(false);
      }
    } catch (SecurityException ignored) {
      result = false;
    }
    return result;
  }
}
