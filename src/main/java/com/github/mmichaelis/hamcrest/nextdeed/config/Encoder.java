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

import com.google.common.annotations.VisibleForTesting;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Encodes strings preferably using UTF-8 but with fallback to system default encoding.
 * @since SINCE
 */
class Encoder {

  private static final Logger LOG = getLogger(Encoder.class);

  private static final String FILE_ENCODING_PROPERTY = "file.encoding";
  private static final String URL_ENCODING_CHARSET = "UTF-8";
  @SuppressWarnings("AccessOfSystemProperties")
  private static final String SYSTEM_DEFAULT_ENCODING = System.getProperty(FILE_ENCODING_PROPERTY);


  /**
   * Encode the given string preferably using UTF-8 but with fallback to system default encoding.
   *
   * @param string string to encode
   * @return encoded string
   */
  @NotNull
  public String encode(@NotNull String string) {
    String enc = preferredEncoding();
    try {
      return URLEncoder.encode(string, enc);
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Unable to encode '{}' with encoding {}. Will try system encoding next.", string,
               enc, e);
    }
    enc = fallbackEncoding();
    try {
      return URLEncoder.encode(string, enc);
    } catch (UnsupportedEncodingException e) {
      LOG.warn(
          "Unable to encode '{}' with system default encoding {}. Will return string unencoded.",
          string,
          enc, e);
    }
    return string;
  }

  @VisibleForTesting
  String fallbackEncoding() {
    return SYSTEM_DEFAULT_ENCODING;
  }

  @NotNull
  @VisibleForTesting
  String preferredEncoding() {
    return URL_ENCODING_CHARSET;
  }

}
