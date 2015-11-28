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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.collect.Lists;

import org.apache.commons.configuration.CombinedConfiguration;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.List;

/**
 * @since SINCE
 */
public final class ConfigUtil {

  private static final Logger LOG = getLogger(ConfigUtil.class);

  private ConfigUtil() {
    // Utility class
  }

  public static void dumpConfigurationProperties(Configuration configuration) {
    dumpConfigurationProperties(configuration, "");
  }

  public static void dumpConfigurationProperties(Configuration configuration, String indent) {
    if (LOG.isDebugEnabled()) {
      try {
        LOG.debug("{}Available properties:", indent);
        List<String> keys = Lists.newArrayList(configuration.getKeys());
        Collections.sort(keys);
        for (String key : keys) {
          Object rawProperty = configuration.getProperty(key);
          String propertyString = configuration.getString(key);
          if (propertyString.equals(rawProperty)) {
            LOG.debug("{}    {} = {}", indent, key, rawProperty);
          } else {
            LOG.debug("{}    {} = {} (raw: {})", indent, key, propertyString, rawProperty);
          }
        }
      } catch (RuntimeException e) {
        LOG.warn("Unable to dump configured properties.", e);
      }
    }
  }

  public static void dumpConfigurationOrder(CombinedConfiguration configuration) {
    if (LOG.isDebugEnabled()) {
      List<String> names = configuration.getConfigurationNameList();
      int configurationCount = configuration.getNumberOfConfigurations();
      LOG.debug("Loaded {} configuration sources (high priority first):", configurationCount);
      for (int i = 0; i < configurationCount; i++) {
        Configuration currentConfiguration = configuration.getConfiguration(i);
        LOG.debug("    {}. Configuration: {} ({})", i + 1, names.get(i),
                 currentConfiguration);
      }
    }
  }

}
