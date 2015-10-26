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

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.PropertyConverter;
import org.jetbrains.annotations.NotNull;
import org.junit.rules.TestName;
import org.junit.runner.Description;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

/**
 * <p>
 * This rule will propagate test specific properties. This allows to use these properties in your
 * <em>Hamcrest &mdash; Next Deed</em> configuration, e. g. to add the test name in file paths.
 * </p>
 * <dl>
 * <dt><strong>Propagated Properties:</strong></dt>
 * <dd>
 * <dl>
 * <dt>{@code testMethodName}</dt>
 * <dd>
 * <p>
 * The test (method) name. Mind that in parameterized tests this might contain special characters.
 * Thus for file names you should use {@code testMethodNameEncoded}.
 * </p>
 * </dd>
 * <dt>{@code testMethodNameEncoded}</dt>
 * <dd>
 * <p>
 * The test (method) name (url encoded).
 * </p>
 * </dd>
 * <dt>{@code testClassName}</dt>
 * <dd>
 * <p>
 * The name of the test class.
 * </p>
 * </dd>
 * <dt>{@code testClassNameEncoded}</dt>
 * <dd>
 * <p>
 * The name of the test class url encoded.
 * </p>
 * </dd>
 * <dt>{@code testFullName}</dt>
 * <dd>
 * <p>
 * Full name of the test, i. e. test class and method name. If used within file names you should
 * use {@code testFullNameEncoded} instead.
 * </p>
 * </dd>
 * <dt>{@code testFullNameEncoded}</dt>
 * <dd>
 * <p>
 * Full name of the test, i. e. test class and method name (url encoded).
 * </p>
 * </dd>
 * <dt>{@code testTimestamp}</dt>
 * <dd>
 * Time stamp denoting the start time of the test.
 * </dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @since SINCE
 */
public class PropagatedTestDetails extends TestName {

  /**
   * Property for the test method name. Unencoded.
   *
   * @see #TEST_METHOD_NAME_ENCODED
   * @since SINCE
   */
  public static final String TEST_METHOD_NAME = "testMethodName";
  /**
   * Property for the test method name. URL encoded.
   *
   * @see #TEST_METHOD_NAME
   * @since SINCE
   */
  public static final String TEST_METHOD_NAME_ENCODED = "testMethodNameEncoded";
  /**
   * Property for the test class name. Unencoded.
   *
   * @see #TEST_CLASS_NAME_ENCODED
   * @since SINCE
   */
  public static final String TEST_CLASS_NAME = "testClassName";
  /**
   * Property for the test class name. URL encoded.
   *
   * @see #TEST_CLASS_NAME
   * @since SINCE
   */
  public static final String TEST_CLASS_NAME_ENCODED = "testClassNameEncoded";
  /**
   * Property for the full test name (class and method). Unencoded.
   *
   * @see #TEST_FULL_NAME_ENCODED
   * @since SINCE
   */
  public static final String TEST_FULL_NAME = "testFullName";
  /**
   * Property for the full test name (class and method). URL encoded.
   *
   * @see #TEST_FULL_NAME
   * @since SINCE
   */
  public static final String TEST_FULL_NAME_ENCODED = "testFullNameEncoded";
  /**
   * Property for the timestamp of the test start time.
   *
   * @since SINCE
   */
  public static final String TEST_TIMESTAMP = "testTimestamp";
  private static final RandomTokenGenerator TOKEN_GENERATOR = new RandomTokenGenerator();
  private static final String TIMESTAMP_PATTERN = "yyyy-MM-dd'T'HH-mm-ss-S";
  private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT_THREAD_LOCAL =
      new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
          return new SimpleDateFormat(TIMESTAMP_PATTERN, Locale.ROOT);
        }
      };
  private static final String UNKNOWN_METHOD_NAME = "unknownMethodName";
  private static final String UNKNOWN_CLASS_NAME = "unknownClassName";
  private static final String UNKNOWN_FULL_NAME = "unknownFullName";
  private String className;
  private String fullName;
  private String timestamp;

  /**
   * Some default values for the properties set by this rule. This might be used, if you rely on
   * the properties but you are unsure if the rule got added to the test. This way you have at
   * least
   * unique values which you might use for example in file names.
   *
   * @return defaults
   * @since SINCE
   */
  @NotNull
  public static Map<String, String> createDefaults() {
    String randomToken = TOKEN_GENERATOR.getRandomToken();
    return ImmutableMap
        .<String, String>builder()
        .put(TEST_METHOD_NAME, UNKNOWN_METHOD_NAME + randomToken)
        .put(TEST_CLASS_NAME, UNKNOWN_CLASS_NAME + randomToken)
        .put(TEST_FULL_NAME, UNKNOWN_FULL_NAME + randomToken)
        .put(TEST_METHOD_NAME_ENCODED, UNKNOWN_METHOD_NAME + randomToken)
        .put(TEST_CLASS_NAME_ENCODED, UNKNOWN_CLASS_NAME + randomToken)
        .put(TEST_FULL_NAME_ENCODED, UNKNOWN_FULL_NAME + randomToken)
        .put(TEST_TIMESTAMP, DATE_FORMAT_THREAD_LOCAL.get().format(new Date()))
        .build();
  }

  /**
   * Get name of current test class.
   *
   * @return test class; might be {@code null} if not added as rule
   * @since SINCE
   */
  public String getClassName() {
    return className;
  }

  /**
   * Get full name of current test (class and method).
   *
   * @return full name; might be {@code null} if not added as rule
   * @since SINCE
   */
  public String getTestFullName() {
    return fullName;
  }

  /**
   * Get timestamp (starting time) of the current test.
   *
   * @return timestamp; might be {@code null} if not added as rule
   * @since SINCE
   */
  public String getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("className", className)
        .add("fullName", fullName)
        .add("timestamp", timestamp)
        .add("super", super.toString())
        .toString();
  }

  @Override
  protected void starting(Description d) {
    super.starting(d);

    className = d.getClassName();
    fullName = String.format("%s.%s", className, getMethodName());
    timestamp = DATE_FORMAT_THREAD_LOCAL.get().format(new Date());

    setProperty(TEST_METHOD_NAME, getMethodName());
    setProperty(TEST_CLASS_NAME, className);
    setProperty(TEST_FULL_NAME, fullName);
    setProperty(TEST_TIMESTAMP, timestamp);

    setProperty(TEST_METHOD_NAME_ENCODED, encode(getMethodName()));
    setProperty(TEST_CLASS_NAME_ENCODED, encode(className));
    setProperty(TEST_FULL_NAME_ENCODED, encode(fullName));
  }

  @Override
  protected void finished(Description description) {
    NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.getWritable().clear();
  }

  private void setProperty(String key, String value) {
    AbstractConfiguration testConfiguration =
        (AbstractConfiguration) NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG.getWritable();
    testConfiguration.setProperty(key, escapeValue(testConfiguration, value));
  }

  private String escapeValue(AbstractConfiguration testConfiguration, String string) {
    return PropertyConverter.escapeDelimiters(string, testConfiguration
        .getListDelimiter());
  }

  /**
   * Encode the given string preferably using UTF-8 but with fallback to system default encoding.
   *
   * @param string string to encode
   * @return encoded string
   */
  @NotNull
  private String encode(@NotNull String string) {
    return new Encoder().encode(string);
  }
}
