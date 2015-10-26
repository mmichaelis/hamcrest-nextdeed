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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests {@link Encoder}.
 *
 * @since SINCE
 */
public class EncoderTest {

  private Encoder defaultEncoder;

  @Before
  public void setUp() throws Exception {
    defaultEncoder = new Encoder();
  }

  @Test
  public void plainStringIsNotEncoded() throws Exception {
    String value = "simple";
    assertThat(defaultEncoder.encode(value), equalTo(value));
  }

  @Test
  public void stringIsEncoded() throws Exception {
    String value = "sim&ple";
    assertThat(defaultEncoder.encode(value), not(equalTo(value)));
  }

  @Test
  public void fallbackToSystemEncoding() throws Exception {
    String value = "sim&ple";
    Encoder encoder = new Encoder() {
      @NotNull
      @Override
      String preferredEncoding() {
        return "not-really-an-encoding";
      }
    };
    assertThat(encoder.encode(value), not(equalTo(value)));
  }

  @Test
  public void fallbackToUnencodedIfNoValidEncodingAvailable() throws Exception {
    String value = "sim&ple";
    Encoder encoder = new Encoder() {
      @Override
      String fallbackEncoding() {
        return "no-fallback-encoding";
      }

      @NotNull
      @Override
      String preferredEncoding() {
        return "not-really-an-encoding";
      }
    };
    assertThat(encoder.encode(value), equalTo(value));
  }
}
