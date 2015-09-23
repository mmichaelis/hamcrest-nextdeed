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

package com.github.mmichaelis.hamcrest.nextdeed.concurrent;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Build the failure message just as Hamcrest does in {@code MatcherAssert}.
 *
 * @param <R> type of the result
 * @since 1.0.0
 */
final class FailureMessage<R> {

  /**
   * The last result which will be used to build the failure message.
   *
   * @since 1.0.0
   */
  @Nullable
  private final R lastResult;
  /**
   * Description for the failure.
   *
   * @since 1.0.0
   */
  @Nullable
  private final String reason;
  /**
   * Matcher which did not accept the last result.
   *
   * @since 1.0.0
   */
  @NotNull
  private final Matcher<? super R> matcher;

  /**
   * Build the failure message just as Hamcrest does in {@code MatcherAssert}.
   *
   * @param lastResult last result
   * @param reason     reason given as e. g. assertion message
   * @param matcher    matcher which did not match the last result
   * @since 1.0.0
   */
  FailureMessage(@Nullable R lastResult,
                 @Nullable String reason,
                 @NotNull Matcher<? super R> matcher) {
    this.lastResult = lastResult;
    this.reason = reason;
    this.matcher = requireNonNull(matcher, "matcher must not be null.");
  }

  /**
   * Create the message.
   *
   * @return message
   * @since 1.0.0
   */
  @NotNull
  public String getMessage() {
    // Copy & Paste from Hamcrest Matcher's assert, to be able to use same
    // message for different exceptions.
    Description description = new StringDescription();
    description.appendText(Optional.fromNullable(reason).or(""))
        .appendText("\nExpected: ")
        .appendDescriptionOf(matcher)
        .appendText("\n     but: ");
    matcher.describeMismatch(lastResult, description);

    return description.toString();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("lastResult", lastResult)
        .add("matcher", matcher)
        .add("reason", reason)
        .toString();
  }
}
