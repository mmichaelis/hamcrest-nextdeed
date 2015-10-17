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

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * <p>
 * Configuration which provides access to test specific configuration for internal use. This
 * configuration allows <em>Hamcrest &mdash; Next Deed</em> to use test-specific configuration
 * parameters provided by {@link PropagatedTestDetails}. So if you want to use test specific
 * configuration for example for paths, etc. you should ensure to add {@link PropagatedTestDetails}
 * as test rule.
 * </p>
 * <p>
 * This class itself is only meant for internal use &mdash; but should be used by any
 * <em>Hamcrest &mdash; Next Deed</em> class which relies on the configuration.
 * </p>
 *
 * @since SINCE
 */
public enum NextDeedTestConfiguration {
  HAMCREST_NEXT_DEED_TEST_CONFIG;

  /**
   * The local test configuration whose properties will override any other configuration.
   * It is meant for setting new properties.
   */
  private static final ThreadLocal<Configuration> LOCAL_TEST_CONFIGURATION_THREAD_LOCAL =
      new ThreadLocal<Configuration>() {
        @Override
        protected Configuration initialValue() {
          return new BaseConfiguration();
        }
      };

  /**
   * Contains the test specific <em>Hamcrest &mdash; Next Deed</em> configuration hierarchy
   * including the test specific properties. This is the configuration which should be used
   * for reading.
   */
  private static final ThreadLocal<Configuration> TEST_CONFIGURATION_THREAD_LOCAL =
      new ThreadLocal<Configuration>() {
        @Override
        protected Configuration initialValue() {
          CombinedConfiguration configuration = new CombinedConfiguration();
          configuration
              .addConfiguration((AbstractConfiguration) HAMCREST_NEXT_DEED_TEST_CONFIG.getWritable(), "test");
          configuration.addConfiguration(
              (AbstractConfiguration) HAMCREST_NEXT_DEED_CONFIG.get(),
              "global");
          return configuration;
        }
      };

  /**
   * <p>
   * Get the global configuration which should be used for reading properties (and actually is
   * read-only).
   * </p>
   *
   * @return configuration
   * @see #get(Map)
   * @see #get(Map, Map)
   */
  @NotNull
  public Configuration get() {
    return TEST_CONFIGURATION_THREAD_LOCAL.get();
  }

  /**
   * Get test configuration with overrides. Meant to be used internally by
   * <em>Hamcrest &mdash; Next Deed</em> if a class wants to add its very own specific
   * configuration to the system properties.
   *
   * @param overrides properties to override/set
   * @param <T>       type of the property value
   * @return configuration
   * @see #get()
   * @see #get(Map, Map)
   */
  @NotNull
  public <T> Configuration get(@NotNull Map<String, T> overrides) {
    return get(overrides, null);
  }

  /**
   * Get test configuration with overrides and defaults. Meant to be used internally by
   * <em>Hamcrest &mdash; Next Deed</em> if a class wants to add its very own specific
   * configuration to the system properties.
   *
   * @param overrides properties to override/set
   * @param defaults  default property values
   * @param <T>       type of the property value
   * @return configuration
   * @see #get()
   * @see #get(Map)
   */
  @NotNull
  public <T> Configuration get(@NotNull Map<String, T> overrides,
                               @Nullable Map<String, T> defaults) {
    CombinedConfiguration configuration = new CombinedConfiguration();
    configuration.addConfiguration(new MapConfiguration(overrides), "override");
    configuration.addConfiguration((AbstractConfiguration) get(), "default");
    if (defaults != null) {
      configuration.addConfiguration(new MapConfiguration(defaults));
    }
    return configuration;
  }

  /**
   * Local test configuration meant to be modified by {@link PropagatedTestDetails} for properties
   * to contain test specific information.
   *
   * @return configuration
   */
  @NotNull
  Configuration getWritable() {
    return LOCAL_TEST_CONFIGURATION_THREAD_LOCAL.get();
  }

}
