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

import static com.github.mmichaelis.hamcrest.nextdeed.glue.DescribedFunction.describe;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;
import org.mockito.AdditionalAnswers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests {@link WaitFunction}.
 *
 * @since 1.0.0
 */
public class WaitFunctionTest {

  @Rule
  public TestName testName = new TestName();
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Test
  public void without_predicate_accept_any_value_immediately() throws Exception {
    WaitFunction<Void, String>
        waitFunction =
        (WaitFunction<Void, String>) WaitFunction
            .waitFor(new Function<Void, String>() {
              @Override
              public String apply(Void input) {
                return testName.getMethodName();
              }
            })
            .get();
    WaitFunction<Void, String> spy = Mockito.spy(waitFunction);

    String result = spy.apply(null);

    assertThat(result, Matchers.equalTo(testName.getMethodName()));
    // Sleep not expected because of immediate success.
    Mockito.verify(spy, VerificationModeFactory.atMost(0)).sleep(Mockito.anyLong());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void without_on_timeout_fail_with_timeout_exception() throws Exception {
    String inputValue = "Lorem";
    String functionName = "Ipsum";

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return testName.getMethodName();
              }
            }).as(functionName))
            .toFulfill(Predicates.<String>alwaysFalse())
            .within(0L, TimeUnit.MILLISECONDS)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectMessage(testName.getMethodName());
    expectedException.expectMessage(inputValue);
    expectedException.expectMessage(functionName);

    spy.apply(inputValue);
  }

  @Test
  public void pass_on_second_try() throws Exception {
    final Deque<Boolean> predicateAnswers = new ArrayDeque<>(Arrays.asList(false, true));
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return predicateAnswers.pop();
              }
            })
            .within(1000L, TimeUnit.MILLISECONDS)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doReturn(0L).when(spy).nowMillis();

    String result = spy.apply(inputValue);

    assertThat(result, Matchers.equalTo(testName.getMethodName()));
    Mockito.verify(spy, VerificationModeFactory.times(1)).sleep(Mockito.anyLong());
  }

  @Test
  public void adopt_new_delay_on_long_duration() throws Exception {
    final Deque<Boolean> predicateAnswers = new ArrayDeque<>(Arrays.asList(false, true));
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";
    List<Long> timeMillis =
        Arrays.asList(
            // used to determine deadline
            0L,
            // used to determine start time
            0L,
            // used to evaluate time after evaluation
            10L);

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return predicateAnswers.pop();
              }
            })
            .withInitialDelay(1, TimeUnit.MILLISECONDS)
            .within(1000L, TimeUnit.MILLISECONDS)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doAnswer(AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();

    spy.apply(inputValue);

    Mockito.verify(spy, VerificationModeFactory.times(1)).sleep(10L);
  }

  @Test
  public void decelerate_polling_on_repetitive_calls() throws Exception {
    final Deque<Boolean> predicateAnswers = new ArrayDeque<>(Arrays.asList(false, false, true));
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";
    List<Long> timeMillis =
        Arrays.asList(
            // used to determine deadline
            0L,
            // used to determine start time
            0L,
            // used to evaluate time after evaluation
            10L,
            // next time before evaluation
            10L,
            // next time after evaluation
            20L);

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return predicateAnswers.pop();
              }
            })
            .withInitialDelay(1, TimeUnit.MILLISECONDS)
            .within(1000L, TimeUnit.MILLISECONDS)
            .and()
            .deceleratePollingBy(1.5)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doAnswer(AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();

    spy.apply(inputValue);

    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Delays accelerate (thus polling decelerates).", argument.getAllValues(),
               Matchers.equalTo(Arrays.asList(10L, 15L)));
  }

  @Test
  public void at_least_decelerate_by_one_millisecond() throws Exception {
    final Deque<Boolean> predicateAnswers = new ArrayDeque<>(Arrays.asList(false, false, true));
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";
    List<Long> timeMillis =
        Arrays.asList(
            // used to determine deadline
            0L,
            // used to determine start time
            0L,
            // used to evaluate time after evaluation
            1L,
            // next time before evaluation
            1L,
            // next time after evaluation
            2L);

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return predicateAnswers.pop();
              }
            })
            .withInitialDelay(1, TimeUnit.MILLISECONDS)
            .within(1000L, TimeUnit.MILLISECONDS)
            .and()
            .deceleratePollingBy(1.1d)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doAnswer(AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();

    spy.apply(inputValue);

    ArgumentCaptor<Long> argument = ArgumentCaptor.forClass(Long.class);
    InOrder inOrder = Mockito.inOrder(spy);
    inOrder.verify(spy).sleep(argument.capture());
    inOrder.verify(spy).sleep(argument.capture());
    assertThat("Delays accelerate (thus polling decelerates) by at least one millisecond.",
               argument.getAllValues(),
               Matchers.equalTo(Arrays.asList(1L, 2L)));
  }

  @Test
  public void fail_on_interrupt_during_sleep() throws Exception {
    final Deque<Boolean> predicateAnswers = new ArrayDeque<>(Arrays.asList(false, true));
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(new Predicate<String>() {
              @Override
              public boolean apply(String input) {
                return predicateAnswers.pop();
              }
            })
            .within(1000L, TimeUnit.MILLISECONDS)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doThrow(InterruptedException.class).when(spy).sleep(Mockito.anyLong());
    Mockito.doReturn(0L).when(spy).nowMillis();

    expectedException.expect(IllegalStateException.class);
    expectedException.expectCause(Matchers.<Throwable>instanceOf(InterruptedException.class));

    spy.apply(inputValue);
  }

  @Test
  public void withinMs_configures_timeout_in_milliseconds() throws Exception {
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";
    List<Long> timeMillis =
        Arrays.asList(
            // init: determine deadline
            0L,
            // cycle 1: determine start time
            0L,
            // cycle 1: determine end time
            9L,
            // cycle 2: determine start time
            9L,
            // cycle 2: determine end time -- timeout
            11L);
    long timeoutMs = 10L;

    StoreTimeoutEvent<String, String> timeoutFunction = new StoreTimeoutEvent<>();

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(Predicates.<String>alwaysFalse())
            .withinMs(timeoutMs)
            .onTimeout(timeoutFunction)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doAnswer(AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();

    spy.apply(inputValue);

    assertThat("Consumed millis greater than or equal to timeout.",
               timeoutFunction.getLastEvent().getConsumedMs(),
               Matchers.greaterThanOrEqualTo(timeoutMs));
  }

  @Test
  public void raised_timeout_event_contains_all_relevant_information() throws Exception {
    String inputValue = "Lorem";
    final String outputValue = testName.getMethodName();
    String functionName = "Ipsum";
    long expectedConsumedMs = 11;
    List<Long> timeMillis =
        Arrays.asList(
            // init: determine deadline
            5L,
            // cycle 1: determine start time
            5L,
            // cycle 1: determine end time
            5L + expectedConsumedMs);
    long timeoutMs = 10L;

    StoreTimeoutEvent<String, String> timeoutFunction = new StoreTimeoutEvent<>();

    WaitFunction<String, String>
        waitFunction =
        (WaitFunction<String, String>) WaitFunction
            .waitFor(describe(new Function<String, String>() {
              @Override
              public String apply(String input) {
                return outputValue;
              }
            }).as(functionName))
            .toFulfill(Predicates.<String>alwaysFalse())
            .withinMs(timeoutMs)
            .onTimeout(timeoutFunction)
            .get();
    WaitFunction<String, String> spy = Mockito.spy(waitFunction);

    Mockito.doNothing().when(spy).sleep(Mockito.anyLong());
    Mockito.doAnswer(AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();

    spy.apply(inputValue);

    WaitTimeoutEvent<String, String> event = timeoutFunction.getLastEvent();
    String eventDescription = event.describe();
    String eventToString = event.toString();

    // Validate fields
    errorCollector.checkThat("Event contains item function got applied to.",
                             event.getItem(),
                             Matchers.equalTo(inputValue));
    errorCollector.checkThat("Event contains WaitFunction which caused the event.",
                             event.getSource(),
                             Matchers.sameInstance(spy));
    errorCollector.checkThat("Event contains result of last apply of function.",
                             event.getLastResult(),
                             Matchers.equalTo(outputValue));
    errorCollector.checkThat("Event contains information on consumed milliseconds.",
                             event.getConsumedMs(),
                             Matchers.equalTo(expectedConsumedMs));

    // Validate description
    errorCollector.checkThat("Event description contains item function got applied to.",
                             eventDescription,
                             Matchers.containsString(event.getItem()));
    errorCollector.checkThat("Event description contains result of last apply of function.",
                             eventDescription,
                             Matchers.containsString(event.getLastResult()));
    errorCollector.checkThat("Event description contains information on consumed milliseconds.",
                             eventDescription,
                             Matchers.containsString(Long.toString(event.getConsumedMs())));

    // Validate toString
    errorCollector.checkThat("Event toString contains WaitFunction which caused the event.",
                             eventToString,
                             Matchers.containsString(String.valueOf(event.getSource())));
    errorCollector.checkThat("Event toString contains item function got applied to.",
                             eventToString,
                             Matchers.containsString(event.getItem()));
    errorCollector.checkThat("Event toString contains result of last apply of function.",
                             eventToString,
                             Matchers.containsString(event.getLastResult()));
    errorCollector.checkThat("Event toString contains information on consumed milliseconds.",
                             eventToString,
                             Matchers.containsString(Long.toString(event.getConsumedMs())));
  }

  private static class StoreTimeoutEvent<T, R> implements Function<WaitTimeoutEvent<T, R>, R> {
    private WaitTimeoutEvent<T, R> lastEvent;

    @Override
    public R apply(@Nullable WaitTimeoutEvent<T, R> input) {
      assert input != null : "null unexpected here";
      lastEvent = input;
      return input.getLastResult();
    }

    public WaitTimeoutEvent<T, R> getLastEvent() {
      return lastEvent;
    }

  }
}
