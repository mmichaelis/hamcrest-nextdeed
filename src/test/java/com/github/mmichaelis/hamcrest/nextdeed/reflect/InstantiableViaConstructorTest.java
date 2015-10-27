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
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaConstructor.isInstantiableWith;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaConstructor.isInstantiableWithNoArguments;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import com.google.common.base.Supplier;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;

import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;

/**
 * Tests {@link InstantiableViaConstructor}.
 *
 * @since SINCE
 */
public class InstantiableViaConstructorTest {

  @Test
  public void isInstantiableWithNoArguments_OnlyDefaultonstructor() throws Exception {
    Matcher<Class<?>> matcher = isInstantiableWithNoArguments();
    assertThat(OnlyDefaultonstructor.class, matcher);
  }

  @Test
  public void isInstantiableWith_acceptingNullParameter_OnlyDefaultonstructor() throws Exception {
    Matcher<Class<?>> matcher = isInstantiableWith((Object[]) null);
    assertThat(OnlyDefaultonstructor.class, matcher);
  }

  @Test
  public void isInstantiableWithNoArguments_SeveralConstructorsIncludingDefault() throws Exception {
    Matcher<Class<?>> matcher = isInstantiableWithNoArguments();
    assertThat(SeveralConstructorsIncludingDefault.class, matcher);
  }

  @Test
  public void isInstantiableWith_SeveralConstructorsIncludingDefault() throws Exception {
    Matcher<Class<?>> matcher = isInstantiableWith("someString");
    assertThat(SeveralConstructorsIncludingDefault.class, matcher);
  }

  @Test
  public void isInstantiableWith_NoDefaultConstructor() throws Exception {
    Matcher<Class<?>> matcher = isInstantiableWith("someString");
    assertThat(NoDefaultConstructor.class, matcher);
  }

  @Test
  public void hasRelevantInformationInToString() throws Exception {
    String argumentValue = "someString";
    Matcher<Class<?>> matcher = isInstantiableWith(argumentValue);
    assertThat(matcher,
               hasToString(
                   allOf(
                       stringContainsInOrder("parameters", argumentValue),
                       stringContainsInOrder("parameterTypes", argumentValue.getClass().getName())
                   )
               )
    );
  }

  @Test
  public void not_isInstantiableWith_OnlyDefaultonstructor() throws Exception {
    final Class<?> classUnderTest = OnlyDefaultonstructor.class;
    String parameterValue = "someString";
    final Object[] parameters = new Object[]{parameterValue};

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    Matcher<Class<?>> matcher = isInstantiableWith(parameters);
                    assertThat(classUnderTest, matcher);
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
                       "isInstantiableWithParameters",
                       parameterValue,
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "constructorWithParametersNotAvailable",
                       String.class.toString()
                   )
               )
    );
  }

  @Test
  public void not_isInstantiableWith_ConstructorsThrowingExceptions() throws Exception {
    final Class<?> classUnderTest = ConstructorsThrowingExceptions.class;
    String parameterValue = "someString";
    final Object[] parameters = new Object[]{parameterValue};

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    Matcher<Class<?>> matcher = isInstantiableWith(parameters);
                    assertThat(classUnderTest, matcher);
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
                       "isInstantiableWithParameters",
                       parameterValue,
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "cannotInstantiateWithConstructor",
                       String.class.toString(),
                       InvocationTargetException.class.getName()
                   )
               )
    );
  }

  @Test
  public void not_isInstantiableWithNoArguments_NoDefaultConstructor() throws Exception {
    final Class<?> classUnderTest = NoDefaultConstructor.class;

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    Matcher<Class<?>> matcher = isInstantiableWithNoArguments();
                    assertThat(classUnderTest, matcher);
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
                       "isInstantiableWithParameters",
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "constructorWithParametersNotAvailable"
                   )
               )
    );
  }

  @Test
  public void not_isInstantiableWithNoArguments_ConstructorsThrowingExceptions() throws Exception {
    final Class<?> classUnderTest = ConstructorsThrowingExceptions.class;

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ReflectMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    Matcher<Class<?>> matcher = isInstantiableWithNoArguments();
                    assertThat(classUnderTest, matcher);
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
                       "isInstantiableWithParameters",
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       "cannotInstantiateWithConstructor",
                       InvocationTargetException.class.getName()
                   )
               )
    );
  }

  private static final class OnlyDefaultonstructor {

  }

  private static final class SeveralConstructorsIncludingDefault {

    public SeveralConstructorsIncludingDefault() {
    }

    public SeveralConstructorsIncludingDefault(String someArg) {
    }
  }

  private static final class NoDefaultConstructor {

    public NoDefaultConstructor(String someArg) {
    }
  }

  private static final class ConstructorsThrowingExceptions {

    public ConstructorsThrowingExceptions() {
      throw new IllegalStateException("probe exception");
    }

    public ConstructorsThrowingExceptions(String someArg) {
      throw new IllegalStateException("probe exception");
    }
  }
}
