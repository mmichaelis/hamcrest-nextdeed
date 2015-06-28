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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.github.mmichaelis.hamcrest.nextdeed.function.ApplyingMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.function.Function;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests {@link WaitFor}.
 *
 * @author Olaf Kummer
 * @author Mark Michaelis
 * @since SINCE
 */
@SuppressWarnings({"MagicNumber", "DuplicateStringLiteralInspection"})
public class WaitForTest {

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Test
  public void end_immediately_if_first_match_succeeds() throws Exception {
    TrackingMatcher<Object> originalMatcher = new AlwaysTrue<>();
    Matcher<Object> matcher = WaitFor.waitFor(originalMatcher, 30);
    assertThat(Integer.MAX_VALUE, matcher);
    assertThat("Original matcher should exactly have been queried once.", 1,
               equalTo(originalMatcher.getMatchesCalls()));
  }

  @Test
  public void retries_on_failure() throws Exception {
    TrackingMatcher<Object> originalMatcher = new TriesUntilTrue<>(1);
    Matcher<Object> matcher = new NeverTimeOutWaitFor<>(originalMatcher);
    assertThat(Integer.MAX_VALUE, matcher);
    assertThat("Original matcher should exactly have been queried twice.", 2,
               equalTo(originalMatcher.getMatchesCalls()));
  }

  @Test
  public void fail_on_continuous_failure() throws Exception {
    TrackingMatcher<Object> originalMatcher = new AlwaysFalse<>();
    Matcher<Object> matcher = new TimeOutImmediatelyWaitFor<>(originalMatcher);
    AssertionError caughtAssertionFailure = null;
    try {
      assertThat(Integer.MAX_VALUE, matcher);
    } catch (AssertionError e) {
      caughtAssertionFailure = e;
    }
    assertThat("Exception should have been caught.", caughtAssertionFailure, notNullValue());
    assertThat(
        "Original matcher should exactly have been queried once.",
        1,
        equalTo(originalMatcher.getMatchesCalls()));
  }

  @Test
  public void embedded_matcher_descriptions_bubble_up() throws Exception {
    String description = "SomeDescription";
    String mismatchDescription = "SomeMismatchDescription";

    Matcher<Object> originalMatcher = new AlwaysFalse<>(description, mismatchDescription);
    Matcher<Object> matcher = new TimeOutImmediatelyWaitFor<>(originalMatcher);
    AssertionError caughtAssertionFailure = null;
    try {
      assertThat(Integer.MAX_VALUE, matcher);
    } catch (AssertionError e) {
      caughtAssertionFailure = e;
    }
    assertThat("Exception should have been caught.", caughtAssertionFailure, notNullValue());
    errorCollector.checkThat(
        "Embedded matcher's description should be contained in failure message",
        caughtAssertionFailure.getMessage(),
        Matchers.containsString(description));
    errorCollector.checkThat(
        "Embedded matcher's mismatch description should be contained in failure message",
        caughtAssertionFailure.getMessage(),
        Matchers.containsString(mismatchDescription));
  }

  @Test
  public void polling_decelerates() throws Exception {
    TrackingMatcher<Object> originalMatcher = new TriesUntilTrue<>(2);
    TrackSleepsWaitFor<Object>
        matcher =
        new TrackSleepsWaitFor<>(originalMatcher, 30, TimeUnit.SECONDS);
    assertThat(Integer.MAX_VALUE, matcher);
    List<Long> sleeps = matcher.getSleeps();
    assertThat("Should have slept two times.", sleeps, Matchers.hasSize(2));
    assertThat("Sleep times should have accelerated.", sleeps.get(0),
               Matchers.lessThan(sleeps.get(1)));
  }

