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

import static com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresConstructor.declaresConstructor;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.hamcrest.Matchers;
import org.junit.Test;

/**
 * Tests {@link ClassDeclaresConstructor}.
 *
 * @since SINCE
 */
public class ClassDeclaresConstructorTest {

  @Test
  public void detectDefaultConstructor() throws Exception {
    assertThat(ClassUnderTest.class, declaresConstructor());
  }

  @Test
  public void detectProtectedConstructor() throws Exception {
    assertThat(ClassUnderTest.class, declaresConstructor(String.class));
  }

  @Test
  public void detectPrivateConstructor() throws Exception {
    assertThat(ClassUnderTest.class, declaresConstructor(String.class, Integer.TYPE));
  }

  @Test
  public void provideMeaningFulErrorMessage() throws Exception {
    try {
      assertThat(ClassUnderTest.class, declaresConstructor(String.class, Long.TYPE));
    } catch (AssertionError e) {
      String message = e.getMessage();
      assertThat(message, allOf(
          containsString(ClassUnderTest.class.getName()),
          containsString(ClassUnderTest.class.getDeclaredConstructors()[0].getName()),
          containsString(ClassUnderTest.class.getDeclaredConstructors()[1].getName()),
          containsString(ClassUnderTest.class.getDeclaredConstructors()[2].getName())
      ));
      return;
    }
    fail("Failure expected.");
  }

  @Test
  public void hasToString() throws Exception {
    assertThat(declaresConstructor(String.class, Long.TYPE), Matchers.hasToString(allOf(
        containsString(ClassDeclaresConstructor.class.getSimpleName()),
        containsString(String.class.getName()),
        containsString(Long.TYPE.getName())
    )));
  }

  private static final class ClassUnderTest {

    private ClassUnderTest(String arg1, int arg2) {
    }

    protected ClassUnderTest(String arg) {
      this(arg, 42);
    }

    public ClassUnderTest() {
      this("Test");
    }

  }

}
