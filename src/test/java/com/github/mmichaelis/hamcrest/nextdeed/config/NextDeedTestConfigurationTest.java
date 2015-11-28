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

import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.HAMCREST_NEXT_DEED_CONFIG;
import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.TestName;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * Tests {@link NextDeedTestConfiguration}.
 *
 * @since SINCE
 */
public class NextDeedTestConfigurationTest {

  private static final Logger LOG = getLogger(NextDeedTestConfigurationTest.class);
  private static String probeKey;
  private static Configuration globalConfig;
  @Rule
  public PropagatedTestDetails testDetails = new PropagatedTestDetails();
  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();
  @Rule
  public TestName testName = new TestName();
  private Configuration writableConfig;
  private Configuration testSpecificConfig;

  @BeforeClass
  public static void setUpClass() throws Exception {
    globalConfig = HAMCREST_NEXT_DEED_CONFIG.get();
    probeKey = globalConfig.getKeys().next();
    LOG.debug("Probing with key '{}' (value: {}).", probeKey, globalConfig.getString(probeKey));
  }

  @Before
  public void setUp() throws Exception {
    writableConfig = HAMCREST_NEXT_DEED_TEST_CONFIG.getWritable();
    testSpecificConfig = HAMCREST_NEXT_DEED_TEST_CONFIG.get();
  }

  @After
  public void tearDown() throws Exception {
    HAMCREST_NEXT_DEED_TEST_CONFIG.getWritable().clear();
  }

  @Test
  public void globalConfigShouldBeContainedInTestSpecificConfig() throws Exception {
    List<String> testSpecificConfigKeys = Lists.newArrayList(testSpecificConfig.getKeys());
    Iterator<String> keys = globalConfig.getKeys();
    while (keys.hasNext()) {
      String globalKey = keys.next();
      errorCollector.checkThat(globalKey, is(in(testSpecificConfigKeys)));
    }
  }

  /**
   * This only refers to usage by {@link PropagatedTestDetails} which uses an additional layer
   * of configuration to set properties which then might be used e. g. when parsing the test
   * configuration file.
   */
  @Test
  public void canOverrideGlobalConfig() throws Exception {
    String previousValue = testSpecificConfig.getString(probeKey);
    String expectedValue = testName.getMethodName();
    writableConfig.setProperty(probeKey, expectedValue);
    String value = testSpecificConfig.getString(probeKey);
    assertThat("Value should have been overridden.",
               value,
               allOf(
                   not(equalTo(previousValue)),
                   equalTo(expectedValue)
               ));
  }

  /**
   * This is meant for <em>Hamcrest &mdash; Next Deed<em> classes which want to introduce
   * additional
   * properties on the fly for parsing configuration.
   */
  @Test
  public void canGetOverriddenConfig() throws Exception {
    String expectedValue = testName.getMethodName();
    Configuration overridingConfiguration =
        HAMCREST_NEXT_DEED_TEST_CONFIG.get(singletonMap(probeKey, expectedValue));
    assertThat("Value should be taken from overriding configuration.",
               overridingConfiguration.getString(probeKey),
               equalTo(expectedValue));
    writableConfig.setProperty(probeKey, expectedValue);
    assertThat("Overriding configuration should always win.",
               overridingConfiguration.getString(probeKey),
               equalTo(expectedValue));
  }

  @Test
  public void canProvideDefaults() throws Exception {
    String someExistingKey = "existing";
    String someOverriddenKey = "overridden";
    String someOverriddenValue = "overriddenValue";
    String someNonExistingKey = "requiresDefault";
    String someDefaultValue = "defaultVaule";

    String expectedValue = testName.getMethodName();

    writableConfig.setProperty(someExistingKey, expectedValue);
    writableConfig.setProperty(someOverriddenKey, expectedValue);

    Configuration configuration =
        HAMCREST_NEXT_DEED_TEST_CONFIG.get(singletonMap(someOverriddenKey, someOverriddenValue),
                                           ImmutableMap.<String, String>builder()
                                               .put(someOverriddenKey, someDefaultValue)
                                               .put(someNonExistingKey, someDefaultValue)
                                               .put(someExistingKey, someDefaultValue)
                                               .build());

    errorCollector.checkThat(
        "Should be able to fall back to default value.",
        configuration.getString(someNonExistingKey),
        equalTo(someDefaultValue));
    errorCollector.checkThat(
        "Default value should not be taken if key exist.",
        configuration.getString(someExistingKey),
        equalTo(expectedValue));
    errorCollector.checkThat(
        "Overriding value should override anything.",
        configuration.getString(someOverriddenKey),
        equalTo(someOverriddenValue));
  }
}
