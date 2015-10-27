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

import static java.lang.String.format;

import com.google.common.base.MoreObjects;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.HashSet;

/**
 * @since SINCE
 */
public class SampleProcessingCompositeContext implements CompositeContext {

  private final Collection<PixelProcessingListener> pixelListeners = new HashSet<>(1);

  @NotNull
  private final SampleComparisonProcessor sampleComparisonProcessor;

  public SampleProcessingCompositeContext(
      @NotNull SampleComparisonProcessor sampleComparisonProcessor) {
    this.sampleComparisonProcessor = sampleComparisonProcessor;
  }

  public void addPixelListener(@NotNull PixelProcessingListener listener) {
    pixelListeners.add(listener);
  }

  public void removePixelListener(@NotNull PixelProcessingListener listener) {
    pixelListeners.remove(listener);
  }

  @Override
  public void dispose() {
  }

  @Override
  public void compose(Raster src, Raster dstIn, @NotNull WritableRaster dstOut) {
    int height = dstOut.getHeight();
    int width = dstOut.getWidth();
    int bands = dstOut.getNumBands();
    SampleType sampleType = getSampleType(dstOut);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        ProcessSampleResult
            pixelResult =
            collectPixelResultFromBands(src, dstIn, dstOut, sampleType, x, y, bands);
        firePixelEvent(pixelResult, new PixelProcessingEventObject(this, x, y, pixelResult));
      }
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("pixelListeners", pixelListeners)
        .add("sampleComparisonProcessor", sampleComparisonProcessor)
        .toString();
  }

  @NotNull
  private ProcessSampleResult collectPixelResultFromBands(Raster src,
                                                          Raster dstIn,
                                                          @NotNull WritableRaster dstOut,
                                                          @NotNull SampleType sampleType,
                                                          int x,
                                                          int y,
                                                          int bands) {
    ProcessSampleResult pixelResult = ProcessSampleResult.EQUAL;
    for (int b = 0; b < bands; b++) {
      ProcessSampleResult bandPixelResult =
          sampleComparisonProcessor.processSample(sampleType, src, dstIn, dstOut, x, y, b);
      pixelResult =
          refreshedPixelResult(pixelResult,
                               bandPixelResult
          );
    }
    return pixelResult;
  }

  @NotNull
  private SampleType getSampleType(@NotNull WritableRaster dstOut) {
    SampleModel sampleModel = dstOut.getSampleModel();
    int dataType = sampleModel.getDataType();
    SampleType sampleType = SampleType.forDataType(dataType);
    if (sampleType == null) {
      throw new IllegalStateException(
          format("Cannot handle sample data type %d.", dataType));
    }
    return sampleType;
  }

  private void firePixelEvent(@NotNull ProcessSampleResult pixelResult,
                              @NotNull PixelProcessingEvent event) {
    switch (pixelResult) {
      case DIFFERENT:
        for (PixelProcessingListener listener : pixelListeners) {
          listener.isDifferent(event);
        }
        break;
      case EQUAL:
        for (PixelProcessingListener listener : pixelListeners) {
          listener.isEqual(event);
        }
        break;
      default:
        throw new IllegalStateException(
            format("Unknown process sample result: %s.", event));
    }
  }

  /**
   * Calculates the new pixel result based on the previous result and the band pixel result.
   *
   * @param pixelResult     previous pixel result
   * @param bandPixelResult new result from band pixel evaluation
   * @return new pixel result
   */
  @NotNull
  private ProcessSampleResult refreshedPixelResult(@NotNull ProcessSampleResult pixelResult,
                                                   @NotNull ProcessSampleResult bandPixelResult) {
    ProcessSampleResult result = pixelResult;
    switch (bandPixelResult) {
      case DIFFERENT:
        result = ProcessSampleResult.DIFFERENT;
        break;
      case EQUAL:
        break;
      default:
        throw new IllegalStateException(
            format("Unknown process sample result: %s.", bandPixelResult));
    }
    return result;
  }
}
