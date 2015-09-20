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

package com.github.mmichaelis.hamcrest.nextdeed.reflect;

import static java.lang.String.format;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.lang.reflect.Modifier;

/**
 * Tests {@link ClassModifierMatcher}.
 *
 * @since SINCE
 */
public class ClassModifierMatcherTest {

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Test
  public void modifierIs_failureMessageContainsActualAndExpectedModifiers() throws Exception {
    int expectedModifier = Modifier.ABSTRACT | Modifier.FINAL;
    try {
      assertThat(ModifierClassUnderTest.class,
                 ClassModifierMatcher.classModifierIs(expectedModifier));
    } catch (AssertionError e) {
      String message = e.getMessage();
      errorCollector.checkThat("Expected modifiers contained in message.", message,
                               containsString(
                                   format("\"%s\"", Modifier.toString(expectedModifier))));
      errorCollector.checkThat("Actual modifiers contained in message.", message,
                               containsString(
                                   format("\"%s\"", Modifier.toString(
                                       ModifierClassUnderTest.class.getModifiers()))));
      return;
    }
    fail("Assertion should have failed.");
  }

  @Test
  public void modifierContains_failureMessageContainsActualAndExpectedModifiers() throws Exception {
    int expectedModifier = Modifier.ABSTRACT | Modifier.FINAL;
    try {
      assertThat(ModifierClassUnderTest.class,
                 ClassModifierMatcher.classModifierContains(expectedModifier));
    } catch (AssertionError e) {
      String message = e.getMessage();
      errorCollector.checkThat("Expected modifiers contained in message.", message,
                               containsString(
                                   format("\"%s\"", Modifier.toString(expectedModifier))));
      errorCollector.checkThat("Actual modifiers contained in message.", message,
                               containsString(
                                   format("\"%s\"", Modifier.toString(
                                       ModifierClassUnderTest.class.getModifiers()))));
      return;
    }
    fail("Assertion should have failed.");
  }

  private static final class ModifierClassUnderTest {
  }
}
