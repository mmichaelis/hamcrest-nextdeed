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
import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Collections2;
import com.google.common.reflect.TypeToken;

import com.github.mmichaelis.hamcrest.nextdeed.base.Issue;
import com.github.mmichaelis.hamcrest.nextdeed.base.IssuesMatcher;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * <p>
 * Matcher for easily checking an exception to fulfill standard Java requirements, thus especially
 * if it contains the same constructors as available for {@link Throwable} and instantiation sets
 * message and cause as expected.
 * </p>
 * <p>
 * This matcher is not (yet) meant for custom exceptions with extra arguments, special error
 * reporting, etc. Its purpose is to ease testing and fulfilling code coverage for most common
 * custom exceptions.
 * </p>
 *
 * @since SINCE
 */
public class IsJavaCompliantExceptionMatcher<T extends Class<?>> extends IssuesMatcher<T> {

  private final JavaComplianceLevel level;

  public IsJavaCompliantExceptionMatcher(JavaComplianceLevel level) {
    super(messages().compliantException(level.getJavaName()));
    this.level = level;
  }

  /**
   * Validates that the exception (or object) is compliant to standard Java Exceptions at
   * a certain compliance level. For example an exception cause exists since Java 1.1.
   *
   * @param level Java level to check
   * @param <T>   type of the exception/object
   * @return matcher
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isJavaCompliantException(
      @NotNull JavaComplianceLevel level) {
    return new IsJavaCompliantExceptionMatcher<>(requireNonNull(level, "level must not be null."));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("level", level)
        .add("super", super.toString())
        .toString();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void validate(@NotNull T item, @NotNull Collection<Issue> issues) {
    TypeToken<Throwable> throwableType = TypeToken.of(Throwable.class);
    TypeToken<?> itemToken = TypeToken.of(item);
    if (!throwableType.isAssignableFrom(itemToken)) {
      issues.add(issue(messages().didNotExtend(Throwable.class)));
      return;
    }
    Collection<String> issueMessages = level.validate((Class<? extends Throwable>) item);
    issues.addAll(Collections2.transform(issueMessages, new Function<String, Issue>() {
      @Override
      public Issue apply(String input) {
        return issue(input);
      }
    }));
  }

}
