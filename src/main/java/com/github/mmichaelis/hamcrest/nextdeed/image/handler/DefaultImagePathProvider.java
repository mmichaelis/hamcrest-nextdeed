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

package com.github.mmichaelis.hamcrest.nextdeed.image.handler;

import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.propertyName;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.createDefaults;
import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedTestConfiguration;
import com.github.mmichaelis.hamcrest.nextdeed.image.ImageType;

import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @since SINCE
 */
public class DefaultImagePathProvider implements Function<ImageType, Path> {

  public static final String IMAGE_OUT_FILE_PATTERN = "image.outFilePattern";
  private static final Logger LOG = getLogger(DefaultImagePathProvider.class);

  @Override
  public Path apply(ImageType input) {
    Configuration configuration =
        NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG
            .get(singletonMap("imageType", input.toString()),
                 createDefaults());
    String path = configuration.getString(propertyName(IMAGE_OUT_FILE_PATTERN));
    LOG.debug("Raw property value: {} -> {}",
              configuration.getProperty(propertyName(IMAGE_OUT_FILE_PATTERN)),
              path);
    try {
      URI uri = URI.create(path);
      return Paths.get(uri);
    } catch (IllegalArgumentException e) {
      LOG.debug("Cannot build URI from {}. Retrying to get path directly.", path, e);
      return Paths.get(path);
    }
  }
}
