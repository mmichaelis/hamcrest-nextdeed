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

import org.jetbrains.annotations.NotNull;

import java.util.EventObject;

/**
 * @since SINCE
 */
public class BandPixelProcessingEventObject extends EventObject implements
                                                                BandPixelProcessingEvent {

  @NotNull
  private final ProcessSampleResult result;
  @NotNull
  private final BandPixel bandPixel;

  /**
   * Constructs a sample processing result event.
   *
   * @param source The object on which the Event initially occurred.
   * @param x      x coordinate
   * @param y      y coordinate
   * @param b      band
   * @param result process sample result
   * @throws IllegalArgumentException if source is null.
   */
  public BandPixelProcessingEventObject(@NotNull Object source,
                                        int x,
                                        int y,
                                        int b,
                                        @NotNull ProcessSampleResult result) {
    super(source);
    bandPixel = new BandPixelImpl(x, y, b);
    this.result = result;
  }

  @NotNull
  @Override
  public BandPixel getBandPixel() {
    return bandPixel;
  }

  @NotNull
  @Override
  public ProcessSampleResult getResult() {
    return result;
  }
}
