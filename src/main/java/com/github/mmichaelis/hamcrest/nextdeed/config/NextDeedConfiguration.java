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

import static com.github.mmichaelis.hamcrest.nextdeed.config.ConfigUtil.dumpConfigurationOrder;
import static com.github.mmichaelis.hamcrest.nextdeed.config.ConfigUtil.dumpConfigurationProperties;
import static java.lang.String.format;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.jetbrains.annotations.NotNull;

import java.net.URL;

/**
 * <p>
 * Global configuration fed by property files for <em>Hamcrest &mdash; Next Deed</em>.
 * </p>
 * <dl>
 * <dt><strong>Prefix:</strong></dt>
 * <dd>
 * <p>
 * All properties loaded from property files will be prefixed with {@link #PROPERTIES_PREFIX}. Thus
 * if the file contains a property named {@code reportDir} it must be referenced and overridden
 * as {@code hamcrest.nextdeed.reportDir}.
 * </p>
 * </dd>
 * <dt><strong>Loading Order:</strong></dt>
 * <dd>
 * <p>
 * The properties are loaded in a predefined order which is comparable to Spring/Spring Boot. The
 * default order is (first has highest priority):
 * </p>
 * <ol>
 * <li>{@link SystemConfiguration}</li>
 * <li>{@link EnvironmentConfiguration}</li>
 * <li>{@link #P_PROPERTIES}: Files specified through this property either via system or
 * environment configuration. If set, these files must exist, thus they are required then.
 * </li>
 * <li>{@link #USER_FILE_NAME}: A file for custom configuration, located by searching the user home
 * directory, the current classpath and the system classpath. See
 * {@link ConfigurationUtils#locate(String)} for details. This file is optional.</li>
 * <li>{@link #DEFAULT_FILE_NAME}: Contains the defaults/fallbacks for
 * <em>Hamcrest &mdash; Next Deed</em>. Can be used as an example how to build
 * {@link #USER_FILE_NAME}.</li>
 * </ol>
 * </dd>
 * <dt><strong>Build Environment:</strong></dt>
 * <dd>
 * <dl>
 * <dt><em>Gradle:</em></dt>
 * <dd>
 * <p>
 * Set for example the {@code reportDir} via:
 * </p>
 * <pre>{@code
 * allprojects {
 *   tasks.withType(Test) {
 *     systemProperty 'hamcrest.nextdeed.reportDir', "$testResultsDir"
 *    }
 * }
 * }</pre>
 * </dd>
 * </dl>
 * </dd>
 * </dl>
 *
 * @since SINCE
 */
public enum NextDeedConfiguration {
  HAMCREST_NEXT_DEED_CONFIG;

  /**
   * Prefix all properties loaded from files will receive.
   */
  public static final String PROPERTIES_PREFIX = "hamcrest.nextdeed";

  /**
   * Property to set in system properties or environment to load additional property files.
   * Comma separated values supported to specify multiple files to be loaded. The files are
   * loaded in the given order and are required to exist, if specified.
   */
  public static final String P_PROPERTIES = PROPERTIES_PREFIX + ".properties";

  /**
   * Filename of user specific settings for <em>Hamcrest &mdash; Next Deed</em>.
   */
  static final String USER_FILE_NAME = "hamcrest-nextdeed.properties";
  /**
   * Defaults provided in this JAR artifact.
   */
  private static final String DEFAULT_FILE_NAME =
      "META-INF/mmichaelis/default-hamcrest-nextdeed.properties";
  /**
   * Supplier for the configuration, cached in order to have only one instance.
   */
  private final Supplier<Configuration> CONFIGURATION_SUPPLIER =
      Suppliers.memoize(new ConfigurationSupplier());

  /**
   * Format a shortcut-property-name to its full name if received from a file.
   *
   * @param baseName base property name
   * @return property prefixed by {@link #PROPERTIES_PREFIX}
   */
  public static String propertyName(@NotNull String baseName) {
    return format("%s.%s", PROPERTIES_PREFIX, baseName);
  }

  /**
   * Add an optional configuration file.
   *
   * @param configuration configuration to add the new configuration to
   * @param fileName      file to read the configuration from;
   *                      ignored if it cannot be read or parsed
   */
  private static void addOptionalConfiguration(@NotNull CombinedConfiguration configuration,
                                               String fileName) {
    URL located = ConfigurationUtils.locate(fileName);
    if (located != null) {
      addRequiredConfiguration(configuration, fileName);
    }
  }

  /**
   * Add a required configuration file.
   *
   * @param configuration configuration to add the new configuration to
   * @param fileName      file to read the configuration from
   * @throws ConfigurationException if the provided file does not exist or cannot be parsed
   */
  private static void addRequiredConfiguration(@NotNull CombinedConfiguration configuration,
                                               String fileName) {
    try {
      PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(fileName);
      configuration.addConfiguration(propertiesConfiguration,
                                     propertiesConfiguration.getFile().getAbsolutePath(),
                                     PROPERTIES_PREFIX);
    } catch (org.apache.commons.configuration.ConfigurationException e) {
      throw new ConfigurationException(
          format("Unable to load properties %s.", fileName), e);
    }
  }

  /**
   * Add configurations from a system/environment property where the property value denotes
   * files to load.
   *
   * @param configuration configuration to add the new configurations to
   */
  private static void addConfigurationFromProperty(@NotNull CombinedConfiguration configuration) {
    String[] namesFromProperty = configuration.getStringArray(P_PROPERTIES);
    for (String nameFromProperty : namesFromProperty) {
      addRequiredConfiguration(configuration, nameFromProperty);
    }
  }

  /**
   * Retrieve Hamcrest Configuration.
   *
   * @return Hamcrest Configuration
   */
  @NotNull
  public Configuration get() {
    return CONFIGURATION_SUPPLIER.get();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("CONFIGURATION_SUPPLIER", CONFIGURATION_SUPPLIER)
        .toString();
  }

  /**
   * Receive a re-initialized configuration.
   *
   * @return Hamcrest Configuration
   */
  @VisibleForTesting
  @NotNull
  Configuration getNew() {
    return new ConfigurationSupplier().get();
  }

  /**
   * Supplier for the actual configuration.
   */
  private static class ConfigurationSupplier implements Supplier<Configuration> {

    @Override
    public Configuration get() {
      // Configuration priority adapted to Spring Boot:
      // http://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html
      CombinedConfiguration configuration = new CombinedConfiguration();
      configuration.addConfiguration(new SystemConfiguration(), "system");
      configuration.addConfiguration(new EnvironmentConfiguration(), "environment");
      addConfigurationFromProperty(configuration);
      addOptionalConfiguration(configuration, USER_FILE_NAME);
      addRequiredConfiguration(configuration, DEFAULT_FILE_NAME);
      dumpConfigurationOrder(configuration);
      dumpConfigurationProperties(configuration);
      return configuration;
    }

  }
}
