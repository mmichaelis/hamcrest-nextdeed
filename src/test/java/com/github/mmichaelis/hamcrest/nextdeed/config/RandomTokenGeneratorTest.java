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

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

/**
 * Tests {@link RandomTokenGenerator}.
 *
 * @since SINCE
 */
public class RandomTokenGeneratorTest {

  private static final int PROBE_SIZE = 10;
  private static final int GRACE_UNIQUE_PROBE_SIZE = 8;

  @Test
  public void randomTokenIsGenerated() throws Exception {
    RandomTokenGenerator generator = new RandomTokenGenerator();
    Collection<String> tokens = new HashSet<>(PROBE_SIZE);
    for (int i = 0; i < PROBE_SIZE; i++) {
      tokens.add(generator.getRandomToken());
    }
    assertThat("Ideally " + PROBE_SIZE + " unique tokens should have been created.",
               tokens,
               hasSize(greaterThanOrEqualTo(GRACE_UNIQUE_PROBE_SIZE)));
  }

  @Test
  public void dealsWithRandomCornerCase() throws Exception {
    RandomTokenGenerator generator = new RandomTokenGenerator() {
      @Override
      long randomTokenNumber() {
        return Long.MIN_VALUE;
      }
    };
    String randomToken = generator.getRandomToken();
    long parsedValue = Long.parseLong(randomToken);
    assertThat("Generator should always create positive numbers as tokens.",
               parsedValue,
               greaterThanOrEqualTo(0L));
  }
}
