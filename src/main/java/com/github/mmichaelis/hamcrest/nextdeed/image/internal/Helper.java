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

import java.awt.image.BufferedImage;

/**
 * @since SINCE
 */
public final class Helper {

  private Helper() {
  }

  @NotNull
  public static String imageTypeString(@NotNull BufferedImage image) {
    return imageTypeString(image.getType());
  }

  @NotNull
  public static String imageTypeString(int type) {
    switch (type) {
      case BufferedImage.TYPE_3BYTE_BGR:
        return "TYPE_3BYTE_BGR";
      case BufferedImage.TYPE_4BYTE_ABGR:
        return "TYPE_4BYTE_ABGR";
      case BufferedImage.TYPE_4BYTE_ABGR_PRE:
        return "TYPE_4BYTE_ABGR_PRE";
      case BufferedImage.TYPE_BYTE_BINARY:
        return "TYPE_BYTE_BINARY";
      case BufferedImage.TYPE_BYTE_GRAY:
        return "TYPE_BYTE_BINARY";
      case BufferedImage.TYPE_BYTE_INDEXED:
        return "TYPE_BYTE_INDEXED";
      case BufferedImage.TYPE_CUSTOM:
        return "TYPE_CUSTOM";
      case BufferedImage.TYPE_INT_ARGB:
        return "TYPE_INT_ARGB";
      case BufferedImage.TYPE_INT_ARGB_PRE:
        return "TYPE_INT_ARGB_PRE";
      case BufferedImage.TYPE_INT_BGR:
        return "TYPE_INT_BGR";
      case BufferedImage.TYPE_INT_RGB:
        return "TYPE_INT_RGB";
      case BufferedImage.TYPE_USHORT_555_RGB:
        return "TYPE_USHORT_555_RGB";
      case BufferedImage.TYPE_USHORT_565_RGB:
        return "TYPE_USHORT_565_RGB";
      case BufferedImage.TYPE_USHORT_GRAY:
        return "TYPE_USHORT_GRAY";
      default:
        return String.format("UNKNOWN(%d)", type);
    }
  }
}
