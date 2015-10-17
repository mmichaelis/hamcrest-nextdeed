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

import static org.slf4j.LoggerFactory.getLogger;

import com.github.mmichaelis.hamcrest.nextdeed.image.handler.WriteImageHandlerFunction;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @since SINCE
 */
public class WriteImageHandlerFunctionTest {

  private static final Logger LOG = getLogger(WriteImageHandlerFunctionTest.class);
  private static final int SIZE = 8;
  private static final int TILE_SIZE = 2;
  private BufferedImage redWhiteImage;

  @Before
  public void setUp() throws Exception {
    redWhiteImage = new CheckersImageBuilderImpl()
        .width(SIZE)
        .height(SIZE)
        .tileHeight(TILE_SIZE)
        .tileWidth(TILE_SIZE)
        .tileColor(Color.RED)
        .background(Color.WHITE)
        .build();
  }

  @Test
  public void poc() throws Exception {
    WriteImageHandlerFunction function = new WriteImageHandlerFunction();
    String result = function.apply(ImageType.ACTUAL, redWhiteImage);
    LOG.info("Result: {}", result);
  }
}
