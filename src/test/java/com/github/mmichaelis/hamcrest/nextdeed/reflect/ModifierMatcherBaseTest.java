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

import static com.github.mmichaelis.hamcrest.nextdeed.TestMarker.TEST;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.mmichaelis.hamcrest.nextdeed.LogTestName;

import org.hamcrest.Matchers;
import org.hamcrest.StringDescription;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.slf4j.Logger;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;

/**
 * @since 1.0.0
 */
@RunWith(Parameterized.class)
public class ModifierMatcherBaseTest {

  private static final Logger LOG = getLogger(ModifierMatcherBaseTest.class);

  private final String description;
  private final int expectedModifiers;
  private final int actualModifiers;
  private final int differenceModifiers;
  private final boolean strict;
  private final boolean expectedMatch;

  @Rule
  public TestRule logTestName = new LogTestName();

  private ModifierMatcherBase<Object> modifierMatcherBase;

  public ModifierMatcherBaseTest(String description,
                                 int expectedModifiers,
                                 int actualModifiers,
                                 int differenceModifiers,
                                 boolean strict,
                                 boolean expectedMatch) {
    this.description = description;
    this.expectedModifiers = expectedModifiers;
    this.actualModifiers = actualModifiers;
    this.differenceModifiers = differenceModifiers;
    this.strict = strict;
    this.expectedMatch = expectedMatch;
  }

  @Parameters(name = "{0} ({index}): expected: {1}, actual: {2}, diff: {3}, strict: {4}, matches: {5}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"Zero matches anything for 'contains'", 0, 0, 0, false, true},
        {"Zero matches anything for 'contains'", 0, Modifier.classModifiers(),
         Modifier.classModifiers(), false, true},
        {"No actual modifiers don't match 'contains'", Modifier.classModifiers(), 0,
         Modifier.classModifiers(), false, false},
        {"Equal is also 'contains'", Modifier.FINAL, Modifier.FINAL, 0, false, true},
        {"Equal is also 'contains'", Modifier.FINAL | Modifier.PUBLIC,
         Modifier.FINAL | Modifier.PUBLIC, 0, false, true},
        {"One match fulfills 'contains'", Modifier.FINAL, Modifier.FINAL | Modifier.PUBLIC,
         Modifier.PUBLIC, false, true},
        {"Two match fulfills 'contains'", Modifier.FINAL | Modifier.PUBLIC,
         Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC, Modifier.STATIC, false, true},
        {"One missing fails 'contains'", Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC,
         Modifier.FINAL | Modifier.PUBLIC, Modifier.STATIC, false, false},

        {"Zero matches anything for 'equals'", 0, 0, 0, true, true},
        {"Zero doesn't match for any other than zero for 'equals'", 0, Modifier.classModifiers(),
         Modifier.classModifiers(), true, false},
        {"No actual modifiers don't match 'equals'", Modifier.classModifiers(), 0,
         Modifier.classModifiers(), true, false},
        {"Equal is also 'equals'", Modifier.FINAL, Modifier.FINAL, 0, true, true},
        {"Equal is also 'equals'", Modifier.FINAL | Modifier.PUBLIC,
         Modifier.FINAL | Modifier.PUBLIC, 0, true, true},
        {"Only one of two required doesn't match 'equals'", Modifier.FINAL,
         Modifier.FINAL | Modifier.PUBLIC, Modifier.PUBLIC, true, false},
        {"Only two of three required doesn't match 'equals'", Modifier.FINAL | Modifier.PUBLIC,
         Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC, Modifier.STATIC, true, false},
        {"One missing fails 'equals'", Modifier.FINAL | Modifier.PUBLIC | Modifier.STATIC,
         Modifier.FINAL | Modifier.PUBLIC, Modifier.STATIC, true, false},
    });
  }

  @Before
  public void setUp() throws Exception {
    modifierMatcherBase = new ModifierMatcherBase<Object>(expectedModifiers, "class", strict) {
      @Override
      protected int getModifiers(Object item) {
        return actualModifiers;
      }
    };
  }

  @Test
  public void containsRelevantInformationInToString() throws Exception {
    assertThat(modifierMatcherBase,
               hasToString(
                   stringContainsInOrder(
                       "expectedModifier",
                       String.valueOf(expectedModifiers)
                   )
               )
    );
  }

  @Test
  public void matchesAsExpected() throws Exception {
    assertThat(description,
               modifierMatcherBase.matches("anything"),
               Matchers.is(expectedMatch)
    );
  }

  @Test
  public void mismatchDescriptionContainsActualModifires() throws Exception {
    StringDescription description = new StringDescription();
    modifierMatcherBase.describeMismatch("anything", description);
    String str = description.toString();
    LOG.debug(TEST, "Mismatch Description: {}", str);
    assertThat("Mismatch contains actual modifiers.", str,
               Matchers
                   .containsString(String.format("\"%s\"", Modifier.toString(actualModifiers))));
  }

  @Test
  public void mismatchDescriptionContainsModifiersDifference() throws Exception {
    StringDescription description = new StringDescription();
    modifierMatcherBase.describeMismatch("anything", description);
    String str = description.toString();
    LOG.debug(TEST, "Mismatch Description: {}", str);
    assertThat("Mismatch contains actual modifiers.", str,
               Matchers.containsString(
                   String.format("\"%s\"", Modifier.toString(differenceModifiers))));
  }

  @Test
  public void descriptionContainsExpectedModifiers() throws Exception {
    StringDescription description = new StringDescription();
    modifierMatcherBase.describeTo(description);
    String str = description.toString();
    LOG.debug(TEST, "Description: {}", str);
    assertThat("Description contains verbose expected modifiers.", str, Matchers
        .containsString(String.format("\"%s\"", Modifier.toString(expectedModifiers))));
  }
}
