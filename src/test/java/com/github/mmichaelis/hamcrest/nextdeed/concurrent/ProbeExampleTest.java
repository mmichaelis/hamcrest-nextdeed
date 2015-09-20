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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.glue.Consumer;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * This test especially exists to show some examples for using Probes.
 *
 * @since SINCE
 */
public class ProbeExampleTest {

  private static final Logger LOG = getLogger(ProbeExampleTest.class);

  @Test
  public void simpleAssertion() throws Exception {
    Deque<String> strings = new ArrayDeque<>(Arrays.asList("Lorem", "Ipsum"));
    Probe.<Deque<String>, String>probing(strings)
        .withinMs(1L)
        .assertThat(
            new Function<Deque<String>, String>() {
              @Override
              public String apply(Deque<String> input) {
                return input.pop().toLowerCase();
              }
            },
            Matchers.equalTo("ipsum")
        );
  }

  @Test
  public void lifecycleObservation() throws Exception {
    Deque<String> strings = new ArrayDeque<>(Arrays.asList("Lorem", "Ipsum"));
    Probe.<Deque<String>, String>probing(strings)
        .withinMs(1L)
        .onTimeout(new Consumer<WaitTimeoutEvent<Deque<String>, String>>() {
          @Override
          public void accept(WaitTimeoutEvent<Deque<String>, String> event) {
            LOG.debug("System state: {}", event.getItem());
            LOG.debug("Last value: {}", event.getLastResult());
          }
        })
        .assertThat(
            new Function<Deque<String>, String>() {
              @Override
              public String apply(Deque<String> input) {
                return input.pop().toLowerCase();
              }
            },
            Matchers.equalTo("ipsum")
        );
  }
}
