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

import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Objects;

/**
 * <p>
 * Represents the different available sample types for buffered images - ignoring undefined sample
 * type.
 * </p>
 *
 * @since SINCE
 */
public enum SampleType {
  /**
   * Byte Type.
   *
   * @see DataBuffer#TYPE_BYTE
   * @since SINCE
   */
  BYTE(
      DataBuffer.TYPE_BYTE,
      Byte.MIN_VALUE,
      Byte.MAX_VALUE,
      Defaults.NUMBER_EQUALS_FUNCTION,
      Defaults.GET_INT_SAMPLE_FUNCTION,
      Defaults.SET_INT_SAMPLE_FUNCTION
  ),
  /**
   * Double Type.
   *
   * @see DataBuffer#TYPE_DOUBLE
   * @since SINCE
   */
  DOUBLE(
      DataBuffer.TYPE_DOUBLE,
      Double.MIN_VALUE,
      Double.MAX_VALUE,
      new DoubleEqualsFunction(),
      new GetFloatSampleFunction(),
      Defaults.SET_INT_SAMPLE_FUNCTION
  ),
  /**
   * Float Type.
   *
   * @see DataBuffer#TYPE_FLOAT
   * @since SINCE
   */
  FLOAT(
      DataBuffer.TYPE_FLOAT,
      Float.MIN_VALUE,
      Float.MAX_VALUE,
      new FloatEqualsFunction(),
      new GetDoubleSampleFunction(),
      Defaults.SET_INT_SAMPLE_FUNCTION
  ),
  /**
   * Integer Type.
   *
   * @see DataBuffer#TYPE_INT
   * @since SINCE
   */
  INTEGER(
      DataBuffer.TYPE_INT,
      Integer.MIN_VALUE,
      Integer.MAX_VALUE,
      Defaults.NUMBER_EQUALS_FUNCTION,
      Defaults.GET_INT_SAMPLE_FUNCTION,
      Defaults.SET_INT_SAMPLE_FUNCTION
  ),
  /**
   * Short Type.
   *
   * @see DataBuffer#TYPE_SHORT
   * @since SINCE
   */
  SHORT(
      DataBuffer.TYPE_SHORT,
      Short.MIN_VALUE,
      Short.MAX_VALUE,
      Defaults.NUMBER_EQUALS_FUNCTION,
      Defaults.GET_INT_SAMPLE_FUNCTION,
      Defaults.SET_INT_SAMPLE_FUNCTION
  ),
  /**
   * Unsigned Short Type.
   *
   * @see DataBuffer#TYPE_USHORT
   * @since SINCE
   */
  USHORT(
      DataBuffer.TYPE_USHORT,
      0,
      (int) Short.MAX_VALUE - (int) Short.MIN_VALUE,
      Defaults.NUMBER_EQUALS_FUNCTION,
      Defaults.GET_INT_SAMPLE_FUNCTION,
      Defaults.SET_INT_SAMPLE_FUNCTION
  );

  private final int dataType;
  @NotNull
  private final Number minValue;
  @NotNull
  private final Number maxValue;
  @NotNull
  private final BiFunction<? super Number, ? super Number, Boolean> equalsFunction;
  @NotNull
  private final GetSampleFunction getSampleFunction;
  @NotNull
  private final SetSampleFunction setSampleFunction;

  SampleType(int dataType,
             @NotNull Number minValue,
             @NotNull Number maxValue,
             @NotNull BiFunction<? super Number, ? super Number, Boolean> equalsFunction,
             @NotNull GetSampleFunction getSampleFunction,
             @NotNull SetSampleFunction setSampleFunction) {
    this.dataType = dataType;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.equalsFunction = equalsFunction;
    this.getSampleFunction = getSampleFunction;
    this.setSampleFunction = setSampleFunction;
  }

  /**
   * Get the sample type for the given data type. null if no sample type is available for the
   * given data type.
   *
   * @param dataType data type (see {@link DataBuffer})
   * @return sample type; or {@code null} if the data type is unsupported
   */
  @Nullable
  public static SampleType forDataType(int dataType) {
    for (SampleType value : values()) {
      if (value.getDataType() == dataType) {
        return value;
      }
    }
    return null;
  }

  public int getDataType() {
    return dataType;
  }

  @NotNull
  public Number getMinValue() {
    return minValue;
  }

  @NotNull
  public Number getMaxValue() {
    return maxValue;
  }

  @NotNull
  public BiFunction<? super Number, ? super Number, Boolean> getEqualsFunction() {
    return equalsFunction;
  }

  @NotNull
  public GetSampleFunction getGetSampleFunction() {
    return getSampleFunction;
  }

  @NotNull
  public SetSampleFunction getSetSampleFunction() {
    return setSampleFunction;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("dataType", dataType)
        .add("minValue", minValue)
        .add("maxValue", maxValue)
        .add("super", super.toString())
        .toString();
  }

  private interface Defaults {

    BiFunction<? super Number, ? super Number, Boolean>
        NUMBER_EQUALS_FUNCTION =
        new BiFunction<Number, Number, Boolean>() {
          @Override
          public Boolean apply(Number a, Number b) {
            return Objects.equals(a, b);
          }
        };
    GetSampleFunction
        GET_INT_SAMPLE_FUNCTION =
        new GetSampleFunction() {
          @Override
          public Number apply(@NotNull Raster rst, int x, int y, int b) {
            return rst.getSample(x, y, b);
          }
        };
    SetSampleFunction
        SET_INT_SAMPLE_FUNCTION =
        new SetSampleFunction() {
          @Override
          public void apply(@NotNull WritableRaster rst, int x, int y, int b, @NotNull Number sampleValue) {
            rst.setSample(x, y, b, (Integer) sampleValue);
          }
        };
    int ULP_TOLERANCE = 5;
  }

  private static class DoubleEqualsFunction implements BiFunction<Number, Number, Boolean> {

    private double epsilon(double f1, double f2) {
      double baseEpsilon;
      if (Math.abs(f1) > Math.abs(f2)) {
        baseEpsilon = Math.ulp(f1);
      } else {
        baseEpsilon = Math.ulp(f2);
      }
      return Defaults.ULP_TOLERANCE * baseEpsilon;
    }

    @Override
    public Boolean apply(Number n1, Number n2) {
      Double d1 = n1.doubleValue();
      Double d2 = n2.doubleValue();
      return (Double.compare(d1, d2) == 0) || (Math.abs(d1 - d2) < epsilon(d1, d2));
    }


  }

  private static class FloatEqualsFunction implements BiFunction<Number, Number, Boolean> {

    private float epsilon(float f1, float f2) {
      float baseEpsilon;
      if (Math.abs(f1) > Math.abs(f2)) {
        baseEpsilon = Math.ulp(f1);
      } else {
        baseEpsilon = Math.ulp(f2);
      }
      return Defaults.ULP_TOLERANCE * baseEpsilon;
    }

    @Override
    public Boolean apply(Number n1, Number n2) {
      Float f1 = n1.floatValue();
      Float f2 = n2.floatValue();
      return (Float.compare(f1, f2) == 0) || (Math.abs(f1 - f2) < epsilon(f1, f2));
    }


  }

  private static class GetFloatSampleFunction implements GetSampleFunction {

    @Override
    public Number apply(@NotNull Raster rst, int x, int y, int b) {
      return rst.getSampleFloat(x, y, b);
    }
  }

  private static class GetDoubleSampleFunction implements GetSampleFunction {

    @Override
    public Number apply(@NotNull Raster rst, int x, int y, int b) {
      return rst.getSampleDouble(x, y, b);
    }
  }
}