  @Test
  public void polling_should_give_a_last_chance_before_failure() throws Exception {
    TrackingMatcher<Object> originalMatcher = new TriesUntilTrue<>(2);
    TrackSleepsWaitFor<Object>
        matcher =
        new TrackSleepsWaitFor<>(originalMatcher, 4, TimeUnit.SECONDS);
    assertThat(Integer.MAX_VALUE, matcher);
    List<Long> sleeps = matcher.getSleeps();
    assertThat("Should have slept two times.", sleeps, Matchers.hasSize(2));
    assertThat(
        "Last sleep time should be lower to grant one additional try.",
        sleeps.get(0),
        Matchers.greaterThan(sleeps.get(1)));
  }

  @Test
  public void example_use_case_with_delegating_matcher_works() throws Exception {
    ComponentUnderTest componentUnderTest = new ComponentUnderTest(1, 2);
    Matcher<ComponentUnderTest>
        waitForMatcher =
        new NeverTimeOutWaitFor<>(new ApplyingMatcher<>(new ToState(), Matchers.equalTo(2)));
    assertThat("Requested state should have been reached on second try.", componentUnderTest,
               waitForMatcher);
  }

  @Test
  public void delegating_matcher_remembers_previous_state_on_failure() throws Exception {
    ComponentUnderTest componentUnderTest = new ComponentUnderTest(42, 43);
    Matcher<ComponentUnderTest> waitForMatcher =
        new TimeOutImmediatelyWaitFor<>(
            ApplyingMatcher.applying(new ToState(), Matchers.equalTo(43)));
    AssertionError caughtAssertionFailure = null;
    try {
      assertThat("Provoke failure because of timeout.", componentUnderTest, waitForMatcher);
    } catch (AssertionError e) {
      caughtAssertionFailure = e;
    }
    assertThat("Exception should have been caught.", caughtAssertionFailure, notNullValue());
    assertThat(
        "Failure message should refer to previous and not current state.",
        caughtAssertionFailure.getMessage(),
        Matchers.containsString("42"));
  }

  @Test(expected = IllegalStateException.class)
  public void rethrow_interrupted_exception() throws Exception {
    Matcher<String> embeddedMatcher = new AlwaysFalse<>();
    Matcher<String> waitForMatcher = new InterruptedSleepWaitFor<>(embeddedMatcher);
    assertThat("Provoke interrupted exception, rethrown as IllegalStateException.",
               "Something", waitForMatcher);
  }

  @Test
  public void sleep_really_sleeps() throws Exception {
    PublicSleepWaitFor matcher = new PublicSleepWaitFor();
    long beforeMillis = System.currentTimeMillis();
    matcher.sleep(5L);
    long afterMillis = System.currentTimeMillis();
    assertThat("Should have delayed about 5 ms.", afterMillis - beforeMillis,
               Matchers.greaterThan(4L));
  }

  /*
   * -------------------------------------------------------------------------------------------------------------------
   * Helper Classes
   * -------------------------------------------------------------------------------------------------------------------
   */

  /**
   * Some component under test which changes its state each time the state is queried.
   */
  private static class ComponentUnderTest {

    private final List<Integer> states;

    private ComponentUnderTest(Integer... states) {
      this(Arrays.asList(states));
    }

    private ComponentUnderTest(List<Integer> states) {
      this.states = new ArrayList<>(states);
    }

    public int getState() {
      return states.remove(0);
    }
  }

  /**
   * Function to retrieve state from the component under test.
   */
  private static class ToState implements Function<ComponentUnderTest, Integer> {

    @Override
    public Integer apply(ComponentUnderTest input) {
      return input.getState();
    }
  }

  private static class TrackingMatcher<T> extends TypeSafeMatcher<T> {

    private int matchesCalls = 0;

    @Override
    public void describeTo(Description description) {
    }

    public int getMatchesCalls() {
      return matchesCalls;
    }

    @Override
    protected boolean matchesSafely(T item) {
      matchesCalls++;
      return false;
    }
  }

  /**
   * Matcher which eventually will succeed.
   */
  private static class TriesUntilTrue<T> extends TrackingMatcher<T> {

    private final int untilCount;
    private int currentCount = 0;

