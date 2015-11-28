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

import static com.github.mmichaelis.hamcrest.nextdeed.incubator.SystemPropertyChanger.SYSTEM_PROPERTY_CHANGER;
import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.HAMCREST_NEXT_DEED_CONFIG;
import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.propertyName;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;

import com.google.common.io.Files;

import com.github.mmichaelis.hamcrest.nextdeed.incubator.SystemPropertyChanger;
import com.github.mmichaelis.hamcrest.nextdeed.base.HamcrestNextdeedException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestName;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Tests {@link NextDeedConfiguration}.
 *
 * @since SINCE
 */
public class NextDeedConfigurationTest {

  private static final Charset JAVA_PROPERTIES_CHARSET = Charset.forName("ISO-8859-1");

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public TestName propagatedTestDetails = new PropagatedTestDetails();

  @Rule
  public SystemPropertyChanger systemPropertyChanger = SYSTEM_PROPERTY_CHANGER;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void canRetrieveConfiguration() throws Exception {
    Configuration configuration = HAMCREST_NEXT_DEED_CONFIG.get();
    assertThat("Configuration should have been successfully retrieved.", configuration,
               Matchers.notNullValue());
  }

  @Test
  public void canOverrideViaUserHomeProperties() throws Exception {
    File homeFolder = temporaryFolder.newFolder();
    systemPropertyChanger.setProperty("user.home", homeFolder.getAbsolutePath());
    File userConfigFile = new File(homeFolder, NextDeedConfiguration.USER_FILE_NAME);
    try (PrintWriter writer = new PrintWriter(Files.newWriter(userConfigFile, JAVA_PROPERTIES_CHARSET))) {
      writer.println("probeKey=probeValue");
      writer.println("image.outFilePattern=testPattern.png");
    }
    Configuration configuration = HAMCREST_NEXT_DEED_CONFIG.getNew();
    String probeValue = configuration.getString(propertyName("probeKey"));
    String outFilePattern =
        configuration.getString(propertyName("image.outFilePattern"));
    assertThat("New property is available from user home.", probeValue, equalTo("probeValue"));
    assertThat("Property overrides default by setting from user home.",
               outFilePattern, equalTo("testPattern.png"));
  }

  @Test
  public void failIfPropertyDefinedConfigurationDoesNotExist() throws Exception {
    File homeFolder = temporaryFolder.newFolder();
    File userConfigFile = new File(homeFolder, "nonexistent.properties");
    systemPropertyChanger.setProperty(NextDeedConfiguration.P_PROPERTIES, userConfigFile.getAbsolutePath());

    expectedException.expect(HamcrestNextdeedException.class);
    expectedException.expectCause(Matchers.<Throwable>instanceOf(ConfigurationException.class));
    HAMCREST_NEXT_DEED_CONFIG.getNew();
  }

  @Test
  public void hasToStringMethod() throws Exception {
    assertThat("Resolver has toString with relevant information.", HAMCREST_NEXT_DEED_CONFIG,
               hasToString(Matchers.containsString("hash")));
  }

}
