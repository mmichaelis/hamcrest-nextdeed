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

package com.github.mmichaelis.hamcrest.nextdeed.function;

import static com.github.mmichaelis.hamcrest.nextdeed.function.ApplyingMatcher.applying;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tests {@link ApplyingMatcher}.
 *
 * @author Olaf Kummer
 * @author Mark Michaelis
 * @since 0.1.0
 */
public class ApplyingMatcherTest {

  @Test
  public void facade_contains_matcher() throws Exception {
    String expectedProfession = "plumber";
    Person kurt = new Person(expectedProfession);
    assertThat(kurt, NextDeedMatchers.applying(new GetProfession(), equalTo(expectedProfession)));
  }

  @Test
  public void main_use_case_example_works() throws Exception {
    String expectedProfession = "plumber";
    Person kurt = new Person(expectedProfession);
    assertThat(kurt, applying(new GetProfession(), equalTo(expectedProfession)));
  }

  @Test
  public void state_is_saved_from_match_to_mismatch_message() throws Exception {
    String firstShape = "dog";
    String secondShape = "cat";
    Shapeshifter shapeshifter = new Shapeshifter(firstShape, secondShape);
    AssertionError caughtError = null;

    try {
      assertThat(shapeshifter, applying(new GetShape(), equalTo(secondShape)));
    } catch (AssertionError e) {
      caughtError = e;
    }

    assertThat("Comparison should have failed.", caughtError, notNullValue());
    assertThat("Message should contain shape from match.", caughtError.getMessage(), Matchers
        .containsString(firstShape));
  }

  private static class Shapeshifter {

    private final List<String> shapes;


    private Shapeshifter(String... shapes) {
      this.shapes = new ArrayList<>(Arrays.asList(shapes));
    }

    public String getShape() {
      return shapes.remove(0);
    }
  }

  private static class Person {

    private final String profession;

    private Person(final String profession) {
      this.profession = profession;
    }

    public String getProfession() {
      return profession;
    }
  }

  private static class GetProfession implements Function<Person, String> {

    @Override
    public String apply(final Person input) {
      return input.getProfession();
    }
  }

  private static class GetShape implements Function<Shapeshifter, String> {

    @Override
    public String apply(final Shapeshifter input) {
      return input.getShape();
    }
  }
}
