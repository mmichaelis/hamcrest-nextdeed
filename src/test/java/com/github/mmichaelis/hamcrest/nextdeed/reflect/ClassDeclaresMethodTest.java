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

import static com.github.mmichaelis.hamcrest.nextdeed.base.MessagesProxyProvider.withRawMessages;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresMethod.declaresMethod;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresMethod.declaresNoArgumentsMethod;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;

import com.google.common.base.Supplier;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * Tests {@link ClassDeclaresMethod}.
 *
 * @since SINCE
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
@RunWith(Parameterized.class)
public class ClassDeclaresMethodTest {

  @NotNull
  private final String methodName;
  @Nullable
  private final Class<?>[] parameterTypes;

  public ClassDeclaresMethodTest(@NotNull String methodName, @Nullable Class<?>... parameterTypes) {
    this.methodName = methodName;
    this.parameterTypes =
        (parameterTypes == null) ? null : Arrays.copyOf(parameterTypes, parameterTypes.length);
  }

  @Test
  public void declaresMethodWithName_passForExistingMethod() throws Exception {
    Matcher<Class<?>> matcher = ClassDeclaresMethod.declaresMethodWithName(methodName);
    assertThat(InspectedClass.class, matcher);
  }

  @Test
  public void declaresMethodWithName_failsForNonExistingMethod() throws Exception {
    String testedMethodName = "nonExisting_" + methodName;
    final Matcher<Class<?>> matcher = ClassDeclaresMethod.declaresMethodWithName(
        testedMethodName);

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(InspectedClass.class, matcher);
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               allOf(
                   stringContainsInOrder(
                       "Expected:",
                       "declaresMethod",
                       testedMethodName,
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "wasClassWithMethods",
                       methodName
                   )
               )
    );
  }

  @Test
  public void declaresMethod_passForExistingMethod() throws Exception {
    Matcher<Class<?>> matcher = declaresMethod(methodName, parameterTypes);
    assertThat(InspectedClass.class, matcher);
  }

  @Test
  public void declaresMethod_failsForNonExistingMethod() throws Exception {
    String testedMethodName = "nonExisting_" + methodName;
    final Matcher<Class<?>> matcher = declaresMethod(testedMethodName, parameterTypes);

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(InspectedClass.class, matcher);
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               allOf(
                   stringContainsInOrder(
                       "Expected:",
                       "declaresMethod",
                       testedMethodName,
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "wasClassWithMethods",
                       methodName
                   )
               )
    );
  }

  @Test
  public void declaresMethod_failsForUnmatchedParameters() throws Exception {
    String testedMethodName = methodName;
    Class<Math> parameterClass = Math.class;
    final Matcher<Class<?>> matcher = declaresMethod(testedMethodName, parameterClass);

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(InspectedClass.class, matcher);
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               allOf(
                   stringContainsInOrder(
                       "Expected:",
                       "declaresMethodWithParameters",
                       testedMethodName,
                       parameterClass.toString(),
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "wasClassWithMethods",
                       methodName
                   )
               )
    );
  }

  @Test
  public void declaresNoArgumentsMethod_passForExistingMethod() throws Exception {
    assumeThat("Test only applicable for methods with no arguments.", parameterTypes,
               nullValue());

    Matcher<Class<?>> matcher = declaresNoArgumentsMethod(methodName);
    assertThat(InspectedClass.class, matcher);
  }

  @Test
  public void declaresNoArgumentsMethod_failsForNonExistingMethod() throws Exception {
    assumeThat("Test only applicable for methods with no arguments.", parameterTypes,
               nullValue());

    String testedMethodName = "nonExisting_" + methodName;
    final Matcher<Class<?>> matcher = declaresNoArgumentsMethod(testedMethodName);

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(InspectedClass.class, matcher);
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               allOf(
                   stringContainsInOrder(
                       "Expected:",
                       "declaresMethod",
                       testedMethodName,
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "wasClassWithMethods",
                       methodName
                   )
               )
    );
  }

  @Test
  public void providesRelevantInformationInToString() throws Exception {
    assertThat(declaresMethod(methodName, parameterTypes), Matchers.hasToString(
        allOf(
            stringContainsInOrder("methodName", methodName),
            stringContainsInOrder("parameterTypes", Arrays.toString(parameterTypes))
        )
    ));

  }

  @Parameters(name = "{index}: {0}, parameters: {1}")
  public static Collection<Object[]> data() {

    return Arrays.asList(new Object[][]{
        {"noArgVoid", null},
        {"noArgValue", null},
        {"varargsMethod", new Class<?>[]{String[].class}},
        {"toString", null},
    });
  }

  @SuppressWarnings("unused")
  private static final class InspectedClass {

    public void noArgVoid() {
    }

    public String noArgValue() {
      return null;
    }

    public void varargsMethod(String... strings) {
    }

    @Override
    public String toString() {
      return "that's me";
    }
  }
}
