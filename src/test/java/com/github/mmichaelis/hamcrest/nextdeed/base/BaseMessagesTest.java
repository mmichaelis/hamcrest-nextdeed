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

package com.github.mmichaelis.hamcrest.nextdeed.base;

import static com.github.mmichaelis.hamcrest.nextdeed.base.BaseMessages.MESSAGES;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;

import com.github.mmichaelis.hamcrest.nextdeed.base.messages.BaseMessagesTestMessages;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.MissingResourceException;
import java.util.concurrent.Callable;

/**
 * Tests {@link BaseMessages}.
 *
 * @since SINCE
 */
public class BaseMessagesTest {

  private static final Class<BaseMessagesTestMessages>
      MESSAGE_CLASS =
      BaseMessagesTestMessages.class;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void backedPropertyValueShouldBeProvided() throws Exception {
    String message = messages().backedBundleProperty();
    assertThat(message, equalTo("backed bundle property"));
  }

  @Test
  public void canGetRawPropertyValueDuringTestsForBackedPropertyValue() throws Exception {
    try (AutoCloseable ignored = MESSAGES.overrideAsRaw(MESSAGE_CLASS)) {
      String message = messages().backedBundleProperty();
      assertThat(message, equalTo("backedBundleProperty()"));
    }
  }

  @Test
  public void argumentsShouldBeResolved() throws Exception {
    String theArgument = "the argument";
    String message = messages().withArgumentProperty(theArgument);
    assertThat(message, equalTo("with argument property: the argument"));
  }

  @Test
  public void canGetRawPropertyValueDuringTestsForArgumentPropertyValue() throws Exception {
    try (AutoCloseable ignored = MESSAGES.overrideAsRaw(MESSAGE_CLASS)) {
      String theArgument = "the argument";
      String message = messages().withArgumentProperty(theArgument);
      assertThat(message, equalTo("withArgumentProperty(the argument)"));
    }
  }

  @Test
  public void canExecuteFunctionWithRawPropertyValues() throws Exception {
    BaseMessages.withRawMessages(
        MESSAGE_CLASS,
        new Callable<Void>() {
          @Override
          public Void call() throws Exception {
            String theArgument = "the argument";
            String message = messages().withArgumentProperty(theArgument);
            assertThat(message, equalTo("withArgumentProperty(the argument)"));
            return null;
          }
        }
    ).call();
  }

  @Test
  public void failForUnbackedProperty() throws Exception {
    expectedException.expect(MissingResourceException.class);
    expectedException.expectMessage("undefinedBundleProperty");
    messages().undefinedBundleProperty();
  }

  @Test
  public void failOnBadMessagePattern() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    expectedException.expectMessage("x30");
    String theArgument = "the argument";
    messages().invalidMessagePattern(theArgument);
  }

  @Test
  public void failOnBadMessageArgument() throws Exception {
    expectedException.expect(IllegalArgumentException.class);
    String theArgument = "the argument";
    messages().withIntegerArgumentProperty(theArgument);
  }

  @Test
  public void providesRelevantInformationInToString() throws Exception {
    assertThat(MESSAGES, hasToString(Matchers.containsString("proxyCache")));
  }

  private BaseMessagesTestMessages messages() {
    return MESSAGES.of(MESSAGE_CLASS);
  }

}
