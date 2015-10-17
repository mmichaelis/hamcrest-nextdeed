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

package com.github.mmichaelis.hamcrest.nextdeed.exception;

import static com.github.mmichaelis.hamcrest.nextdeed.base.BaseMessages.withRawMessages;
import static com.github.mmichaelis.hamcrest.nextdeed.exception.IsJavaCompliantExceptionMatcher.isJavaCompliantException;
import static com.github.mmichaelis.hamcrest.nextdeed.exception.JavaComplianceLevel.JAVA_1_1;
import static com.github.mmichaelis.hamcrest.nextdeed.exception.JavaComplianceLevel.JAVA_1_4;
import static com.github.mmichaelis.hamcrest.nextdeed.exception.JavaComplianceLevel.JAVA_1_7;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import com.google.common.base.Supplier;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.concurrent.Callable;

/**
 * Tests {@link IsJavaCompliantExceptionMatcher}.
 *
 * @since SINCE
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
public class IsJavaCompliantExceptionMatcherTest {

  @Test
  public void standardJavaExceptionIsJava_1_1_compliant() throws Exception {
    assertThat("RuntimeException should be regarded as Java 1.1 compliant.",
               RuntimeException.class,
               isJavaCompliantException(JAVA_1_1));
  }

  @Test
  public void standardJavaExceptionIsJava_1_4_compliant() throws Exception {
    assertThat("RuntimeException should be regarded as Java 1.4 compliant.",
               RuntimeException.class,
               isJavaCompliantException(JAVA_1_4));
  }

  @Test
  public void standardJavaExceptionIsJava_1_7_compliant() throws Exception {
    assertThat("RuntimeException should be regarded as Java 1.7 compliant.",
               RuntimeException.class,
               isJavaCompliantException(JAVA_1_7));
  }

  /**
   * That null is not compliant is actually covered by the TypeSafeMatcher which only forwards to
   * the extended matcher if the value is different to null.
   */
  @Test
  public void nullIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "null should not be regarded as compliant exception.",
                        null,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.", errorMessage,
               stringContainsInOrder(JAVA_1_7.getJavaName(), "null"));
  }

  @Test
  public void simpleStringIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "String should not be regarded as compliant exception.",
                        String.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.", errorMessage,
               stringContainsInOrder(JAVA_1_7.getJavaName(), Throwable.class.toString()));
  }

  @Test
  public void exceptionWithoutDefaultConstructorIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat("Exceptions without default constructor are not compliant.",
                               StrangeConstructorsException.class,
                               isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no default constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "noDefaultConstructor"));
  }

  @Test
  public void exceptionWithoutMessageConstructorIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat("Exceptions without message constructor are not compliant.",
                               StrangeConstructorsException.class,
                               isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no message constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "noMessageConstructor"));
  }

  @Test
  public void exceptionWithoutCauseConstructorIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat("Exceptions without cause constructor are not compliant.",
                               StrangeConstructorsException.class,
                               isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no cause constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "noCauseConstructor"));
  }

  @Test
  public void exceptionWithoutMessageCauseConstructorIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat("Exceptions without message-cause constructor are not compliant.",
                               StrangeConstructorsException.class,
                               isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no message-cause constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "noMessageCauseConstructor"));
  }

  @Test
  public void exceptionWithoutSuppressionEnabledConstructorIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions without protected suppression-enabled constructor are not compliant.",
                        StrangeConstructorsException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no suppression-enabled constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "noSuppressionEnabledConstructor"));
  }

  @Test
  public void exceptionSuppressionEnabledConstructorNotProtectedIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions without protected suppression-enabled constructor are not compliant.",
                        PublicSuppressionEnabledConstructor.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that no suppression-enabled constructor could be found (raw message).",
        errorMessage,
        stringContainsInOrder("Expected:",
                              "compliantException",
                              JAVA_1_7.getJavaName(),
                              "but:",
                              "suppressionEnabledConstructorIsNotProtected"));
  }

  @Test
  public void exceptionModifyingOrIgnoringMessageIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions ignoring message are not compliant.",
                        IgnoringMessageException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exceptions ignores the set message.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "messageNotAsExpected",
                "messageConstructor",
                "null"
            )
        )
    );
  }

  @Test
  public void exceptionModifyingOrIgnoringCauseIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions ignoring cause are not compliant.",
                        IgnoringCauseException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exceptions ignores the set cause.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "messageNotAsExpected",
                "causeConstructor",
                "null"
            ),
            stringContainsInOrder(
                "but:",
                "causeNotAsExpected",
                "causeConstructor",
                "null"
            )
        )
    );
  }

  @Test
  public void exceptionModifyingOrIgnoringMessageAndCauseIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions ignoring message and cause are not compliant.",
                        IgnoringMessageCauseException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exceptions ignores the set message and cause.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "messageNotAsExpected",
                "messageCauseConstructor",
                "null"
            ),
            stringContainsInOrder(
                "but:",
                "causeNotAsExpected",
                "messageCauseConstructor",
                "null"
            )
        )
    );
  }

  @Test
  public void exceptionInitializingCauseWithNullIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions initializing cause with null is not compliant.",
                        InvalidCauseNullInitializationException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exception errornously initializes the cause with null.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "initCauseFailureNull",
                "defaultConstructor"
            )
        )
    );
  }

  @Test
  public void exceptionInitializingForeignCauseIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions initializing cause some foreign exception is not compliant.",
                        InvalidForeignCauseInitializationException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exception errornously initializes the cause with a foreign exception.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "causeNotAsExpected",
                "defaultConstructor",
                "null",
                "IllegalStateException"
            ),
            stringContainsInOrder(
                "but:",
                "initCauseFailureNotNull",
                "defaultConstructor"
            )
        )
    );
  }

  @Test
  public void exceptionDealingWrongWithSuppressionFlagsIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions not dealing correctly with suppression flags is not compliant.",
                        IgnoringSuppressionFlagsException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exception deals wrong with the suppression flags.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "writableStacktraceNotDisabled",
                "suppressionEnabledConstructor"
            ),
            stringContainsInOrder(
                "but:",
                "suppressionNotDisabled",
                "suppressionEnabledConstructor"
            )
        )
    );
  }

  @Test
  public void exceptionFailingToBuildIsNotCompliant() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ExceptionMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(
                        "Exceptions with failures during instantiation are not compliant",
                        ConstructorFailuresException.class,
                        isJavaCompliantException(JAVA_1_7));
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat(
        "Should have failed denoting that the analyzed exception has problems on initialization.",
        errorMessage,
        Matchers.allOf(
            stringContainsInOrder(
                "Expected:",
                "compliantException",
                JAVA_1_7.getJavaName(),
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "cannotInstantiateVia",
                "defaultConstructor",
                "InvocationTargetException",
                ConstructorFailuresException.PROVOKED_FAILURE
            ),
            stringContainsInOrder(
                "but:",
                "cannotInstantiateVia",
                "causeConstructor",
                "InvocationTargetException",
                ConstructorFailuresException.PROVOKED_FAILURE
            ),
            stringContainsInOrder(
                "but:",
                "cannotInstantiateVia",
                "suppressionEnabledConstructor",
                "InvocationTargetException",
                ConstructorFailuresException.PROVOKED_FAILURE
            ),
            stringContainsInOrder(
                "but:",
                "cannotInstantiateVia",
                "messageCauseConstructor",
                "InvocationTargetException",
                ConstructorFailuresException.PROVOKED_FAILURE
            ),
            stringContainsInOrder(
                "but:",
                "cannotInstantiateVia",
                "messageConstructor",
                "InvocationTargetException",
                ConstructorFailuresException.PROVOKED_FAILURE
            )
        )
    );
  }

  @Test
  public void hasToString() throws Exception {
    assertThat("Provides relevant information in toString().",
               isJavaCompliantException(JAVA_1_1),
               Matchers.hasToString(Matchers.allOf(
                   containsString("level"),
                   containsString(String.valueOf(JAVA_1_1)),
                   containsString(IsJavaCompliantExceptionMatcher.class.getSimpleName())
               )));
  }

  private static final class StrangeConstructorsException extends RuntimeException {

    private static final long serialVersionUID = -3457451245728193682L;

    public StrangeConstructorsException(int i) {
      super(Integer.toString(i));
    }
  }

  private static final class PublicSuppressionEnabledConstructor extends RuntimeException {

    private static final long serialVersionUID = 526401132541163503L;

    public PublicSuppressionEnabledConstructor(String message, Throwable cause,
                                               boolean enableSuppression,
                                               boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
    }
  }

  @SuppressWarnings("UnusedParameters")
  private static final class IgnoringMessageException extends RuntimeException {

    private static final long serialVersionUID = -9156738501806793510L;

    public IgnoringMessageException(String message) {
    }
  }

  @SuppressWarnings("UnusedParameters")
  private static final class IgnoringCauseException extends RuntimeException {

    private static final long serialVersionUID = 4671042650829301594L;

    public IgnoringCauseException(Throwable cause) {
    }
  }

  @SuppressWarnings("UnusedParameters")
  private static final class IgnoringMessageCauseException extends RuntimeException {

    private static final long serialVersionUID = -8253331886707724445L;

    public IgnoringMessageCauseException(String message, Throwable cause) {
    }
  }

  @SuppressWarnings("UnusedParameters")
  private static final class InvalidCauseNullInitializationException extends RuntimeException {

    private static final long serialVersionUID = -3421082007308082955L;

    public InvalidCauseNullInitializationException() {
      // This is wrong as a default exception initializes the cause with "this" to denote
      // that the cause is not yet set. "null" sets the cause - and it will fail to reset
      // the cause later on.
      super((Throwable) null);
    }
  }

  @SuppressWarnings("UnusedParameters")
  private static final class InvalidForeignCauseInitializationException extends RuntimeException {

    private static final long serialVersionUID = -6860397437741552643L;

    public InvalidForeignCauseInitializationException() {
      super(new IllegalStateException("foreign cause"));
    }
  }

  @SuppressWarnings({"UnusedParameters", "unused"})
  private static final class IgnoringSuppressionFlagsException extends RuntimeException {

    private static final long serialVersionUID = -6214964553357592467L;

    public IgnoringSuppressionFlagsException() {
    }

    protected IgnoringSuppressionFlagsException(String message, Throwable cause,
                                                boolean enableSuppression,
                                                boolean writableStackTrace) {
      super(message, cause, !enableSuppression, !writableStackTrace);
    }
  }

  @SuppressWarnings({"UnusedParameters", "unused"})
  private static final class ConstructorFailuresException extends RuntimeException {

    private static final long serialVersionUID = 4068799485301588101L;
    private static final String PROVOKED_FAILURE = "provoked failure";

    public ConstructorFailuresException() {
      throw new IllegalStateException(PROVOKED_FAILURE);
    }

    public ConstructorFailuresException(String message) {
      super(message);
      throw new IllegalStateException(PROVOKED_FAILURE);
    }

    public ConstructorFailuresException(String message, Throwable cause) {
      super(message, cause);
      throw new IllegalStateException(PROVOKED_FAILURE);
    }

    public ConstructorFailuresException(Throwable cause) {
      super(cause);
      throw new IllegalStateException(PROVOKED_FAILURE);
    }

    protected ConstructorFailuresException(String message, Throwable cause,
                                           boolean enableSuppression,
                                           boolean writableStackTrace) {
      super(message, cause, enableSuppression, writableStackTrace);
      throw new IllegalStateException(PROVOKED_FAILURE);
    }
  }

}
