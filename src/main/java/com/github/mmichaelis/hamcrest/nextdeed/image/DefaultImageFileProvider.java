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

package com.github.mmichaelis.hamcrest.nextdeed.image;

import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.propertyName;
import static com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails.createDefaults;
import static java.util.Collections.singletonMap;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedTestConfiguration;

import org.apache.commons.configuration.Configuration;

import java.io.File;

/**
 * @since SINCE
 */
public class DefaultImageFileProvider implements Function<ImageType, File> {

  private static final String IMAGE_OUT_FILE_PATTERN = "image.outFilePattern";

  @Override
  public File apply(ImageType input) {
    Configuration configuration =
        NextDeedTestConfiguration.HAMCREST_NEXT_DEED_TEST_CONFIG
            .get(singletonMap("imageType", input.toString()),
                 createDefaults());
    return new File(configuration.getString(propertyName(IMAGE_OUT_FILE_PATTERN)));
  }
}