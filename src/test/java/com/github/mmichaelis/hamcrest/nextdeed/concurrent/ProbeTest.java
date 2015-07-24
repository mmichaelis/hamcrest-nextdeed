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

import static java.lang.String.valueOf;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyLong;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.base.Function;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.AssumptionViolatedException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests {@link Probe} or more specifically
 * {@link com.github.mmichaelis.hamcrest.nextdeed.concurrent.Probe.ProbeBuilder}. The main focus
 * is to test that the given arguments are correctly handed over to the wait function.
 *
 * @since SINCE
 */
public class ProbeTest {

  /**
   * Remembers spy-mock on wait function.
   */
  private static final ThreadLocal<WaitFunction<SystemUnderTest, State>>
      TL_WAIT_FUNCTION =
      new ThreadLocal<>();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TestName testName = new TestName();

/*
  @Test
  public void works_without_generic_types() throws Exception {
    SystemUnderTest systemUnderTest = new SystemUnderTest(State.RUNNING);

    probing(systemUnderTest)
        .withinMs(1000L)
        .assertThat(new GetSystemState(),
                    Matchers.equalTo(State.RUNNING));
  }
*/

  @Test
  public void throw_assertion_error_on_failure_no_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);
    try {
      configuredProbe.assertThat(new GetSystemState(), Matchers.equalTo(State.RUNNING));
      fail("AssertionError should have been thrown");
    } catch (AssertionError e) {
      assertThat("All required information should be contained in exception message.",
                 e.getMessage(),
                 allOf(not(containsString("null")),
                       containsString(valueOf(State.RUNNING)),
                       containsString(valueOf(State.STOPPED))));
    }
  }

  @Test
  public void throw_assertion_error_on_failure_with_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    String message = testName.getMethodName();
    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);
    try {
      configuredProbe.assertThat(message, new GetSystemState(), Matchers.equalTo(State.RUNNING));
      fail("AssertionError should have been thrown");
    } catch (AssertionError e) {
      assertThat("All required information should be contained in exception message.",
                 e.getMessage(),
                 allOf(containsString(message),
                       containsString(valueOf(State.RUNNING)),
                       containsString(valueOf(State.STOPPED))));
    }
  }

  @Test
  public void throw_assumption_violated_exception_on_failure_no_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);
    try {
      configuredProbe.assumeThat(new GetSystemState(),
                                 Matchers.equalTo(State.RUNNING));
      fail("AssumptionViolatedException should have been thrown");
    } catch (AssumptionViolatedException e) {
      assertThat("All required information should be contained in exception message.",
                 e.getMessage(),
                 allOf(not(containsString("null")),
                       containsString(valueOf(State.RUNNING)),
                       containsString(valueOf(State.STOPPED))));
    }
  }

  @Test
  public void throw_assumption_violated_exception_on_failure_with_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);
    try {
      configuredProbe.assumeThat(message,
                                 new GetSystemState(),
                                 Matchers.equalTo(State.RUNNING));
      fail("AssumptionViolatedException should have been thrown");
    } catch (AssumptionViolatedException e) {
      assertThat("All required information should be contained in exception message.",
                 e.getMessage(),
                 allOf(containsString(message),
                       containsString(valueOf(State.RUNNING)),
                       containsString(valueOf(State.STOPPED))));
    }
  }

  @Test
  public void throw_wait_timeout_exception_on_failure_no_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectMessage(allOf(not(containsString("null")),
                                          containsString(valueOf(State.RUNNING)),
                                          containsString(valueOf(State.STOPPED))));

    configuredProbe.requireThat(new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));
  }

  @Test
  public void throw_wait_timeout_exception_on_failure_with_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest();

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectMessage(allOf(containsString(message),
                                          containsString(valueOf(State.RUNNING)),
                                          containsString(valueOf(State.STOPPED))));

    configuredProbe.requireThat(message,
                                new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));
  }

  @Test
  public void pass_on_second_try() throws Exception {
    List<Long> usedTimeMillis = Arrays.asList(1000L, 2000L);
    SystemUnderTest systemUnderTest = new SystemUnderTest(State.STOPPED, State.RUNNING);

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withinMs(1000L);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(message,
                                new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    Mockito.verify(spy, times(1)).sleep(anyLong());
  }

  @Test
  public void deceleration_factor_is_forwarded_to_wait_function() throws Exception {
    List<Long> usedTimeMillis = Arrays.asList(3L, 5L, 7L);
    SystemUnderTest
        systemUnderTest =
        new SystemUnderTest(State.STOPPED, State.STOPPED, State.RUNNING);

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelay(1, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(2.0d)
            .and()
            .within(1L, TimeUnit.SECONDS);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(message,
                                new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Delays accelerate (thus polling decelerates).", argument.getAllValues(),
               Matchers.equalTo(Arrays.asList(3L, 6L)));
  }

  @Test
  public void initial_delay_2arg_is_forwarded_to_wait_function() throws Exception {
    List<Long> usedTimeMillis = Arrays.asList(3L, 5L, 200L, 7L);
    SystemUnderTest
        systemUnderTest =
        new SystemUnderTest(State.STOPPED, State.STOPPED, State.STARTING, State.RUNNING);

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelay(100L, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(1.5d)
            .and()
            .within(1L, TimeUnit.SECONDS);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Initial delay is respected and eventually overridden when system becomes slow.",
               argument.getAllValues(),
               Matchers.equalTo(Arrays.asList(100L, 150L, 225L)));
  }

  @Test
  public void initial_delay_1arg_is_forwarded_to_wait_function() throws Exception {
    long initialDelayMs = 100L;
    double decelerationFactor = 1.5d;
    long slowSystemMs = 300L;

    List<Long> usedTimeMillis = Arrays.asList(3L, 5L, slowSystemMs, 7L);
    SystemUnderTest
        systemUnderTest =
        new SystemUnderTest(State.STOPPED, State.STOPPED, State.STARTING, State.RUNNING);

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(1L, TimeUnit.SECONDS);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Initial delay is respected and eventually overridden when system becomes slow.",
               argument.getAllValues(),
               Matchers.equalTo(Arrays.asList(
                   initialDelayMs,
                   Math.round(initialDelayMs * decelerationFactor),
                   slowSystemMs)));
  }

  @Test
  public void grace_period_1arg_is_respected() throws Exception {
    long initialDelayMs = 0L;
    long gracePeriodMs = 50L;
    double decelerationFactor = 1d;

    List<Long> usedTimeMillis = Arrays.asList(100L, 100L);
    SystemUnderTest systemUnderTest = new SystemUnderTest(State.STOPPED, State.RUNNING);

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .withFinalGracePeriodMs(gracePeriodMs)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(140L, TimeUnit.MILLISECONDS);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(message,
                                new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    assertThat(
        "Without grace period only 40 ms would remain after first poll. With grace we should get 90 ms.",
        argument.getAllValues(),
        Matchers.equalTo(Collections.singletonList(90L)));
  }

  @Test
  public void grace_period_2arg_is_respected() throws Exception {
    long initialDelayMs = 0L;
    long gracePeriodMs = 50L;
    double decelerationFactor = 1d;

    List<Long> usedTimeMillis = Arrays.asList(100L, 100L);
    SystemUnderTest systemUnderTest = new SystemUnderTest(State.STOPPED, State.RUNNING);

    String message = testName.getMethodName();

    Probe.ProbeBuilder<SystemUnderTest, State> configuredProbe =
        Probe.<SystemUnderTest, State>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .withFinalGracePeriod(gracePeriodMs, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(140L, TimeUnit.MILLISECONDS);
    spyOnWaitFunction((Probe.ProbeBuilderImpl<SystemUnderTest, State>) configuredProbe,
                      usedTimeMillis);

    configuredProbe.requireThat(message,
                                new GetSystemState(),
                                Matchers.equalTo(State.RUNNING));

    WaitFunction<SystemUnderTest, State> spy = TL_WAIT_FUNCTION.get();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    assertThat(
        "Without grace period only 40 ms would remain after first poll. With grace we should get 90 ms.",
        argument.getAllValues(),
        Matchers.equalTo(Collections.singletonList(90L)));
  }

  /**
   * Creates spy on wait function. Spy won't wait and the system time returned will be build from
   * the given used time millis. So you only need to specify how long each call to the system will
   * take.
   *
   * @param configuredProbe probe to configure wait function for
   * @param usedTimeMillis  array of used times in milliseconds
   */
  private void spyOnWaitFunction(
      Probe.ProbeBuilderImpl<SystemUnderTest, State> configuredProbe,
      List<Long> usedTimeMillis) {
    final List<Long> timeMillis = getTimeMillis(usedTimeMillis);

    configuredProbe.preProcessWaitFunction(
        new Function<WaitFunction<SystemUnderTest, State>, WaitFunction<SystemUnderTest, State>>() {
          @Override
          public WaitFunction<SystemUnderTest, State> apply(
              WaitFunction<SystemUnderTest, State> input) {
            WaitFunction<SystemUnderTest, State> spy = Mockito.spy(input);
            try {
              Mockito.doNothing().when(spy).sleep(anyLong());
              Mockito.doAnswer(
                  AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();
            } catch (InterruptedException ignored) {
            }
            TL_WAIT_FUNCTION.set(spy);
            return spy;
          }
        });
  }

  /**
   * Transform list of used time millis to the system millis returned during wait function. Thus
   * this method internally knows exactly how many times the system millis are queried and needs
   * to be adopted if this after changes.
   *
   * @param usedTimeMillis how long each call to the system takes
   * @return time millis required for mocking wait function
   */
  @NotNull
  private List<Long> getTimeMillis(List<Long> usedTimeMillis) {
    final List<Long> timeMillis = new ArrayList<>();
    long currentTime = 0L;
    // start time to calculate timeout time
    timeMillis.add(currentTime);
    for (Long usedTimeMilli : usedTimeMillis) {
      // time before evaluation
      timeMillis.add(currentTime);
      currentTime = currentTime + usedTimeMilli;
      // time after evaluation
      timeMillis.add(currentTime);
    }
    return timeMillis;
  }

  private enum State {
    STARTING,
    RUNNING,
    STOPPED
  }

  private static class SystemUnderTest {

    @NotNull
    private final Deque<State> states;

    @NotNull
    private State state = State.STOPPED;

    private SystemUnderTest(@NotNull State... states) {
      this(new ArrayDeque<>(Arrays.asList(states)));
    }

    private SystemUnderTest(@NotNull Deque<State> states) {
      this.states = states;
    }

    @NotNull
    public State getState() {
      if (!states.isEmpty()) {
        state = states.pop();
      }
      return state;
    }
  }

  private static class GetSystemState implements Function<SystemUnderTest, State> {

    @Override
    public State apply(SystemUnderTest input) {
      return input.getState();
    }
  }
}
