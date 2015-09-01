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

import static com.github.mmichaelis.hamcrest.nextdeed.glue.HamcrestGlue.asPredicate;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.AssumptionViolatedException;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Utility class for launching probes on a system.
 * </p>
 * <dl>
 * <dt><strong>Assert/Assume/Require:</strong></dt>
 * <dd>
 * <p>
 * When to use which validation of the system under test?
 * </p>
 * <dl>
 * <dt><em>Assert:</em></dt>
 * <dd>
 * Use this if you are about to test that the system reaches the requested state. On
 * failure your test case will be marked as <em>Failure</em>.
 * </dd>
 * <dt><em>Assume:</em></dt>
 * <dd>
 * Use this for programmatically ignoring your tests (instead of using for example annotations).
 * On failure your test case will be marked as <em>Ignored</em>.
 * </dd>
 * <dt><em>Require:</em></dt>
 * <dd>
 * In tests use this to check for preconditions which, if failed, will prevent you from doing
 * your actual test. On failure your test case will be marked as <em>Error</em>.
 * </dd>
 * </dl>
 * </dd>
 * <dt><strong>Example:</strong></dt>
 * <dd>
 * <pre>{@code
 * Probe.<System, State>probing(systemUnderTest)
 *      .withinMs(1000L);
 *      .assertThat(new Function<System,State>(){...}, equalTo(RUNNING));
 * }</pre>
 * <p>
 * Mind that it is required to add the generic type parameters to {@code probing} &mdash; which
 * in return will prevent you from using static import for {@code probing}. The advantage is
 * that you see right at the start what is your system under test and what is the state type
 * you will wait for.
 * </p>
 * </dd>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * <p>
 * This class can be compared to Hamcrest's {@code MatcherAssert}. So whenever you want to
 * assert/assume/require giving some grace period to the system under test this class is
 * the one you should choose.
 * </p>
 * </dd>
 * </dl>
 *
 * @since SINCE
 */
public final class Probe {

  /**
   * Utility class constructor. You must not instantiate this :-)
   */
  private Probe() {
    // Utility class
  }

  /**
   * First specify what system you want to probe.
   *
   * @param <T>    the type of system you are probing
   * @param <R>    the type of state variable you are polling
   * @param target the system under test
   * @return Builder for your waiting assertion, ...
   */
  @NotNull
  public static <T, R> ProbeBuilder<T, R> probing(@NotNull T target) {
    return new ProbeBuilderImpl<>(target);
  }

  /**
   * Build the failure message just as Hamcrest does in {@code MatcherAssert}.
   *
   * @param lastResult last result
   * @param reason     reason given as e. g. assertion message
   * @param matcher    matcher which did not match the last result
   * @param <R>        type of the result
   * @return message
   */
  @NotNull
  private static <R> String getMessage(R lastResult, @Nullable String reason,
                                       Matcher<? super R> matcher) {
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

  public interface ProbeBuilder<T, R> extends WaitBuilder {

    @NotNull
    @Override
    ProbeBuilder<T, R> withinMs(long timeoutMs);

    @NotNull
    @Override
    ProbeBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit);

    @NotNull
    @Override
    ProbeBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs);

    @NotNull
    @Override
    ProbeBuilder<T, R> withFinalGracePeriod(long gracePeriod,
                                            @NotNull TimeUnit timeUnit);

    @NotNull
    @Override
    ProbeBuilder<T, R> withInitialDelayMs(long initialDelayMs);

    @NotNull
    @Override
    ProbeBuilder<T, R> withInitialDelay(long initialDelay,
                                        @NotNull TimeUnit timeUnit);

    @NotNull
    @Override
    ProbeBuilder<T, R> deceleratePollingBy(double decelerationFactor);

    @NotNull
    @Override
    ProbeBuilder<T, R> and();

    void assertThat(@NotNull Function<T, R> actualFunction,
                    @NotNull Matcher<? super R> matcher);

    void assertThat(@Nullable String reason,
                    @NotNull Function<T, R> actualFunction,
                    @NotNull Matcher<? super R> matcher);

    void assumeThat(@NotNull Function<T, R> actualFunction,
                    @NotNull Matcher<? super R> matcher);

    void assumeThat(@Nullable String reason,
                    @NotNull Function<T, R> actualFunction,
                    @NotNull Matcher<? super R> matcher);

    void requireThat(@NotNull Function<T, R> actualFunction,
                     @NotNull Matcher<? super R> matcher);

