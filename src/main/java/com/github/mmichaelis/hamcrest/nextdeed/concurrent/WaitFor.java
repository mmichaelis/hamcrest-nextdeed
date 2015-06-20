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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * <p>
 * Wait for the given matcher to evaluate to true. It is required that the embedded matcher checks a mutable
 * aspect of the matched object.
 * </p>
 * <p>
 * This matcher uses a decelerating wait approach, i. e. it tries to poll the state very often at the beginning
 * of the wait period and slows down towards the end. As a result fast system under tests will finish very quickly
 * while slower systems are given more time to actually reach the wanted state. If the state is reached directly
 * the algorithm will not even wait at all.
 * </p>
 * <dl>
 * <dt><strong>Convenience Function Included:</strong></dt>
 * <dd>
 * <p>
 * Most of the time using this matcher you will have a component under test which changes its state over time. For
 * the assertion you will need to retrieve the state of this component repeatedly until it meets your required
 * conditions.
 * </p>
 * <p>
 * Therefore this matcher comes with two inner classes ready to be filled with your state retrieving function
 * and to compare the retrieved state with any normal matcher available in the library. If you rely on Guava or
 * Java 8 you might as well use the functional interfaces they provide. So this is just for convenience if you happen
 * not to have Guava and/or Java 8 at hand.
 * </p>
 * </dd>
 * <dt><strong>Usage:</strong></dt>
 * <dd>
 * <p>
 * Relying on the function mentioned above a typical usage might look like this:
 * </p>
 * <pre>{@code
 * ComponentUnderTest cut = ...;
 * State expectedState = ...;
 * Function<ComponentUnderTest,State> stateFunction = new Function<>() { ... };
 * assertThat(componentUnderTest, waitFor(applying(stateFunction, equalTo(expectedState)), 2, SECONDS));
 * }</pre>
 * </dd>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * Unlike other matchers it is not useful to combine this matcher with e. g. logical operations like for example
 * <em>not</em>. Such logical operations need to be placed in the embedded matcher instead.
 * </dd>
 * </dl>
 *
 * @param <T> original type of the wrapped matcher
 * @author Olaf Kummer
 * @author Mark Michaelis
 * @see <a href="http://minds.coremedia.com/2012/11/29/haste-makes-waste/">Haste makes waste | Minds</a>
 * @since 1.0.0
 */
public class WaitFor<T> extends TypeSafeMatcher<T> {
  /**
   * Initial delay to wait if we need to wait.
   */
  private static final long INITIAL_DELAY_MS = 10L;
  /**
   * Factor by which the polling factor decelerates.
   */
  private static final double DECELERATION_FACTOR = 1.1;
  /**
   * A grace period for the last poll.
   */
  private static final long SLEEP_NOT_MUCH_LONGER_OFFSET_MILLIS = 100L;

  /**
   * Original matcher who is required to check a mutable aspect of the matched object.
   */
  @NotNull
  private final Matcher<T> originalMatcher;
  /**
   * Amount of timeout.
   *
   * @see #timeUnit
   */
  private final long timeout;
  /**
   * Unit for timeout.
   *
   * @see #timeout
   */
  @NotNull
  private final TimeUnit timeUnit;

  /**
   * Constructor with original matcher and timeout. For convenience it is recommended to use the
   * factory methods instead.
   *
   * @param originalMatcher matcher to wrap; must change its state over time
   * @param timeout         amount of timeout
   * @param timeUnit        unit of timeout
   */
  public WaitFor(@NotNull Matcher<T> originalMatcher,
                 long timeout,
                 @NotNull TimeUnit timeUnit) {
    this.originalMatcher = originalMatcher;
    this.timeout = timeout;
    this.timeUnit = timeUnit;
  }

  @Override
  protected boolean matchesSafely(T item) {
    long timeoutMs = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
    long deadlineTimeMs = nowMillis() + timeoutMs;
    // At first, wait 10ms between checks.
    long delay = INITIAL_DELAY_MS;

    boolean result;
    while (true) {
      long beforeEvaluationTimeMs = nowMillis();
      result = originalMatcher.matches(item);
      long afterEvaluationTimeMs = nowMillis();
      if (result || afterEvaluationTimeMs > deadlineTimeMs) {
        break;
      }
      delay = sleepAndRecalculateDelay(delay, deadlineTimeMs, beforeEvaluationTimeMs, afterEvaluationTimeMs);
    }
    return result;
  }

