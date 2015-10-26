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

package com.github.mmichaelis.hamcrest.nextdeed.config;

import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_CLASS_NAME;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_CLASS_NAME_ENCODED;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_FULL_NAME;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_FULL_NAME_ENCODED;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_METHOD_NAME;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_METHOD_NAME_ENCODED;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.TEST_TIMESTAMP;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeThat;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tests {@link PropagatedTestDetails}.
 *
 * @since SINCE
 */
@RunWith(Parameterized.class)
public class PropagatedTestDetailsTest {

  private static final Matcher<String> IS_URL_ENCODED = new IsUrlEncoded();
  private final String key;
  private final String defaultValue;
  @Rule
  public PropagatedTestDetails testDetails = new PropagatedTestDetails();

  public PropagatedTestDetailsTest(String key, String defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Parameters(name = "{index}: {0}, default: {1}")
  public static Collection<Object[]> data() {
    Map<String, String> defaults = PropagatedTestDetails.createDefaults();
    Collection<Object[]> testData = new ArrayList<>(defaults.size());
    for (Entry<String, String> entry : defaults.entrySet()) {
      testData.add(new Object[]{entry.getKey(), entry.getValue()});
    }
    return testData;
  }

  @Test
  public void defaultValueIsAvailable() throws Exception {
    assertThat("Default value should be available.", defaultValue, not(emptyOrNullString()));
  }

  @Test
  public void defaultsHandleCornerCaseMinLong() throws Exception {


  }

  @Test
  public void defaultKeyIsAvailable() throws Exception {
    assertThat("Default key should be available.", key, not(emptyOrNullString()));
  }

  @Test
  public void valueIsSetByRule() throws Exception {
    assertThat("Value should be set in configuration.", NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(key),
               not(emptyOrNullString()));
  }

  @Test
  public void valueContainedInToString() throws Exception {
    assumeThat("Skipped for encoded property value: Only unencoded values expected to be part of toString().",
               key,
               not(endsWith("Encoded"))
    );
    assertThat("Value should be contained in toString().", testDetails,
               hasToString(
                   containsString(NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(key))
               )
    );
  }

  @Test
  public void keySpecificRequirementsFulfilled() throws Exception {
    String testMethodName = "keySpecificRequirementsFulfilled";
    String propertyValue;
    switch (key) {
      case TEST_METHOD_NAME:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_METHOD_NAME);
        assertThat("Test method name should be set in properties as expected.",
                   propertyValue,
                   containsString(testMethodName));
        assertThat("Property value should be equal to direct value returned..",
                   propertyValue,
                   equalTo(testDetails.getMethodName()));
        break;
      case TEST_METHOD_NAME_ENCODED:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_METHOD_NAME_ENCODED);
        assertThat("Encoded test method name should be set in properties as expected.",
                   propertyValue,
                   containsString(testMethodName));
        assertThat("Test method name (encoded) should not contain forbidden characters.",
                   propertyValue, isUrlEncoded());
        break;
      case TEST_CLASS_NAME:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_CLASS_NAME);
        assertThat("Test class name should be set in properties as expected.",
                   propertyValue,
                   containsString(getClass().getName()));
        assertThat("Property value should be equal to direct value returned.",
                   propertyValue,
                   equalTo(testDetails.getClassName()));
        break;
      case TEST_CLASS_NAME_ENCODED:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_CLASS_NAME_ENCODED);
        assertThat("Encoded test class name should be set in properties as expected.",
                   propertyValue,
                   containsString(getClass().getSimpleName()));
        assertThat("Test class name (encoded) should not contain forbidden characters.",
                   propertyValue, isUrlEncoded());
        break;
      case TEST_FULL_NAME:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_FULL_NAME);
        assertThat("Test full name should be set in properties as expected.",
                   propertyValue,
                   allOf(containsString(testMethodName),
                         containsString(getClass().getName())
                   )
        );
        assertThat("Property value should be equal to direct value returned.",
                   propertyValue,
                   equalTo(testDetails.getTestFullName()));
        break;
      case TEST_FULL_NAME_ENCODED:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_FULL_NAME_ENCODED);
        assertThat("Test full name (encoded) should be set in properties as expected.",
                   propertyValue,
                   allOf(containsString(testMethodName),
                         containsString(getClass().getSimpleName())
                   )
        );
        assertThat("Test full name (encoded) should not contain forbidden characters.",
                   propertyValue, isUrlEncoded());
        break;
      case TEST_TIMESTAMP:
        propertyValue = NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.get().getString(TEST_TIMESTAMP);
        assertThat("Property value should be equal to direct value returned.",
                   propertyValue,
                   equalTo(testDetails.getTimestamp()));
        break;
      default:
        fail("Unknown/Untested key. Please extend test or fix key. Key: " + key);
    }
  }

  @NotNull
  private static Matcher<String> isUrlEncoded() {
    return IS_URL_ENCODED;
  }

  /**
   * Simple check if a String is URL encoded.
   */
  private static final class IsUrlEncoded extends CustomTypeSafeMatcher<String> {

    public IsUrlEncoded() {
      super("String must be URL encoded.");
    }

    @Override
    protected boolean matchesSafely(String item) {
      try {
        URL file = new URL("file", "", item);
        file.toURI();
      } catch (MalformedURLException ignored) {
        return false;
      } catch (URISyntaxException e) {
        return false;
      }
      return true;
    }
  }
}
