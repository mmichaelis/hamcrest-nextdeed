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

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

/**
 * @since SINCE
 */
public class WaitTimeoutExceptionTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  @Rule
  public TestName testName = new TestName();

  @Test
  public void noArgConstructor() throws Exception {
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectCause(Matchers.nullValue(Throwable.class));
    expectedException.expectMessage(Matchers.emptyOrNullString());
    throw new WaitTimeoutException();
  }

  @Test
  public void onlyCauseConstructor() throws Exception {
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectCause(Matchers.<Exception>instanceOf(IllegalStateException.class));
    expectedException.expectMessage(Matchers.containsString(IllegalStateException.class.getName()));
    throw new WaitTimeoutException(new IllegalStateException());
  }

  @Test
  public void onlyMessageConstructor() throws Exception {
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectCause(Matchers.nullValue(Throwable.class));
    expectedException.expectMessage(Matchers.containsString(testName.getMethodName()));
    throw new WaitTimeoutException(testName.getMethodName());
  }

  @Test
  public void messageAndCauseConstructor() throws Exception {
    expectedException.expect(WaitTimeoutException.class);
    expectedException.expectCause(Matchers.<Exception>instanceOf(IllegalStateException.class));
    expectedException.expectMessage(Matchers.containsString(testName.getMethodName()));
    throw new WaitTimeoutException(testName.getMethodName(), new IllegalStateException());
  }
}
