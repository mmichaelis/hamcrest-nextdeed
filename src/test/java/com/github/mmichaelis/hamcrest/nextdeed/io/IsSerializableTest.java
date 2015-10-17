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

package com.github.mmichaelis.hamcrest.nextdeed.io;

import static com.github.mmichaelis.hamcrest.nextdeed.base.BaseMessages.withRawMessages;
import static com.github.mmichaelis.hamcrest.nextdeed.io.IsSerializable.isSerializable;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Tests {@link IsSerializable}.
 *
 * @since SINCE
 */
public class IsSerializableTest {

  @Test
  public void nullIsNotSerializable() throws Exception {
    assertThat(null, not(isSerializable()));
  }

  @Test
  public void objectIsNotSerializable() throws Exception {
    assertThat(new Object(), not(isSerializable()));
  }

  @Test
  public void exceptionsAreSerializable() throws Exception {
    assertThat(new RuntimeException(), isSerializable());
  }

  @Test
  public void doesNotSerializeWithUnserializableField() throws Exception {
    assertThat(new NotSerializableField("Hurz"), not(isSerializable()));
  }

  @Test
  public void hasMessageForSerializationFailureWithUnserializableField() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                IoMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(new NotSerializableField("Hurz"), isSerializable());
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.", errorMessage,
               stringContainsInOrder(
                   "isSerializable",
                   "serializationDeserializationFailure",
                   "NotSerializableException"
               )
    );
  }

  @Test
  public void matcherFailsIfNotEqualAfterDeserialization() throws Exception {
    NotEqual actual = new NotEqual();
    assertThat(actual, not(isSerializable(NotEqual.class)
                               .and()
                               .deserializedResultMatches(equalTo(actual))
               )
    );
  }

  @Test
  public void hasMessageWhenMatcherFailsOnDeserializedResult() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                IoMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    NotEqual actual = new NotEqual();
                    assertThat(actual,
                               isSerializable(NotEqual.class)
                                   .and()
                                   .deserializedResultMatches(equalTo(actual))
                    );
                    return null;
                  }
                }
            ),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.", errorMessage,
               stringContainsInOrder(
                   "isSerializable",
                   "Expected:",
                   "but: was"
               )
    );
  }

  @Test
  public void hasToString() throws Exception {
    NotEqual actual = new NotEqual();
    assertThat(isSerializable(NotEqual.class)
                   .and()
                   .deserializedResultMatches(equalTo(actual)),
               Matchers.hasToString(containsString("deserializedMatcher")));
  }

  private static final class NoNoArgConstructor {

    @SuppressWarnings("UnusedParameters")
    public NoNoArgConstructor(String dumb) {
    }
  }

  @SuppressWarnings("unused")
  private static final class NotSerializableField implements Serializable {

    private static final long serialVersionUID = -6219237944254785644L;
    private final NoNoArgConstructor notSerializableField = new NoNoArgConstructor("Dumb");
    private final String setMe;

    private NotSerializableField(String setMe) {
      this.setMe = setMe;
    }
  }

  private static final class NotEqual implements Serializable {

    private static final long serialVersionUID = 5526573442772051785L;
  }
}
