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

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * @since SINCE
 */
public class DefaultSampleComparisonProcessor implements SampleComparisonProcessor {

  @Override
  public ProcessSampleResult processSample(@NotNull SampleType sampleType,
                                           @NotNull Raster src,
                                           @NotNull Raster dstIn,
                                           @NotNull WritableRaster dstOut,
                                           int x,
                                           int y,
                                           int b) {
    GetSampleFunction getSampleFunction = sampleType.getGetSampleFunction();
    SetSampleFunction setSampleFunction = sampleType.getSetSampleFunction();
    NumberEqualsFunction equalsFunction = sampleType.getEqualsFunction();

    Number srcSample = getSampleFunction.apply(src, x, y, b);
    Number dstInSample = getSampleFunction.apply(dstIn, x, y, b);

    if (equalsFunction.apply(srcSample, dstInSample)) {
      setSampleFunction.apply(dstOut, x, y, b, sampleType.getMinValue());
      return ProcessSampleResult.EQUAL;
    } else {
      setSampleFunction.apply(dstOut, x, y, b, sampleType.getMaxValue());
      return ProcessSampleResult.DIFFERENT;
    }
  }

}
