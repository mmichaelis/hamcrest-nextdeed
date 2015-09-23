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

import static org.mockito.Matchers.anyLong;

import com.google.common.base.Function;

import org.mockito.AdditionalAnswers;
import org.mockito.Mockito;

import java.util.List;

/**
 * @since 1.0.0
 */
public class OnWaitFunctionSpy {
  private WaitFunction<SystemUnderTest_SUT, SystemState> waitFunction;

  public OnWaitFunctionSpy(ProbeBuilderImpl<SystemUnderTest_SUT, SystemState> configuredProbe,
                           Iterable<Long> usedTimeMillis) {
    initSpyOnWaitFunction(configuredProbe, usedTimeMillis);
  }

  public WaitFunction<SystemUnderTest_SUT, SystemState> getWaitFunction() {
    return waitFunction;
  }

  /**
   * Creates spy on wait function. Spy won't wait and the system time returned will be build from
   * the given used time millis. So you only need to specify how long each call to the system will
   * take.
   *
   * @param configuredProbe probe to configure wait function for
   * @param usedTimeMillis  array of used times in milliseconds
   */
  private void initSpyOnWaitFunction(
      ProbeBuilderImpl<SystemUnderTest_SUT, SystemState> configuredProbe,
      Iterable<Long> usedTimeMillis) {
    final List<Long> timeMillis = ConcurrentTestUtil.getTimeMillis(usedTimeMillis);

    configuredProbe.preProcessWaitFunction(
        new Function<Function<SystemUnderTest_SUT, SystemState>, Function<SystemUnderTest_SUT, SystemState>>() {
          @Override
          public Function<SystemUnderTest_SUT, SystemState> apply(
              Function<SystemUnderTest_SUT, SystemState> input) {
            WaitFunction<SystemUnderTest_SUT, SystemState> spy =
                (WaitFunction<SystemUnderTest_SUT, SystemState>) Mockito.spy(input);
            try {
              Mockito.doNothing().when(spy).sleep(anyLong());
              Mockito.doAnswer(
                  AdditionalAnswers.returnsElementsOf(timeMillis)).when(spy).nowMillis();
            } catch (InterruptedException ignored) {
            }
            waitFunction = spy;
            return spy;
          }
        });
  }

  public static OnWaitFunctionSpy spyOnWaitFunction(
      ProbeBuilderImpl<SystemUnderTest_SUT, SystemState> configuredProbe,
      Iterable<Long> usedTimeMillis) {
    return new OnWaitFunctionSpy(configuredProbe, usedTimeMillis);
  }

}
