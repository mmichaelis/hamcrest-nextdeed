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

/**
 * <p>
 * Matcher for concurrent changes of the system/component under test.
 * </p>
 * <dl>
 * <dt><strong>Example:</strong></dt>
 * <dd>
 * <pre>{@code Deque<String> strings = new ArrayDeque<>(Arrays.asList("Lorem", "Ipsum"));
 * Probe.<Deque<String>, String>probing(strings)
 *      .withinMs(1L)
 *      .assertThat(
 *                  new Function<Deque<String>, String>() {
 *                    &#64;Override
 *                    public String apply(Deque<String> input) {
 *                      return input.pop().toLowerCase();
 *                    }
 *                  },
 *                  Matchers.equalTo("ipsum")
 *      );
 * }</pre>
 * <p>
 * While this example is somewhat academic you still might get the meaning. While the test above
 * triggers the state change, a typical system of course changes its state over time.
 * </p>
 * </dd>
 * </dl>
 *
 * @since 0.1.0
 */
package com.github.mmichaelis.hamcrest.nextdeed.concurrent;
