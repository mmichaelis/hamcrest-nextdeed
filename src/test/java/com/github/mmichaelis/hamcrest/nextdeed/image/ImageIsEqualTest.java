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

import static com.github.mmichaelis.hamcrest.nextdeed.image.ImageIsEqual.imageEqualTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails;
import com.github.mmichaelis.hamcrest.nextdeed.image.handler.WriteImageHandlerFunction;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;


/**
 * Tests {@link ImageIsEqual}.
 *
 * @since SINCE
 */
public class ImageIsEqualTest {

  private static final Logger LOG = getLogger(ImageIsEqualTest.class);

  private static final int SIZE = 8;
  private static final int TILE_SIZE = 2;
  private BufferedImage redWhiteImage;
  private BufferedImage anotherRedWhiteImage;
  private BufferedImage greenWhiteImage;

  @Rule
  public TestName testNameConfigurationPropagator = new PropagatedTestDetails();

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
    anotherRedWhiteImage = new CheckersImageBuilderImpl()
        .width(SIZE)
        .height(SIZE)
        .tileHeight(TILE_SIZE)
        .tileWidth(TILE_SIZE)
        .tileColor(Color.RED)
        .background(Color.WHITE)
        .build();
    greenWhiteImage = new CheckersImageBuilderImpl()
        .width(SIZE)
        .height(SIZE)
        .tileHeight(TILE_SIZE)
        .tileWidth(TILE_SIZE)
        .tileColor(Color.GREEN)
        .background(Color.WHITE)
        .build();
  }

  @Test
  public void imageIsEqualToItself() throws Exception {
    assertThat(redWhiteImage, imageEqualTo(redWhiteImage));
  }

  @Test
  public void imageIsEqualToSameImage() throws Exception {
    assertThat(redWhiteImage, imageEqualTo(anotherRedWhiteImage));
  }

  @Test
  public void imageIsNotEqualToOther() throws Exception {
    assertThat(redWhiteImage, not(imageEqualTo(greenWhiteImage)));
  }

  @Test
  @Ignore("fails, replace with message validation")
  public void pocFailure() throws Exception {
    assertThat(redWhiteImage, imageEqualTo(greenWhiteImage));
  }

  @Test
  @Ignore("fails, replace with message validation")
  public void pocFailure2() throws Exception {
    assertThat(redWhiteImage, imageEqualTo(greenWhiteImage, new WriteImageHandlerFunction()));
  }

  @Test
  @Ignore("fails, replace with message validation")
  public void pocFailure3() throws Exception {
    assertThat(redWhiteImage, not(
        imageEqualTo(anotherRedWhiteImage, new WriteImageHandlerFunction())));
  }

}
