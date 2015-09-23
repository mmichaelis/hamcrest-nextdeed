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

package com.github.mmichaelis.hamcrest.nextdeed;

import com.google.common.base.Function;

import org.jetbrains.annotations.NotNull;

/**
 * @since 1.0.0
 */
public class TestFunctions {

  @NotNull
  public static <T extends Throwable> Function<T, String> messageFunction() {
    return new Function<T, String>() {
      @Override
      public String apply(T input) {
        return input.getMessage();
      }
    };
  }
}
