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

import static com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaDefaultConstructor.isInstantiableViaDefaultConstructor;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/**
 * Tests {@link InstantiableViaDefaultConstructor}.
 *
 * @since 1.0.0
 */
@SuppressWarnings("deprecation")
public class InstantiableViaDefaultConstructorTest {

  @Test
  public void passForDefaultDefaultConstructor() throws Exception {
    assertThat(DefaultDefaultConstructor.class,
               isInstantiableViaDefaultConstructor());
  }

  @Test
  public void passForPrivateDefaultConstructor() throws Exception {
    assertThat(PrivateDefaultConstructor.class,
               isInstantiableViaDefaultConstructor());
  }

  @Test
  public void dontPassForNoDefaultConstructor() throws Exception {
    assertThat(NoDefaultConstructor.class,
               not(isInstantiableViaDefaultConstructor()));
  }

  @Test
  public void dontPassForAbstractClass() throws Exception {
    assertThat(AbstractClass.class,
               not(isInstantiableViaDefaultConstructor()));
  }

  @Test
  public void dontPassForErroneousDefaultConstructor() throws Exception {
    assertThat(ErrorThrowingDefaultConstructor.class,
               not(isInstantiableViaDefaultConstructor()));
  }

  @Test
  public void messageContainsNoSuchMethodException() throws Exception {
    try {
      assertThat(NoDefaultConstructor.class,
                 isInstantiableViaDefaultConstructor());
    } catch (AssertionError e) {
      String message = e.getMessage();
      assertThat(message, Matchers.containsString(NoSuchMethodException.class.getSimpleName()));
      return;
    }
    fail("AssertionError should have been raised.");
  }

  @Test
  public void messageContainsInvocationTargetException() throws Exception {
    try {
      assertThat(ErrorThrowingDefaultConstructor.class,
                 isInstantiableViaDefaultConstructor());
    } catch (AssertionError e) {
      String message = e.getMessage();
      assertThat(message, Matchers.containsString(InvocationTargetException.class.getSimpleName()));
      return;
    }
    fail("AssertionError should have been raised.");
  }

  @Test
  public void messageContainsInstantiationException() throws Exception {
    try {
      assertThat(AbstractClass.class,
                 isInstantiableViaDefaultConstructor());
    } catch (AssertionError e) {
      String message = e.getMessage();
      assertThat(message, Matchers.containsString(InstantiationException.class.getSimpleName()));
      return;
    }
    fail("AssertionError should have been raised.");
  }

  private static final class DefaultDefaultConstructor {

  }

  private abstract static class AbstractClass {

    protected AbstractClass() {
    }
  }

  private static final class PrivateDefaultConstructor {

    private PrivateDefaultConstructor() {
    }
  }

  private static final class ErrorThrowingDefaultConstructor {

    public ErrorThrowingDefaultConstructor() {
      throw new IllegalStateException("Provoked Error for Test.");
    }
  }

  private static final class NoDefaultConstructor {

    public NoDefaultConstructor(String arg) {
    }
  }
}