    void requireThat(@Nullable String reason,
                     @NotNull Function<T, R> actualFunction,
                     @NotNull Matcher<? super R> matcher);
  }

  @VisibleForTesting
  static class ProbeBuilderImpl<T, R> implements ProbeBuilder<T, R> {

    @NotNull
    private final WaitFunction.Builder<T, R> waitFunctionBuilder;
    @NotNull
    private final T target;
    private Function<T, R> actualFunction;
    private Function<WaitFunction<T, R>, WaitFunction<T, R>> waitFunctionPreProcessor =
        Functions.identity();

    private ProbeBuilderImpl(@NotNull final T target) {
      this.target = target;
      waitFunctionBuilder = WaitFunction.waitFor(new Function<T, R>() {
        @Override
        public R apply(@Nullable T input) {
          return ProbeBuilderImpl.this.getActualFunction().apply(input);
        }
      });
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> withinMs(long timeoutMs) {
      waitFunctionBuilder.withinMs(timeoutMs);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> within(long timeout, @NotNull TimeUnit timeUnit) {
      waitFunctionBuilder.within(timeout, timeUnit);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> withFinalGracePeriodMs(long gracePeriodMs) {
      waitFunctionBuilder.withFinalGracePeriodMs(gracePeriodMs);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> withFinalGracePeriod(long gracePeriod,
                                                   @NotNull TimeUnit timeUnit) {
      waitFunctionBuilder.withFinalGracePeriod(gracePeriod, timeUnit);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> withInitialDelayMs(long initialDelayMs) {
      waitFunctionBuilder.withInitialDelayMs(initialDelayMs);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> withInitialDelay(long initialDelay, @NotNull TimeUnit timeUnit) {
      waitFunctionBuilder.withInitialDelay(initialDelay, timeUnit);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> deceleratePollingBy(double decelerationFactor) {
      waitFunctionBuilder.deceleratePollingBy(decelerationFactor);
      return this;
    }

    @NotNull
    @Override
    public ProbeBuilder<T, R> and() {
      waitFunctionBuilder.and();
      return this;
    }

    @Override
    public void assertThat(@NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      assertThat(null, actualFunction, matcher);
    }

    @Override
    public void assertThat(@Nullable String reason,
                           @NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      checkThat(
          actualFunction,
          matcher,
          new ThrowAssertionError<T, R>(reason, matcher));
    }

    @Override
    public void assumeThat(@NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      assumeThat(null, actualFunction, matcher);
    }

    @Override
    public void assumeThat(@Nullable String reason,
                           @NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      checkThat(
          actualFunction,
          matcher,
          new ThrowAssumptionViolation<T, R>(reason, matcher));
    }

    @Override
    public void requireThat(@NotNull Function<T, R> actualFunction,
                            @NotNull Matcher<? super R> matcher) {
      requireThat(null, actualFunction, matcher);
    }

    @Override
    public void requireThat(@Nullable String reason,
                            @NotNull Function<T, R> actualFunction,
                            @NotNull Matcher<? super R> matcher) {
      checkThat(
          actualFunction,
          matcher,
          new ThrowWaitTimeoutException<T, R>(reason, matcher));
    }

    @VisibleForTesting
    ProbeBuilder<T, R> preProcessWaitFunction(
        @NotNull Function<WaitFunction<T, R>, WaitFunction<T, R>> waitFunctionPreProcessor) {
      this.waitFunctionPreProcessor = waitFunctionPreProcessor;
      return this;
    }

    private Function<T, R> getActualFunction() {
      assert actualFunction != null : "actualFunction must be set.";
      return actualFunction;
    }


    private void checkThat(@NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher,
                           @NotNull Function<WaitTimeoutEvent<T, R>, R> timeoutFunction) {
      this.actualFunction = actualFunction;
      WaitFunction<T, R> waitFunction = waitFunctionBuilder
          .toFulfill(asPredicate(matcher))
          .onTimeout(timeoutFunction)
          .get();
      WaitFunction<T, R> preProcessedWaitFunction = waitFunctionPreProcessor.apply(waitFunction);
      assert preProcessedWaitFunction
             != null : "Wait function should not have been preprocessed to null.";
      preProcessedWaitFunction.apply(target);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("hash", Integer.toHexString(System.identityHashCode(this)))
          .add("actualFunction", actualFunction)
          .add("target", target)
          .add("waitFunctionBuilder", waitFunctionBuilder)
          .add("waitFunctionPreProcessor", waitFunctionPreProcessor)
          .toString();
    }
  }

  private static class ThrowAssumptionViolation<T, R>
      implements Function<WaitTimeoutEvent<T, R>, R> {

    private final String reason;
    private final Matcher<? super R> matcher;

    public ThrowAssumptionViolation(String reason, Matcher<? super R> matcher) {
      this.reason = reason;
      this.matcher = matcher;
    }

    @Override
    public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
      assert input != null : "null values unexpected";
      R lastResult = input.getLastResult();
      // Copy & Paste from Hamcrest Matcher's assert, but with new exception
      if (!matcher.matches(lastResult)) {
        throw new AssumptionViolatedException(getMessage(lastResult, reason, matcher));
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

  private static class ThrowWaitTimeoutException<T, R>
      implements Function<WaitTimeoutEvent<T, R>, R> {

    private final String reason;
    private final Matcher<? super R> matcher;

    public ThrowWaitTimeoutException(String reason, Matcher<? super R> matcher) {
      this.reason = reason;
      this.matcher = matcher;
    }

    @Override
    public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
      assert input != null : "null values unexpected";
      R lastResult = input.getLastResult();
      if (!matcher.matches(lastResult)) {
        throw new WaitTimeoutException(getMessage(lastResult, reason, matcher));
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

  private static class ThrowAssertionError<T, R> implements Function<WaitTimeoutEvent<T, R>, R> {

    private final String reason;
    private final Matcher<? super R> matcher;

    public ThrowAssertionError(String reason, Matcher<? super R> matcher) {
      this.reason = reason;
      this.matcher = matcher;
    }

    @Override
    public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
      assert input != null : "null values unexpected";
      R lastResult = input.getLastResult();
      org.hamcrest.MatcherAssert
          .assertThat(Optional.fromNullable(reason).or(""), lastResult, matcher);
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

}
