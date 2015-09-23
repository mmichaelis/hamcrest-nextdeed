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

package com.github.mmichaelis.hamcrest.nextdeed.concurrent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for concurrent tests.
 *
 * @since 1.0.0
 */
public final class ConcurrentTestUtil {

  private ConcurrentTestUtil() {
    // Utility class
  }

  /**
   * Transform list of used time millis to the system millis returned during wait function. Thus
   * this method internally knows exactly how many times the system millis are queried and needs
   * to be adopted if this after changes.
   *
   * @param usedTimeMillis how long each call to the system takes
   * @return time millis required for mocking wait function
   */
  @NotNull
  public static List<Long> getTimeMillis(Iterable<Long> usedTimeMillis) {
    List<Long> timeMillis = new ArrayList<>();
    long currentTime = 0L;
    // start time to calculate timeout time
    timeMillis.add(currentTime);
    for (Long usedTimeMilli : usedTimeMillis) {
      // time before evaluation
      timeMillis.add(currentTime);
      currentTime += usedTimeMilli;
      // time after evaluation
      timeMillis.add(currentTime);
    }
    return timeMillis;
  }
}
