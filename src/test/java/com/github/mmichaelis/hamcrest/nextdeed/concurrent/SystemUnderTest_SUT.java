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

import com.google.common.base.MoreObjects;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/**
 * System under Test.
 *
 * @since 1.0.0
 */
class SystemUnderTest_SUT {

  @NotNull
  private final Deque<SystemState> states;

  @NotNull
  private SystemState state = SystemState.STOPPED;

  /**
   * Constructor specifying states to take when queried. Each time queried the state will change
   * to the next state in the list. If all states are consumed, the last state will be the final
   * state answered each time when queried.
   */
  SystemUnderTest_SUT(@NotNull SystemState... states) {
    this(new ArrayDeque<>(Arrays.asList(states)));
  }

  private SystemUnderTest_SUT(@NotNull Deque<SystemState> states) {
    this.states = states;
  }

  /**
   * Retrieve state and as side effect switch to next specified state.
   *
   * @return current state
   */
  @NotNull
  public SystemState getState() {
    if (!states.isEmpty()) {
      state = states.pop();
    }
    return state;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("state", state)
        .add("states", states)
        .toString();
  }
}
