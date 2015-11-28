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

import static com.github.mmichaelis.hamcrest.nextdeed.exception.Messages.messages;

import com.google.common.base.MoreObjects;
import com.google.common.reflect.Invokable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

/**
 * <p>
 * Describes the various requirements for exceptions in the different Java releases. Only contains
 * Java versions which introduced changes to exceptions.
 * </p>
 *
 * @since SINCE
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public enum JavaComplianceLevel {
  JAVA_1_1("Java 1.1", false, false),
  JAVA_1_4("Java 1.4", true, false),
  JAVA_1_7("Java 1.7", true, true);

  private static final Logger LOG = LoggerFactory.getLogger(JavaComplianceLevel.class);

  /**
   * Just some sample cause message during validation.
   */
  private static final String PROBE_CAUSE_MESSAGE = "probeCauseMessage";
  /**
   * Just some sample causing exception during validation.
   */
  private static final Throwable PROBE_CAUSE = new RuntimeException(PROBE_CAUSE_MESSAGE);
  /**
   * Just some sample message for the exception during validation.
   */
  private static final String PROBE_MESSAGE = "probeMessage";
  /**
   * Name of the Java Compliance Level.
   */
  private final String javaName;
  /**
   * If exception cause is supported. Value differs for compliance levels.
   */
  private final boolean supportsCause;
  /**
   * If suppression e. g. for stacktraces is supported. Value differs for compliance levels.
   */
  private final boolean supportsSuppression;

  JavaComplianceLevel(String javaName, boolean supportsCause, boolean supportsSuppression) {
    this.javaName = javaName;
    this.supportsCause = supportsCause;
    this.supportsSuppression = supportsSuppression;
  }

  /**
   * Get the Java Compliance Level Name.
   *
   * @return name
   */
  public String getJavaName() {
    return javaName;
  }

  public <T extends Class<? extends Throwable>> Collection<String> validate(
      @NotNull T itemClass) {
    Collection<String> issues = new ArrayList<>();
    validateDefaultConstructor(itemClass, issues);
    validateMessageConstructor(itemClass, issues);
    validateCauseConstructor(itemClass, issues);
    validateMessageCauseConstructor(itemClass, issues);
    validateSuppressionEnabledConstructor(itemClass, issues);
    return issues;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("javaName", javaName)
        .add("supportsCause", supportsCause)
        .add("supportsSuppression", supportsSuppression)
        .add("super", super.toString())
        .toString();
  }

  private void validateDefaultConstructor(Class<? extends Throwable> itemClass,
                                          Collection<String> issues) {
    try {
      Constructor<? extends Throwable> constructor = itemClass.getDeclaredConstructor();
      instantiateViaDefaultConstructor(constructor, issues);
    } catch (NoSuchMethodException e) {
      LOG.trace("Issue detected: Cannot instantiate via default constructor.", e);
      issues.add(messages().noDefaultConstructor());
    }
  }

  private void validateMessageConstructor(Class<? extends Throwable> itemClass,
                                          Collection<String> issues) {
    try {
      Constructor<? extends Throwable> constructor =
          itemClass.getDeclaredConstructor(String.class);
      instantiateViaMessageConstructor(constructor, issues);
    } catch (NoSuchMethodException e) {
      LOG.trace("Issue detected: message constructor not available.", e);
      issues.add(messages().noMessageConstructor());
    }
  }

  private void validateCauseConstructor(Class<? extends Throwable> itemClass,
                                        Collection<String> issues) {
    if (supportsCause) {
      try {
        Constructor<? extends Throwable> constructor =
            itemClass.getDeclaredConstructor(Throwable.class);
        instantiateViaCauseConstructor(constructor, issues);
      } catch (NoSuchMethodException e) {
        LOG.trace("Issue detected: cause constructor not available.", e);
        issues.add(messages().noCauseConstructor());
      }
    }
  }

  private void validateMessageCauseConstructor(Class<? extends Throwable> itemClass,
                                               Collection<String> issues) {
    if (supportsCause) {
      try {
        Constructor<? extends Throwable> constructor =
            itemClass.getDeclaredConstructor(String.class, Throwable.class);
        instantiateViaMessageCauseConstructor(constructor, issues);
      } catch (NoSuchMethodException e) {
        LOG.trace("Issue detected: message-cause constructor not available.", e);
        issues.add(messages().noMessageCauseConstructor());
      }
    }
  }

  private void validateSuppressionEnabledConstructor(Class<? extends Throwable> itemClass,
                                                     Collection<String> issues) {
    if (supportsSuppression) {
      try {
        Constructor<? extends Throwable> constructor =
            itemClass.getDeclaredConstructor(String.class, Throwable.class, Boolean.TYPE,
                                             Boolean.TYPE);
        if (!Invokable.from(constructor).isProtected()) {
          issues.add(messages().suppressionEnabledConstructorIsNotProtected());
        }
        instantiateViaSuppressionEnabledConstructor(constructor, issues);
      } catch (NoSuchMethodException e) {
        LOG.trace("Issue detected: suppression-enabled constructor not available.", e);
        issues.add(messages().noSuppressionEnabledConstructor());
      }
    }
  }

  private static void validateConstructorMessage(@NotNull String description,
                                          @Nullable String expectedMessage,
                                          @NotNull Throwable instance,
                                          @NotNull Collection<String> issues) {
    String actualMessage = instance.getMessage();
    if (!Objects.equals(expectedMessage, actualMessage)) {
      issues.add(messages().messageNotAsExpected(description, expectedMessage, actualMessage));
    }
  }

  private void validateConstructorCause(@NotNull String description,
                                        @Nullable Throwable expectedCause,
                                        @NotNull Throwable instance,
                                        @NotNull Collection<String> issues) {
    if (supportsCause) {
      Throwable actualCause = instance.getCause();
      if (!Objects.equals(expectedCause, actualCause)) {
        issues.add(messages().causeNotAsExpected(description, expectedCause, actualCause));
      }
    }
  }

  private void validateCanInitializeCause(@NotNull String description,
                                          @NotNull Throwable instance,
                                          @NotNull Collection<String> issues) {
    if (supportsCause) {
      try {
        instance.initCause(PROBE_CAUSE);
      } catch (IllegalArgumentException | IllegalStateException e) {
        LOG.trace("Issue detected: problem while initializing cause.", e);
        if (null == instance.getCause()) {
          issues.add(messages().initCauseFailureNull(description));
        } else {
          issues.add(messages().initCauseFailureNotNull(description));
        }
      }
    }
  }

  private static void validateConstructorSuppression(@NotNull String description,
                                              @NotNull Throwable instance,
                                              @NotNull Collection<String> issues) {
    StackTraceElement[] stackTraceBefore = instance.getStackTrace();
    instance.fillInStackTrace();
    StackTraceElement[] stackTraceAfter = instance.getStackTrace();

    if (!Arrays.deepEquals(stackTraceBefore, stackTraceAfter)) {
      issues.add(messages().writableStacktraceNotDisabled(description));
    }

    instance.addSuppressed(PROBE_CAUSE);
    Throwable[] suppressed = instance.getSuppressed();
    if (suppressed.length != 0) {
      issues.add(messages().suppressionNotDisabled(description));
    }
  }

  private void instantiateViaDefaultConstructor(
      Constructor<? extends Throwable> constructor,
      Collection<String> issues) {
    String constructorType = messages().defaultConstructor();
    try {
      Throwable instance = constructor.newInstance();
      validateConstructorMessage(constructorType, null, instance, issues);
      validateConstructorCause(constructorType, null, instance, issues);
      validateCanInitializeCause(constructorType, instance, issues);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(messages().cannotInstantiateVia(constructorType, e, e.getCause()));
    }
  }

  private void instantiateViaMessageConstructor(
      Constructor<? extends Throwable> constructor,
      Collection<String> issues) {
    String constructorType = messages().messageConstructor();
    try {
      Throwable instance = constructor.newInstance(PROBE_MESSAGE);
      validateConstructorMessage(constructorType, PROBE_MESSAGE, instance, issues);
      validateConstructorCause(constructorType, null, instance, issues);
      validateCanInitializeCause(constructorType, instance, issues);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(messages().cannotInstantiateVia(constructorType, e, e.getCause()));
    }
  }

  private void instantiateViaCauseConstructor(
      Constructor<? extends Throwable> constructor,
      Collection<String> issues) {
    String constructorType = messages().causeConstructor();
    try {
      Throwable instance = constructor.newInstance(PROBE_CAUSE);
      validateConstructorMessage(constructorType, PROBE_CAUSE.toString(), instance, issues);
      validateConstructorCause(constructorType, PROBE_CAUSE, instance, issues);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(messages().cannotInstantiateVia(constructorType, e, e.getCause()));
    }
  }

  private void instantiateViaMessageCauseConstructor(
      Constructor<? extends Throwable> constructor,
      Collection<String> issues) {
    String constructorType = messages().messageCauseConstructor();
    try {
      Throwable instance = constructor.newInstance(PROBE_MESSAGE, PROBE_CAUSE);
      validateConstructorMessage(constructorType, PROBE_MESSAGE, instance, issues);
      validateConstructorCause(constructorType, PROBE_CAUSE, instance, issues);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(messages().cannotInstantiateVia(constructorType, e, e.getCause()));
    }
  }

  private void instantiateViaSuppressionEnabledConstructor(
      Constructor<? extends Throwable> constructor,
      Collection<String> issues) {
    String constructorType = messages().suppressionEnabledConstructor();
    try {
      Invokable.from(constructor).setAccessible(true);
      Throwable instance = constructor.newInstance(PROBE_MESSAGE, PROBE_CAUSE, false, false);
      validateConstructorMessage(constructorType, PROBE_MESSAGE, instance, issues);
      validateConstructorCause(constructorType, PROBE_CAUSE, instance, issues);
      validateConstructorSuppression(constructorType, instance, issues);
    } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
      issues.add(messages().cannotInstantiateVia(constructorType, e, e.getCause()));
    }
  }
}
