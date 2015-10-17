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

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.Nullable;
import org.junit.AssumptionViolatedException;

/**
 * Strategy to timeout with assumption violation.
 *
 * @since 1.0.0
 */
final class ThrowAssumptionViolatedExceptionStrategy<T, R>
    implements Function<WaitTimeoutEvent<T, R>, R> {

  /**
   * Custom message for assertion error.
   */
  private final String reason;
  /**
   * Matcher which was used to validate the input value.
   */
  private final Matcher<? super R> matcher;

  public ThrowAssumptionViolatedExceptionStrategy(String reason, Matcher<? super R> matcher) {
    this.reason = reason;
    this.matcher = matcher;
  }

  @Override
  public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
    assert input != null : "null values unexpected";
    R lastResult = input.getLastResult();
    // Copy & Paste from Hamcrest Matcher's assert, but with new exception
    if (!matcher.matches(lastResult)) {
      throw new AssumptionViolatedException(
          new FailureMessage<>(lastResult, reason, matcher).getMessage());
    }
    // Will never get here unless as last validation the actual value eventually matches,
    // which actually means that the matcher responds differently on the same value.
    return lastResult;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("matcher", matcher)
        .add("reason", reason)
        .toString();
  }
}
