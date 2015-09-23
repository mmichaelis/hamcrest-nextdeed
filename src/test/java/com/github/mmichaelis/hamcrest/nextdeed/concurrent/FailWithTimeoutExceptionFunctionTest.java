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

import static org.hamcrest.Matchers.nullValue;

import com.google.common.base.Function;

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
public class FailWithTimeoutExceptionFunctionTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private WaitTimeoutEvent<String, String> event;

  private Function<WaitTimeoutEvent<String, String>, String> timeoutExceptionFunction;

  @Before
  public void setUp() throws Exception {
    timeoutExceptionFunction = new FailWithTimeoutExceptionFunction<>();
  }

  @Test
  public void handleNullArgumentGracefully() throws Exception {
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectMessage(nullValue(String.class));
    timeoutExceptionFunction.apply(null);
  }

  @Test
  public void useTimeoutEventDescriptionForMessage() throws Exception {
    String theMessage = "My Message";
    Mockito.doReturn(theMessage).when(event).describe();

    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectMessage(theMessage);

    timeoutExceptionFunction.apply(event);
  }
}
