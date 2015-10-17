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

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @since SINCE
 */
public class BaseImageBuilder implements ImageBuilder {

  private int width = 1;
  private int height = 1;
  private int type = BufferedImage.TYPE_INT_RGB;
  private Color backgroundColor = Color.BLACK;

  @NotNull
  @Override
  public ImageBuilder width(int width) {
    this.width = width;
    return this;
  }

  protected void paint(Graphics2D graphics) {
    graphics.setBackground(backgroundColor);
    graphics.clearRect(0, 0, width, height);
  }

  @NotNull
  @Override
  public ImageBuilder height(int height) {
    this.height = height;
    return this;
  }

  @NotNull
  @Override
  public ImageBuilder imageType(int type) {
    this.type = type;
    return this;
  }

  @NotNull
  @Override
  public ImageBuilder background(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
    return this;
  }


  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public int getType() {
    return type;
  }

  public Color getBackgroundColor() {
    return backgroundColor;
  }

  @NotNull
  @Override
  public BufferedImage build() {
    BufferedImage image = new BufferedImage(width, height, type);
    Graphics2D graphics = image.createGraphics();
    try {
      paint(graphics);
    } finally {
      graphics.dispose();
    }
    return image;
  }
}
