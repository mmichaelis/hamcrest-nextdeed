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

import static com.github.mmichaelis.hamcrest.nextdeed.TestFunctions.messageFunction;
import static com.github.mmichaelis.hamcrest.nextdeed.function.ApplyingMatcher.applying;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Tests {@link FailWithTimeoutExceptionFunction}.
 *
 * @since 1.0.0
 */
@RunWith(MockitoJUnitRunner.class)
public class ThrowWaitTimeoutExceptionStrategyTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private WaitTimeoutEvent<String, String> event;

  @Mock
  private Matcher<String> someMatcher;

  private Function<WaitTimeoutEvent<String, String>, String> timeoutExceptionFunction;
  private String failureMessage;
  private String matcherDescription;

  @Before
  public void setUp() throws Exception {
    failureMessage = "some message";
    matcherDescription = "expected string";
    timeoutExceptionFunction = new ThrowWaitTimeoutExceptionStrategy<>(failureMessage,
                                                               containsString(matcherDescription));
  }

  @Test
  public void useTimeoutEventDescriptionForMessage() throws Exception {
    String theMessage = "last result";
    Mockito.doReturn(theMessage).when(event).getLastResult();

    expectedException.expect(RuntimeException.class);
    expectedException.expectCause(allOf(
        Matchers.<Throwable>instanceOf(WaitTimeoutException.class),
        applying(messageFunction(),
                 allOf(
                     containsString(failureMessage),
                     containsString(theMessage),
                     containsString(matcherDescription)
                 ))
    ));
    try {
      timeoutExceptionFunction.apply(event);
    } catch (WaitTimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void hasToString() throws Exception {
    assertThat(timeoutExceptionFunction, Matchers.hasToString(allOf(
        containsString(failureMessage),
        containsString(matcherDescription)
    )));
  }
}
