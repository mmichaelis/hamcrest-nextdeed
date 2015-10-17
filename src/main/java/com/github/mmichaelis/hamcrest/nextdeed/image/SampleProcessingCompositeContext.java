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

package com.github.mmichaelis.hamcrest.nextdeed.image;

import java.awt.*;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;

/**
 * @since SINCE
 */
public class SampleProcessingCompositeContext implements CompositeContext {

  private final SampleComparisonProcessor sampleComparisonProcessor;

  public SampleProcessingCompositeContext(SampleComparisonProcessor sampleComparisonProcessor) {
    this.sampleComparisonProcessor = sampleComparisonProcessor;
  }

  @Override
  public void dispose() {
  }

  @Override
  public void compose(Raster src, Raster dstIn, WritableRaster dstOut) {
    int height = dstOut.getHeight();
    int width = dstOut.getWidth();
    int bands = dstOut.getNumBands();
    SampleModel sampleModel = dstOut.getSampleModel();
    int dataType = sampleModel.getDataType();
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        for (int b = 0; b < bands; b++) {
          switch (dataType) {
            case DataBuffer.TYPE_BYTE:
              sampleComparisonProcessor
                  .processSample(src, dstIn, dstOut, x, y, b, Byte.MIN_VALUE,
                                 Byte.MAX_VALUE);
              break;
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
              sampleComparisonProcessor
                  .processSample(src, dstIn, dstOut, x, y, b, Short.MIN_VALUE,
                                 Short.MAX_VALUE);
              break;
            case DataBuffer.TYPE_INT:
              sampleComparisonProcessor
                  .processSample(src, dstIn, dstOut, x, y, b, Integer.MIN_VALUE,
                                 Integer.MAX_VALUE);
              break;
            case DataBuffer.TYPE_FLOAT:
              sampleComparisonProcessor.processSampleFloat(src, dstIn, dstOut, x, y, b);
              break;
            case DataBuffer.TYPE_DOUBLE:
              sampleComparisonProcessor.processSampleDouble(src, dstIn, dstOut, x, y, b);
              break;
            case DataBuffer.TYPE_UNDEFINED:
              throw new IllegalArgumentException(
                  String
                      .format("Cannot handle undefined DataBuffer type of sample model %s.",
                              sampleModel));
            default:
              throw new IllegalArgumentException(
                  String.format("Cannot handle unknown DataBuffer type %d of sample model %s.",
                                dataType, sampleModel));
          }
        }
      }
    }
  }


}
