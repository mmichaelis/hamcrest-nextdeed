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

import static com.github.mmichaelis.hamcrest.nextdeed.concurrent.OnWaitFunctionSpy.spyOnWaitFunction;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassModifierMatcher.classModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaDefaultConstructor.isInstantiableViaDefaultConstructor;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.MemberModifierMatcher.memberModifierContains;
import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyLong;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import com.github.mmichaelis.hamcrest.nextdeed.glue.Consumer;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests {@link Probe} or more specifically
 * {@link ProbeBuilder}. The main focus
 * is to test that the given arguments are correctly handed over to the wait function.
 *
 * @since 1.0.0
 */
@SuppressWarnings("ThrowableResultOfMethodCallIgnored")
@RunWith(Parameterized.class)
public class ProbeTest {

  @NotNull
  private final ProbeFacadeMode mode;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();
  @Rule
  public TestName testName = new TestName();

  public ProbeTest(@NotNull ProbeFacadeMode mode) {
    this.mode = mode;
  }

  @Parameters(name = "{index}: Mode {0}")
  public static Collection<Object[]> data() {
    return asList(new Object[][]{
        {ProbeFacadeMode.ASSERT},
        {ProbeFacadeMode.ASSUME},
        {ProbeFacadeMode.REQUIRE},
    });
  }

