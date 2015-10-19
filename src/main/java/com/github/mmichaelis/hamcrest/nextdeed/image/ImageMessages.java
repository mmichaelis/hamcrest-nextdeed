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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.ColorModel;

/**
 * Messages used within this package backed by {@code Bundle.properties}.
 *
 * @since SINCE
 */
interface ImageMessages {

  @NotNull
  String colorModelDiffers(@NotNull ColorModel expectedColorModel,
                           @NotNull ColorModel actualColorModel);

  @NotNull
  String heightDiffers(int expectedImageHeight, int actualImageHeight);

  @NotNull
  String typeDiffers(@NotNull String expectedType,
                     @NotNull String actualType);

  @NotNull
  String widthDiffers(int expectedImageWidth, int actualImageWidth);

  /**
   * Message that images are different at a certain number of pixels.
   *
   * @param differentPixels number of differing pixels
   * @param hasImage 0 to don't display image text, 1 to display image text
   * @param imageText image text; might be {@code null} if {@code hasImage} is 0
   * @return
   */
  @NotNull
  String imageDiffers(long differentPixels, int hasImage, @Nullable String imageText);
}
