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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Tests {@link ReferenceImpl}.
 *
 * @since SINCE
 */
public class ReferenceImplTest {

  private static final String VALUE_1 = "Lorem Ipsum";
  private static final String VALUE_2 = "Dolor Sit Amet";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void setAndGetValue() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    assertThat(reference.get(), equalTo(VALUE_1));
  }

  @Test
  public void returnValueOnSet() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    String returnedValue = reference.set(VALUE_2);
    assertThat(reference.get(), equalTo(VALUE_2));
    assertThat(returnedValue, equalTo(VALUE_2));
  }

  @Test
  public void failOnGetWithoutSet() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();

    expectedException.expect(IllegalStateException.class);
    reference.get();
  }

  @Test
  public void failOnGetAfterRemove() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    reference.remove();

    expectedException.expect(IllegalStateException.class);
    reference.get();
  }

  @Test
  public void returnPreviousValueOnRemove() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    String previousValue = reference.remove();
    assertThat(previousValue, equalTo(VALUE_1));
  }

  @Test
  public void signalUnsetInitially() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    assertThat(reference.isSet(), is(false));
  }

  @Test
  public void signalSetAfterValueSet() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    assertThat(reference.isSet(), is(true));
  }

  @Test
  public void signalUnsetAfterRemove() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    reference.remove();
    assertThat(reference.isSet(), is(false));
  }

  @Test
  public void showSetValueInToString() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    assertThat(reference, hasToString(containsString(VALUE_1)));
  }

  @Test
  public void showSetStateInToString() throws Exception {
    Reference<String> reference = new ReferenceImpl<>();
    reference.set(VALUE_1);
    assertThat(reference, hasToString(containsString("true")));
    reference.remove();
    assertThat(reference, hasToString(containsString("false")));
  }

}
