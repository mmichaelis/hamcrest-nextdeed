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

/**
 * @since SINCE
 */
public class CheckersImageBuilderImpl extends BaseImageBuilder implements CheckersImageBuilder {

  private int tileWidth = 1;
  private int tileHeight = 1;
  private Color tileColor = Color.WHITE;

  @NotNull
  @Override
  public CheckersImageBuilder width(int width) {
    super.width(width);
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder height(int height) {
    super.height(height);
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder imageType(int type) {
    super.imageType(type);
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder background(Color color) {
    super.background(color);
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder tileWidth(int tileWidth) {
    this.tileWidth = tileWidth;
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder tileHeight(int tileHeight) {
    this.tileHeight = tileHeight;
    return this;
  }

  @NotNull
  @Override
  public CheckersImageBuilder tileColor(Color tileColor) {
    this.tileColor = tileColor;
    return this;
  }

  @Override
  protected void paint(Graphics2D graphics) {
    super.paint(graphics);

    int width = getWidth();
    int height = getHeight();

    graphics.setColor(tileColor);

    int xTiles = width / tileWidth;
    int yTiles = height / tileHeight;

    for (int xTile = 0; xTile < (xTiles + 1); xTile++) {
      for (int yTile = 0; yTile < (yTiles + 1); yTile++) {
        if ((xTile % 2) == (yTile % 2)) {
          graphics.fillRect(xTile * tileWidth, yTile * tileHeight, tileWidth, tileHeight);
        }
      }
    }

  }
}
