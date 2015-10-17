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

import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * @since SINCE
 */
final class DefaultSampleComparisonProcessor implements SampleComparisonProcessor {

  private static final int DEFAULT_ULP_TOLERANCE = 5;

  @Override
  public boolean processSample(Raster src, Raster dstIn, WritableRaster dstOut, int x, int y,
                               int b,
                               int minSampleValue, int maxSampleValue) {
    int srcSample = src.getSample(x, y, b);
    int dstInSample = dstIn.getSample(x, y, b);
    if (srcSample == dstInSample) {
      dstOut.setSample(x, y, b, 0);
      return true;
    } else {
      dstOut.setSample(x, y, b, maxSampleValue);
      return false;
    }
  }

  @Override
  public boolean processSampleFloat(Raster src, Raster dstIn, WritableRaster dstOut, int x, int y,
                                    int b) {
    float srcSample = src.getSampleFloat(x, y, b);
    float dstInSample = dstIn.getSampleFloat(x, y, b);
    if (areEqual(srcSample, dstInSample)) {
      dstOut.setSample(x, y, b, 0f);
      return true;
    } else {
      dstOut.setSample(x, y, b, Float.MAX_VALUE);
      return false;
    }
  }

  @Override
  public boolean processSampleDouble(Raster src, Raster dstIn, WritableRaster dstOut, int x,
                                     int y,
                                     int b) {
    double srcSample = src.getSampleDouble(x, y, b);
    double dstInSample = dstIn.getSampleDouble(x, y, b);
    if (areEqual(srcSample, dstInSample)) {
      dstOut.setSample(x, y, b, 0f);
      return true;
    } else {
      dstOut.setSample(x, y, b, Double.MAX_VALUE);
      return false;
    }
  }

  private boolean areEqual(float f1, float f2) {
    return (Float.compare(f1, f2) == 0) || (Math.abs(f1 - f2) < epsilon(f1, f2));
  }

  private boolean areEqual(double d1, double d2) {
    return (Double.compare(d1, d2) == 0) || (Math.abs(d1 - d2) < epsilon(d1, d2));
  }

  private float epsilon(float f1, float f2) {
    float baseEpsilon;
    if (Math.abs(f1) > Math.abs(f2)) {
      baseEpsilon = Math.ulp(f1);
    } else {
      baseEpsilon = Math.ulp(f2);
    }
    return DEFAULT_ULP_TOLERANCE * baseEpsilon;
  }

  private double epsilon(double d1, double d2) {
    double baseEpsilon;
    if (Math.abs(d1) > Math.abs(d2)) {
      baseEpsilon = Math.ulp(d1);
    } else {
      baseEpsilon = Math.ulp(d2);
    }
    return DEFAULT_ULP_TOLERANCE * baseEpsilon;
  }
}
