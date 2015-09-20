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

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link FailureMessage}.
 *
 * @since SINCE
 */
public class FailureMessageTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @SuppressWarnings("ConstantConditions")
  @Test
  public void constructor_failOnNullMatcher() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("matcher");
    new FailureMessage<String>(null, null, null);
  }

  @Test
  public void getMessage_dealWithLastResultNull() throws Exception {
    FailureMessage<String> message =
        new FailureMessage<>(null, "some Reason", containsString("contained String"));
    String builtMessage = message.getMessage();
    assertThat(builtMessage, containsString("but: was null"));
  }

  @Test
  public void getMessage_dealWithReasonNull() throws Exception {
    FailureMessage<String> message =
        new FailureMessage<>("probed String", null, containsString("contained String"));
    String builtMessage = message.getMessage();
    assertThat("Empty reason just should not appear in message.",
               builtMessage,
               Matchers.not(containsString("null")));
  }

  @Test
  public void getMessage_mentionLastResult() throws Exception {
    String expected = "probed String";
    FailureMessage<String> message =
        new FailureMessage<>(expected, "some Reason", containsString("contained String"));
    String builtMessage = message.getMessage();
    assertThat("Last Result should be mentioned in message.",
               builtMessage,
               containsString(expected));
  }

  @Test
  public void getMessage_mentionReason() throws Exception {
    String expected = "some Reason";
    FailureMessage<String> message =
        new FailureMessage<>("probed String", expected, containsString("contained String"));
    String builtMessage = message.getMessage();
    assertThat("Last Result should be mentioned in message.",
               builtMessage,
               containsString(expected));
  }

  @Test
  public void getMessage_respectMatcherDescription() throws Exception {
    String expected = "contained String";
    FailureMessage<String> message =
        new FailureMessage<>("probed String", "some Reason", containsString(expected));
    String builtMessage = message.getMessage();
    assertThat("Matcher description should be part of message.",
               builtMessage,
               containsString(expected));
  }

  @Test
  public void toString_shouldContainRelevantInformation() throws Exception {
    String lastResult = "probed String";
    String reason = "some Reason";
    String matcherText = "contained String";
    FailureMessage<String> message =
        new FailureMessage<>(lastResult, reason, containsString(matcherText));
    String str = message.toString();
    assertThat(str,
               Matchers.allOf(
                   containsString(lastResult),
                   containsString(reason),
                   containsString(matcherText)
               )
    );
  }
}
