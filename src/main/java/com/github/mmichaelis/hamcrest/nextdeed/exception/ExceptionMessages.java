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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Messages used within this package backed by {@code Bundle.properties}.
 *
 * @since SINCE
 */
interface ExceptionMessages {

  /**
   * Message "is compliant to exceptions for {0}".
   *
   * @param javaName name of java compliance level
   * @return message
   */
  @NotNull
  String compliantException(@NotNull String javaName);

  /**
   * Message "was null".
   *
   * @return message
   */
  @NotNull
  String wasNull();

  /**
   * Message "did not extend {0}"
   *
   * @param clazz class which is not extended as expected
   * @return message
   */
  @NotNull
  String didNotExtend(@NotNull Class<?> clazz);

  /**
   * Message "Does not provide default constructor."
   *
   * @return message
   */
  @NotNull
  String noDefaultConstructor();

  /**
   * Message "Does not provide message constructor."
   *
   * @return message
   */
  @NotNull
  String noMessageConstructor();

  /**
   * Message "Does not provide cause constructor."
   *
   * @return message
   */
  @NotNull
  String noCauseConstructor();

  /**
   * Message "Does not provide message-cause constructor."
   *
   * @return message
   */
  @NotNull
  String noMessageCauseConstructor();

  /**
   * Message "Does not provide suppression enabled constructor."
   *
   * @return message
   */
  String noSuppressionEnabledConstructor();

  /**
   * Message "%s: Expected Message: '%s' but was '%s'."
   *
   * @param description     description of the current check type
   * @param expectedMessage expected exception message
   * @param actualMessage   actual exception message
   * @return message
   */
  @NotNull
  String messageNotAsExpected(@NotNull String description,
                              @Nullable String expectedMessage,
                              @Nullable String actualMessage);

  /**
   * Message "%s: Expected Cause: '%s' but was '%s'."
   *
   * @param description   description of the current check type
   * @param expectedCause expected exception cause
   * @param actualCause   actual exception cause
   * @return message
   */
  @NotNull
  String causeNotAsExpected(@NotNull String description,
                            @Nullable Throwable expectedCause,
                            @Nullable Throwable actualCause);

  /**
   * Message that {@code initCause()} cannot be called and cause is null.
   *
   * @param description description of the current check type
   * @return message
   */
  @NotNull
  String initCauseFailureNull(@NotNull String description);

  /**
   * Message that {@code initCause()} cannot be called and cause is set (not null).
   *
   * @param description description of the current check type
   * @return message
   */
  @NotNull
  String initCauseFailureNotNull(@NotNull String description);

  /**
   * Message "%s: Writable stacktrace not disabled as expected."
   *
   * @param description description of the current check type
   * @return message
   */
  @NotNull
  String writableStacktraceNotDisabled(@NotNull String description);

  /**
   * Message "%s: Suppression not disabled as expected."
   *
   * @param description description of the current check type
   * @return message
   */
  @NotNull
  String suppressionNotDisabled(@NotNull String description);

  /**
   * Message "Cannot instantiate via %s: %s"
   *
   * @param constructorType  constructor which could not be used
   * @param exception exception raised when trying to instantiate
   * @param exceptionCause exception cause of exception raised when trying to instantiate
   * @return message
   */
  @NotNull
  String cannotInstantiateVia(@NotNull String constructorType,
                              @NotNull Throwable exception,
                              @NotNull Throwable exceptionCause);

  /**
   * Message "Default Constructor"
   *
   * @return message
   */
  @NotNull
  String defaultConstructor();

  /**
   * Message "Message Constructor"
   *
   * @return message
   */
  @NotNull
  String messageConstructor();

  /**
   * Message "Cause Constructor"
   *
   * @return message
   */
  @NotNull
  String causeConstructor();

  /**
   * Message "Message-Cause Constructor"
   *
   * @return message
   */
  @NotNull
  String messageCauseConstructor();

  /**
   * Message "Suppression-Enabled Constructor"
   *
   * @return message
   */
  @NotNull
  String suppressionEnabledConstructor();

  /**
   * Message that the suppression enabled constructor is not protected as required.
   *
   * @return message
   */
  @NotNull
  String suppressionEnabledConstructorIsNotProtected();
}
