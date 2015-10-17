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

package com.github.mmichaelis.hamcrest.nextdeed.image.internal;

import com.google.common.base.MoreObjects;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @since SINCE
 */
public class PixelCountingSampleProcessingListener extends AbstractPixelProcessingListener {

  private final AtomicLong different = new AtomicLong();
  private final AtomicLong equal = new AtomicLong();

  @Override
  public void isEqual(@NotNull PixelProcessingEvent event) {
    equal.incrementAndGet();
  }

  @Override
  public void isDifferent(@NotNull PixelProcessingEvent event) {
    different.incrementAndGet();
  }

  public long getDifferent() {
    return different.get();
  }

  public long getEqual() {
    return equal.get();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("different", different)
        .add("equal", equal)
        .add("super", super.toString())
        .toString();
  }
}
