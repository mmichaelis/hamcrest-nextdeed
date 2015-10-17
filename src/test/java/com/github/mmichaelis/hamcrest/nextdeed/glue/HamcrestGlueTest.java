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

package com.github.mmichaelis.hamcrest.nextdeed.glue;

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.classModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.declaresNoArgumentsConstructor;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.isInstantiableWithNoArguments;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.memberModifierContains;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.base.Predicate;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.Modifier;

/**
 * Tests {@link HamcrestGlue}.
 *
 * @since SINCE
 */
public class HamcrestGlueTest {

  @Test
  public void canWrapMatcherAsPredicate() throws Exception {
    String expectedString = "some string";
    Predicate<String> predicate = HamcrestGlue.asPredicate(Matchers.equalTo(expectedString));
    assertThat(predicate.apply(expectedString), is(true));
    assertThat(predicate.apply("some other string"), is(false));
  }

  @Test
  public void isUtilityClass() throws Exception {
    assertThat(HamcrestGlue.class,
               allOf(
                   declaresNoArgumentsConstructor(),
                   classModifierContains(Modifier.FINAL),
                   isInstantiableWithNoArguments()
               )
    );

    assertThat("Any constructors must be private.",
               asList(HamcrestGlue.class.getDeclaredConstructors()),
               everyItem(memberModifierContains(Modifier.PRIVATE)));
  }

}
