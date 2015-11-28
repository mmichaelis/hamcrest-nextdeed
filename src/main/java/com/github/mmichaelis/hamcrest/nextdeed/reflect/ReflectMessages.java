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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Messages for the {@code reflect} package.
 *
 * @since SINCE
 */
interface ReflectMessages {

  @NotNull
  String constructorWithParametersNotAvailable(@Nullable Class<?>[] parameterTypes);

  @NotNull
  String cannotInstantiateWithConstructor(@Nullable Class<?>[] parameterTypes, Throwable e);

  @NotNull
  String declaresMethod(@NotNull String methodName);

  @NotNull
  String declaresMethodWithParameters(@NotNull String methodName, @NotNull Class<?>[] parameterTypes);

  @NotNull
  <T extends Class<?>> String wasClassWithMethods(@NotNull T item, @NotNull Method[] declaredMethods);

  @NotNull
  String isInstantiableWithParameters(int parameterCount, @Nullable Object[] parameters);
}
