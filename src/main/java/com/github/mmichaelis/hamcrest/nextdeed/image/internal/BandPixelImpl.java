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

import java.util.Objects;

/**
 * @since SINCE
 */
public class BandPixelImpl implements BandPixel {

  private final int b;
  @NotNull
  private final PixelImpl pixel;

  public BandPixelImpl(int x, int y, int b) {
    pixel = new PixelImpl(x, y);
    this.b = b;
  }

  @Override
  public int getX() {
    return pixel.getX();
  }

  @Override
  public int getY() {
    return pixel.getY();
  }

  @Override
  public int getBand() {
    return b;
  }

  @Override
  public Pixel getPixel() {
    return pixel;
  }

  @Override
  public int hashCode() {
    return Objects.hash(b, pixel);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if ((o == null) || (getClass() != o.getClass())) {
      return false;
    }
    BandPixelImpl bandPixel = (BandPixelImpl) o;
    return Objects.equals(b, bandPixel.b) &&
           Objects.equals(pixel, bandPixel.pixel);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("pixel", pixel)
        .add("b", b)
        .toString();
  }
}
