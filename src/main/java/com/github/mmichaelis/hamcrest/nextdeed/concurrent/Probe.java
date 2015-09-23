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

/**
 * <p>
 * Utility class for launching probes on a system.
 * </p>
 * <dl>
 * <dt><strong>Assert/Assume/Require:</strong></dt>
 * <dd>
 * <p>
 * When to use which validation of the system under test?
 * </p>
 * <dl>
 * <dt><em>Assert:</em></dt>
 * <dd>
 * Use this if you are about to test that the system reaches the requested state. On
 * failure your test case will be marked as <em>Failure</em>.
 * </dd>
 * <dt><em>Assume:</em></dt>
 * <dd>
 * Use this for programmatically ignoring your tests (instead of using for example annotations).
 * On failure your test case will be marked as <em>Ignored</em>.
 * </dd>
 * <dt><em>Require:</em></dt>
 * <dd>
 * In tests use this to check for preconditions which, if failed, will prevent you from doing
 * your actual test. On failure your test case will be marked as <em>Error</em>.
 * </dd>
 * </dl>
 * </dd>
 * <dt><strong>Example:</strong></dt>
 * <dd>
 * <pre>{@code
 * Probe.<System, State>probing(systemUnderTest)
 *      .withinMs(1000L);
 *      .assertThat(new Function<System,State>(){...}, equalTo(RUNNING));
 * }</pre>
 * <p>
 * Mind that it is required to add the generic type parameters to {@code probing} &mdash; which
 * in return will keep you from using static import for {@code probing}. The advantage is
 * that you see right at the start what is your system under test and what is the state type
 * you will wait for.
 * </p>
 * </dd>
 * <dt><strong>Note:</strong></dt>
 * <dd>
 * <p>
 * This class can be compared to Hamcrest's {@code MatcherAssert}. So whenever you want to
 * assert/assume/require giving some grace period to the system under test this class is
 * the one you should choose.
 * </p>
 * </dd>
 * </dl>
 *
 * @since 1.0.0
 */
public final class Probe {

  /**
   * Utility class constructor. You must not instantiate this :-)
   *
   * @since 1.0.0
   */
  private Probe() {
    // Utility class
  }

  /**
   * First specify what system you want to probe.
   *
   * @param <T>    the type of system you are probing
   * @param <R>    the type of state variable you are polling
   * @param target the system under test
   * @return Builder for your waiting assertion, ...
   * @since 1.0.0
   */
  @NotNull
  public static <T, R> ProbeBuilder<T, R> probing(@NotNull T target) {
    return new ProbeBuilderImpl<>(target);
  }

}