  /**
   * {@inheritDoc}
   * <p>
   * The description is the same as from the original matcher.
   * </p>
   */
  @Override
  public void describeTo(@NotNull Description description) {
    originalMatcher.describeTo(description);
  }

  /**
   * <p>
   * Describes the mismatch by falling back to the original matcher's mismatch description.
   * </p>
   * <dl>
   * <dt><strong>Note:</strong></dt>
   * <dd>
   * <p>
   * As the item tested here is expected to change over time it might result in a mismatch description
   * which does not match the invalid state retrieved before. Thus you might want to store the state
   * between comparison and failure.
   * </p>
   * </dd>
   * </dl>
   */
  @Override
  protected void describeMismatchSafely(T item, @NotNull Description mismatchDescription) {
    originalMatcher.describeMismatch(item, mismatchDescription);
  }

  /**
   * <p>
   * Decelerating wait. Decreases the polling interval over time to give the system under test a chance to
   * actually reach the desired state.
   * </p>
   */
  private long sleepAndRecalculateDelay(long previousDelay,
                                        long deadlineTimeMs,
                                        long beforeEvaluationTimeMs,
                                        long afterEvaluationTimeMs) {
    long newDelay = previousDelay;
    // Leave at least as much time between two checks as the check itself took.
    final long lastDuration = afterEvaluationTimeMs - beforeEvaluationTimeMs;
    if (lastDuration > newDelay) {
      newDelay = lastDuration;
    }

    // Wait, but not much longer than until the deadlineTimeMillis and at least a millisecond.
    try {
      sleep(Math.max(1, Math.min(newDelay, deadlineTimeMs + SLEEP_NOT_MUCH_LONGER_OFFSET_MILLIS - afterEvaluationTimeMs)));
    } catch (InterruptedException e) {
      throw new IllegalStateException("Unexpected interruption.", e);
    }

    // Make checks less and less frequently.
    // Increase the wait period using the deceleration factor, but
    // wait at least one millisecond longer next time.
    newDelay = Math.max(newDelay + 1, (long) (newDelay * DECELERATION_FACTOR));
    return newDelay;
  }

  /**
   * Sleep the given number of milliseconds.
   *
   * @param millis how long to sleep
   * @throws InterruptedException if the current thread has been interrupted
   */
  protected void sleep(long millis) throws InterruptedException {
    Thread.sleep(millis);
  }

  /**
   * Retrieve the current time in milliseconds. Especially allows to override this behavior for testing purpose.
   *
   * @return time in milliseconds
   */
  protected long nowMillis() {
    return System.currentTimeMillis();
  }

  /**
   * Creates a matcher which waits for the embedded matcher to evaluate to true.
   * It is required that the embedded matcher checks a mutable aspect of the matched object thus
   * typical default matchers like {@code equalTo} won't work.
   *
   * @param originalMatcher the original matcher to wait to return true
   * @param timeoutMs       timeout in milliseconds
   * @param <T>             accepted value type of the original matcher
   * @return matcher which waits
   */
  public static <T> Matcher<T> waitFor(Matcher<T> originalMatcher, long timeoutMs) {
    return waitFor(originalMatcher, timeoutMs, TimeUnit.MILLISECONDS);
  }

  /**
   * Creates a matcher which waits for the embedded matcher to evaluate to true.
   * It is required that the embedded matcher checks a mutable aspect of the matched object thus
   * typical default matchers like {@code equalTo} won't work.
   *
   * @param originalMatcher the original matcher to wait to return true
   * @param timeout         timeout amount
   * @param timeUnit        timeout unit
   * @param <T>             accepted value type of the original matcher
   * @return matcher which waits
   */
  public static <T> Matcher<T> waitFor(Matcher<T> originalMatcher, long timeout, TimeUnit timeUnit) {
    return new WaitFor<>(originalMatcher, timeout, timeUnit);
  }

}