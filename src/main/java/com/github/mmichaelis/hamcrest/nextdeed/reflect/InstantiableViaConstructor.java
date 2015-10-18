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
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.google.common.reflect.Invokable;

import com.github.mmichaelis.hamcrest.nextdeed.base.Issue;
import com.github.mmichaelis.hamcrest.nextdeed.base.IssuesMatcher;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Validates that a class declares a constructor with the given parameters and can be
 * instantiated by it.
 *
 * @param <T> the type of the class to check
 * @since SINCE
 */
public class InstantiableViaConstructor<T extends Class<?>> extends IssuesMatcher<T> {

  @Nullable
  private final Object[] parameters;
  @Nullable
  private final Class<?>[] parameterTypes;

  /**
   * Create matcher validating that a declared constructor with the given parameters exist and
   * can be instantiated.
   *
   * @param parameters the parameter array; must not contain {@code null} values as otherwise the
   *                   type cannot be guessed
   * @since SINCE
   */
  public InstantiableViaConstructor(@Nullable Object... parameters) {
    super(((parameters == null) || (parameters.length == 0)) ? "is instantiable with no parameters"
                                                             : "is instantiable with parameters: ",
          Arrays.toString(parameters));
    if (parameters != null) {
      this.parameters = parameters.clone();
      Collection<Class<?>> types = new ArrayList<>(parameters.length);
      for (Object parameter : parameters) {
        requireNonNull(parameter, "Parameters must not be null.");
        types.add(parameter.getClass());
      }
      parameterTypes = Iterables.toArray(types, Class.class);
    } else {
      this.parameters = null;
      parameterTypes = null;
    }
  }

  /**
   * Validates that a declared constructor with the given parameters exists and can be
   * instantiated.
   *
   * @param parameters the parameter array
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #isInstantiableWithNoArguments()
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isInstantiableWith(
      @Nullable Object... parameters) {
    return new InstantiableViaConstructor<>(parameters);
  }

  /**
   * Validates that a declared constructor with no arguments exists and can be
   * instantiated.
   *
   * @param <T> the type of the class to check
   * @return matcher
   * @see #isInstantiableWith(Object...)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isInstantiableWithNoArguments() {
    return new InstantiableViaConstructor<>();
  }

  @Override
  protected void validate(@NotNull T item, @NotNull Collection<Issue> issues) {
    Constructor<?> constructor;
    try {
      constructor = item.getDeclaredConstructor(parameterTypes);
    } catch (NoSuchMethodException e) {
      issues.add(issue(messages().constructorWithParametersNotAvailable(parameterTypes)));
      return;
    }
    try {
      Invokable.from(constructor).setAccessible(true);
      constructor.newInstance(parameters);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(issue(messages().cannotInstantiateWithConstructor(parameterTypes, e)));
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("parameters", (parameters == null) ? null : Arrays.asList(parameters))
        .add("parameterTypes", (parameterTypes == null) ? null : Arrays.asList(parameterTypes))
        .add("super", super.toString())
        .toString();
  }

}