  @Test
  public void throw_exception_on_failure_no_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest_SUT systemUnderTest = new SystemUnderTest_SUT();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                      usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("Exception should have been thrown.", result, notNullValue());
    assertThat("All required information should be contained in exception message.",
               result.getMessage(),
               allOf(not(containsString("null")),
                     containsString(valueOf(SystemState.RUNNING)),
                     containsString(valueOf(SystemState.STOPPED))));
  }

  @Test
  public void inform_timeout_handlers_on_failure() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest_SUT systemUnderTest = new SystemUnderTest_SUT();

    WaitTimeoutEventConsumer eventConsumer1 = new WaitTimeoutEventConsumer();
    WaitTimeoutEventConsumer eventConsumer2 = new WaitTimeoutEventConsumer();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .onTimeout(eventConsumer1)
            .onTimeout(eventConsumer2)
            .withinMs(0L);
    spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                      usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("Consumer 1 should have been informed.", eventConsumer1.getEvent(), notNullValue());
    assertThat("Consumer 2 should have been informed.", eventConsumer2.getEvent(), notNullValue());

    assertThat("Event should contain last result before failure.",
               eventConsumer1.getEvent().getLastResult(), is(SystemState.STOPPED));

    assertThat("Exception should have been thrown.", result, notNullValue());
    assertThat("All required information should be contained in exception message.",
               result.getMessage(),
               allOf(not(containsString("null")),
                     containsString(valueOf(SystemState.RUNNING)),
                     containsString(valueOf(SystemState.STOPPED))));
  }

  @Test
  public void throw_exception_on_failure_with_message() throws Exception {
    List<Long> usedTimeMillis = Collections.singletonList(1000L);
    SystemUnderTest_SUT systemUnderTest = new SystemUnderTest_SUT();
    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(0L)
            .withinMs(0L);
    spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                      usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("Exception should have been thrown.", result, notNullValue());
    assertThat("All required information should be contained in exception message.",
               result.getMessage(),
               allOf(containsString(message),
                     containsString(valueOf(SystemState.RUNNING)),
                     containsString(valueOf(SystemState.STOPPED))));
  }

  @Test
  public void pass_on_second_try_with_message() throws Exception {
    List<Long> usedTimeMillis = asList(1000L, 2000L);
    SystemUnderTest_SUT
        systemUnderTest = new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.RUNNING);

    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withinMs(1000L);
    OnWaitFunctionSpy functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    Mockito.verify(spy, times(1)).sleep(anyLong());
  }

  @Test
  public void pass_on_second_try_without_message() throws Exception {
    List<Long> usedTimeMillis = asList(1000L, 2000L);
    SystemUnderTest_SUT
        systemUnderTest = new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.RUNNING);

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withinMs(1000L);
    OnWaitFunctionSpy functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    Mockito.verify(spy, times(1)).sleep(anyLong());
  }

  @Test
  public void pass_on_eventual_match() throws Exception {
    List<Long> usedTimeMillis = asList(1000L, 2000L);
    SystemUnderTest_SUT systemUnderTest =
        new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.RUNNING, SystemState.RUNNING);

    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withinMs(1000L);
    spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                      usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), new SameStateTwice());

    assertThat("No exception should have been thrown.", result, nullValue());
  }

  @Test
  public void deceleration_factor_is_forwarded_to_wait_function() throws Exception {
    List<Long> usedTimeMillis = asList(3L, 5L, 7L);
    SystemUnderTest_SUT
        systemUnderTest =
        new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.STOPPED, SystemState.RUNNING);

    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelay(1, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(2.0d)
            .and()
            .within(1L, TimeUnit.SECONDS);
    OnWaitFunctionSpy functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Delays accelerate (thus polling decelerates).", argument.getAllValues(),
               equalTo(asList(3L, 6L)));
  }

  @Test
  public void initial_delay_2arg_is_forwarded_to_wait_function() throws Exception {
    List<Long> usedTimeMillis = asList(3L, 5L, 200L, 7L);
    SystemUnderTest_SUT
        systemUnderTest =
        new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.STOPPED, SystemState.STARTING,
                                SystemState.RUNNING);

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelay(100L, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(1.5d)
            .and()
            .within(1L, TimeUnit.SECONDS);
    OnWaitFunctionSpy
        functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Initial delay is respected and eventually overridden when system becomes slow.",
               argument.getAllValues(),
               equalTo(asList(100L, 150L, 225L)));
  }

  @Test
  public void initial_delay_1arg_is_forwarded_to_wait_function() throws Exception {
    long initialDelayMs = 100L;
    double decelerationFactor = 1.5d;
    long slowSystemMs = 300L;

    List<Long> usedTimeMillis = asList(3L, 5L, slowSystemMs, 7L);
    SystemUnderTest_SUT
        systemUnderTest =
        new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.STOPPED, SystemState.STARTING,
                                SystemState.RUNNING);

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(1L, TimeUnit.SECONDS);
    OnWaitFunctionSpy functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Initial delay is respected and eventually overridden when system becomes slow.",
               argument.getAllValues(),
               equalTo(asList(
                   initialDelayMs,
                   Math.round(initialDelayMs * decelerationFactor),
                   slowSystemMs)));
  }

  @Test
  public void grace_period_1arg_is_respected() throws Exception {
    long initialDelayMs = 0L;
    long gracePeriodMs = 50L;
    double decelerationFactor = 1d;

    List<Long> usedTimeMillis = asList(100L, 100L);
    SystemUnderTest_SUT
        systemUnderTest = new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.RUNNING);

    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .withFinalGracePeriodMs(gracePeriodMs)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(140L, TimeUnit.MILLISECONDS);
    OnWaitFunctionSpy
        functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    assertThat(
        "Without grace period only 40 ms would remain after first poll. With grace we should get 90 ms.",
        argument.getAllValues(),
        equalTo(Collections.singletonList(90L)));
  }

  @Test
  public void grace_period_2arg_is_respected() throws Exception {
    long initialDelayMs = 0L;
    long gracePeriodMs = 50L;
    double decelerationFactor = 1d;

    List<Long> usedTimeMillis = asList(100L, 100L);
    SystemUnderTest_SUT
        systemUnderTest = new SystemUnderTest_SUT(SystemState.STOPPED, SystemState.RUNNING);

    String message = testName.getMethodName();

    ProbeBuilder<SystemUnderTest_SUT, SystemState> configuredProbe =
        Probe.<SystemUnderTest_SUT, SystemState>probing(systemUnderTest)
            .withInitialDelayMs(initialDelayMs)
            .withFinalGracePeriod(gracePeriodMs, TimeUnit.MILLISECONDS)
            .deceleratePollingBy(decelerationFactor)
            .and()
            .within(140L, TimeUnit.MILLISECONDS);
    OnWaitFunctionSpy functionSpy =
        spyOnWaitFunction((ProbeBuilderImpl<SystemUnderTest_SUT, SystemState>) configuredProbe,
                          usedTimeMillis);

    Throwable result =
        new ProbeFacade<>(configuredProbe)
            .run(mode, message, new GetSystemState(), equalTo(SystemState.RUNNING));

    assertThat("No exception should have been thrown.", result, nullValue());

    WaitFunction<SystemUnderTest_SUT, SystemState> spy = functionSpy.getWaitFunction();
    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    assertThat(
        "Without grace period only 40 ms would remain after first poll. With grace we should get 90 ms.",
        argument.getAllValues(),
        equalTo(Collections.singletonList(90L)));
  }

  @Test
  public void probeBuilder_has_toString() throws Exception {
    Random random = new Random(0);

    long initialDelayMs = random.nextInt(1000);
    double decelerationFactor = Math.abs(random.nextDouble() - 1d) + 1;
    long gracePeriodMs = random.nextInt(1000);
    long timeoutMs = random.nextInt(1000);

    ProbeBuilder<String, String> configuredProbe =
        Probe.<String, String>probing(testName.getMethodName())
            .withInitialDelayMs(initialDelayMs)
            .deceleratePollingBy(decelerationFactor)
            .withFinalGracePeriodMs(gracePeriodMs)
            .withinMs(timeoutMs);
    assertThat("toString shows all configured values.",
               configuredProbe,
               hasToString(allOf(
                               containsString(configuredProbe.getClass().getSimpleName()),
                               containsString(testName.getMethodName()),
                               containsString(valueOf(initialDelayMs)),
                               containsString(valueOf(decelerationFactor)),
                               containsString(valueOf(gracePeriodMs)),
                               containsString(valueOf(timeoutMs))
                           )
               )
    );
  }

  @Test
  public void probeIsUtilityClass() throws Exception {
    errorCollector.checkThat("Class must be final.",
                             Probe.class,
                             classModifierContains(Modifier.FINAL));
    errorCollector.checkThat("Any constructors must be private.",
                             asList(Probe.class.getDeclaredConstructors()),
                             everyItem(memberModifierContains(Modifier.PRIVATE)));
    assertThat("Default constructor must exist.",
               Probe.class,
               isInstantiableViaDefaultConstructor());
  }

  private enum ProbeFacadeMode {
    ASSERT,
    ASSUME,
    REQUIRE
  }

  private static final class ProbeFacade<T, R>
      implements ProbeAssert<T, R>, ProbeAssume<T, R>, ProbeRequire<T, R> {

    @NotNull
    private final ProbeBuilder<T, R> delegateProbeBuilder;

    private ProbeFacade(@NotNull ProbeBuilder<T, R> delegateProbeBuilder) {
      this.delegateProbeBuilder = delegateProbeBuilder;
    }

    @Override
    public void assertThat(@NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.assertThat(actualFunction, matcher);
    }

    @Override
    public void assertThat(@Nullable String reason, @NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.assertThat(reason, actualFunction, matcher);
    }

    @Override
    public void assumeThat(@NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.assumeThat(actualFunction, matcher);
    }

    @Override
    public void assumeThat(@Nullable String reason, @NotNull Function<T, R> actualFunction,
                           @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.assumeThat(reason, actualFunction, matcher);
    }

    @Override
    public void requireThat(@NotNull Function<T, R> actualFunction,
                            @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.requireThat(actualFunction, matcher);
    }

    @Override
    public void requireThat(@Nullable String reason, @NotNull Function<T, R> actualFunction,
                            @NotNull Matcher<? super R> matcher) {
      delegateProbeBuilder.requireThat(reason, actualFunction, matcher);
    }

    @Nullable
    public Throwable run(@NotNull ProbeFacadeMode mode,
                         @NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
      try {
        switch (mode) {
          case ASSERT:
            assertThat(actualFunction, matcher);
            break;
          case ASSUME:
            assumeThat(actualFunction, matcher);
            break;
          case REQUIRE:
            requireThat(actualFunction, matcher);
            break;
        }
      } catch (Throwable e) {
        return e;
      }
      return null;
    }

    @Nullable
    public Throwable run(@NotNull ProbeFacadeMode mode,
                         @Nullable String reason,
                         @NotNull Function<T, R> actualFunction,
                         @NotNull Matcher<? super R> matcher) {
      try {
        switch (mode) {
          case ASSERT:
            assertThat(reason, actualFunction, matcher);
            break;
          case ASSUME:
            assumeThat(reason, actualFunction, matcher);
            break;
          case REQUIRE:
            requireThat(reason, actualFunction, matcher);
            break;
        }
      } catch (Throwable e) {
        return e;
      }
      return null;
    }
  }

  private static class SameStateTwice extends TypeSafeMatcher<SystemState> {

    private AtomicReference<SystemState> previousState = new AtomicReference<>();

    @Override
    public void describeTo(Description description) {
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("hash", Integer.toHexString(System.identityHashCode(this)))
          .add("super", super.toString())
          .add("previousState", previousState)
          .toString();
    }

    @Override
    protected boolean matchesSafely(SystemState item) {
      return item == previousState.getAndSet(item);
    }
  }

  private static class WaitTimeoutEventConsumer
      implements Consumer<WaitTimeoutEvent<SystemUnderTest_SUT, SystemState>> {

    private WaitTimeoutEvent<SystemUnderTest_SUT, SystemState> event;

    @Override
    public void accept(WaitTimeoutEvent<SystemUnderTest_SUT, SystemState> event) {
      this.event = event;
    }

    public WaitTimeoutEvent<SystemUnderTest_SUT, SystemState> getEvent() {
      return event;
    }
  }
}