    private TriesUntilTrue(int untilCount) {
      this.untilCount = untilCount;
    }

    @Override
    protected boolean matchesSafely(T item) {
      super.matchesSafely(item);
      return untilCount == currentCount++;
    }
  }

  /**
   * Matcher which denotes success always.
   */
  private static class AlwaysTrue<T> extends TrackingMatcher<T> {

    @Override
    protected boolean matchesSafely(T item) {
      super.matchesSafely(item);
      return true;
    }
  }

  /**
   * Matcher which denotes failure always.
   */
  private static class AlwaysFalse<T> extends TrackingMatcher<T> {

    private final String fixedDescription;
    private final String fixedMismatchDescription;

    private AlwaysFalse() {
      this(null, null);
    }

    public AlwaysFalse(String fixedDescription, String fixedMismatchDescription) {
      this.fixedDescription = fixedDescription;
      this.fixedMismatchDescription = fixedMismatchDescription;
    }

    @Override
    public void describeTo(Description description) {
      super.describeTo(description);
      if (fixedDescription != null) {
        description.appendText(fixedDescription);
      }
    }    @Override
    protected boolean matchesSafely(T item) {
      super.matchesSafely(item);
      return false;
    }



    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
      super.describeMismatchSafely(item, mismatchDescription);
      if (fixedMismatchDescription != null) {
        mismatchDescription.appendText(fixedMismatchDescription);
      }
    }
  }

  /**
   * Special WaitFor matcher which will never time out.
   */
  private static class NeverTimeOutWaitFor<T> extends WaitFor<T> {

    public NeverTimeOutWaitFor(Matcher<T> originalMatcher) {
      super(originalMatcher, 24, TimeUnit.HOURS);
    }

    @Override
    protected void sleep(long millis) throws InterruptedException {
      // do nothing, don't sleep
    }

    @Override
    protected long nowMillis() {
      return 0;
    }

  }

  /**
   * Special WaitFor matcher which will time out immediately.
   */
  private static class TimeOutImmediatelyWaitFor<T> extends WaitFor<T> {

    private long count = 0;

    public TimeOutImmediatelyWaitFor(Matcher<T> originalMatcher) {
      super(originalMatcher, 0L, TimeUnit.MILLISECONDS);
    }

    @Override
    protected long nowMillis() {
      return count++;
    }

    @Override
    protected void sleep(long millis) throws InterruptedException {
      // do nothing, don't sleep
    }
  }

  /**
   * WaitFor matcher which tracks sleep-calls for later assertions regarding the decelerating
   * behavior.
   */
  private static class TrackSleepsWaitFor<T> extends WaitFor<T> {

    private long count = 0;
    private List<Long> sleeps = new ArrayList<>();

    public TrackSleepsWaitFor(Matcher<T> originalMatcher, long timeout, TimeUnit timeUnit) {
      super(originalMatcher, timeout, timeUnit);
    }

    public List<Long> getSleeps() {
      return Collections.unmodifiableList(sleeps);
    }

    @Override
    protected long nowMillis() {
      count += 1000;
      return count;
    }

    @Override
    protected void sleep(long millis) throws InterruptedException {
      // don't sleep, just track
      sleeps.add(millis);
    }


  }

  private static class InterruptedSleepWaitFor<T> extends NeverTimeOutWaitFor<T> {

    public InterruptedSleepWaitFor(@NotNull Matcher<T> originalMatcher) {
      super(originalMatcher);
    }

    @Override
    protected void sleep(long millis) throws InterruptedException {
      Thread.currentThread().interrupt();
      Thread.sleep(1L);
    }
  }

  private static class PublicSleepWaitFor extends WaitFor<Boolean> {

    public PublicSleepWaitFor() {
      super(is(false), 0, TimeUnit.MILLISECONDS);
    }

    @Override
    public void sleep(long millis) throws InterruptedException {
      super.sleep(millis);
    }
  }
}
