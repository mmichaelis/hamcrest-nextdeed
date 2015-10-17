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

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.applying;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.isJavaCompliantException;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.isSerializable;
import static com.github.mmichaelis.hamcrest.nextdeed.exception.JavaComplianceLevel.JAVA_1_7;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests {@link WaitTimeoutException}.
 *
 * @since 1.0.0
 */
public class WaitTimeoutExceptionTest {

  private static final Function<Throwable, String>
      GET_MESSAGE_FUNCTION =
      new Function<Throwable, String>() {
        @Override
        public String apply(Throwable input) {
          return input.getMessage();
        }
      };

  private static final Function<Throwable, Throwable>
      GET_CAUSE_FUNCTION =
      new Function<Throwable, Throwable>() {
        @Override
        public Throwable apply(Throwable input) {
          return input.getCause();
        }
      };

  @Test
  public void fulfillsJavaComplianceLevel() throws Exception {
    assertThat(WaitTimeoutException.class,
               isJavaCompliantException(JAVA_1_7));
  }

  @Test
  public void defaultExceptionIsSerializable() throws Exception {
    assertThat(new WaitTimeoutException(), isSerializable());
  }

  @Test
  public void withMessageExceptionIsSerializable() throws Exception {
    String message = "some message";
    assertThat(new WaitTimeoutException(message),
               isSerializable(Throwable.class)
                   .and()
                   .deserializedResultMatches(
                       applying(GET_MESSAGE_FUNCTION, equalTo(message))
                   )
    );
  }

  @Test
  public void withCauseExceptionIsSerializable() throws Exception {
    Throwable someCause = new RuntimeException();
    assertThat(new WaitTimeoutException(someCause),
               isSerializable(Throwable.class)
                   .and()
                   .deserializedResultMatches(
                       applying(GET_CAUSE_FUNCTION, Matchers.notNullValue())
                   )
    );
  }

  @Test
  public void withMessageCauseExceptionIsSerializable() throws Exception {
    String message = "some message";
    Throwable someCause = new RuntimeException();
    assertThat(new WaitTimeoutException(message, someCause),
               isSerializable(Throwable.class)
                   .and()
                   .deserializedResultMatches(
                       allOf(
                           applying(GET_MESSAGE_FUNCTION, equalTo(message)),
                           applying(GET_CAUSE_FUNCTION, Matchers.notNullValue())
                       )
                   )
    );
  }

}
