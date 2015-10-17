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

import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

/**
 * @since SINCE
 */
public class ImageIsEqual extends TypeSafeMatcher<BufferedImage> {

  private static final Logger LOG = getLogger(ImageIsEqual.class);
  private static final ThreadLocal<BufferedImage> tlDiffImage = new ThreadLocal<>();

  @Nullable
  private final BufferedImage expectedImage;
  @Nullable
  private final BiFunction<ImageType, BufferedImage, String> imageHandlerFunction;

  public ImageIsEqual(@Nullable BufferedImage expectedImage) {
    this(expectedImage, null);
  }

  public ImageIsEqual(@Nullable BufferedImage expectedImage,
                      @Nullable BiFunction<ImageType, BufferedImage, String> imageHandlerFunction) {
    this.expectedImage = expectedImage;
    this.imageHandlerFunction = imageHandlerFunction;
  }

  @Override
  public void describeTo(Description description) {
    if (imageHandlerFunction != null) {
      description.appendText(imageHandlerFunction.apply(ImageType.EXPECTED, expectedImage));
    } else {
      description.appendValue(expectedImage);
    }
  }

  @Override
  protected boolean matchesSafely(BufferedImage actualImage) {
    disposeDiffImage();
    if (actualImage == null) {
      return expectedImage == null;
    }
    if (actualImage == expectedImage) {
      return true;
    }
    return isComparableTo(actualImage) && imagesAreEqual(actualImage) && disposeDiffImage();
  }

  @Override
  protected void describeMismatchSafely(BufferedImage actualImage,
                                        Description mismatchDescription) {
    try {
      if ((actualImage == null) || (expectedImage == null)) {
        super.describeMismatchSafely(actualImage, mismatchDescription);
      } else if (!widthIsEqualTo(actualImage) && !heightIsEqualTo(actualImage)) {
        mismatchDescription.appendText("has different dimensions than ").appendValue(actualImage);
      } else if (!typeIsEqualTo(actualImage)) {
        mismatchDescription.appendText("has type ")
            .appendText(Helper.imageTypeString(actualImage))
            .appendText(" rather than expected ")
            .appendText(Helper.imageTypeString(expectedImage));
      } else if (!colorModelIsEqualTo(actualImage)) {
        mismatchDescription.appendText("has different color model than ").appendValue(actualImage);
      } else if (!isComparableTo(actualImage)) {
        mismatchDescription.appendText("was not comparable to ").appendValue(actualImage);
      } else if (imageHandlerFunction == null) {
        super.describeMismatchSafely(actualImage, mismatchDescription);
      } else {
        BufferedImage difference = tlDiffImage.get();
        String actualText = imageHandlerFunction.apply(ImageType.ACTUAL, actualImage);
        String differenceText = imageHandlerFunction.apply(ImageType.DIFFERENCE, difference);
        mismatchDescription
            .appendText("is different to ")
            .appendText(actualText)
            .appendText(" as can be seen in ")
            .appendText(differenceText);
      }
    } finally {
      disposeDiffImage();
    }
  }

  private boolean disposeDiffImage() {
    tlDiffImage.remove();
    return true;
  }

  private boolean imagesAreEqual(@NotNull BufferedImage actualImage) {
    BufferedImage diffImageTarget = createDiffImageTarget(actualImage);
    tlDiffImage.set(diffImageTarget);
    return imagesAreEqual(actualImage, diffImageTarget);
  }

  private boolean imagesAreEqual(@NotNull BufferedImage actualImage,
                                 @NotNull BufferedImage diffImage) {
    final SampleComparisonProcessor sampleComparisonProcessor = new DefaultSampleComparisonProcessor();
    Graphics2D graphics = diffImage.createGraphics();
    graphics.drawImage(expectedImage, 0, 0, null);
    graphics.setComposite(new Composite() {
      @Override
      public CompositeContext createContext(ColorModel srcColorModel,
                                            ColorModel dstColorModel,
                                            RenderingHints hints) {
        return new SampleProcessingCompositeContext(sampleComparisonProcessor);
      }
    });
    graphics.drawImage(actualImage, 0, 0, new ImageObserver() {
      @Override
      public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
        LOG.info("imageUpdate: I<{}>, Inf<{}>, x<{}>, y<{}>, w<{}>, h<{}>", img, infoflags, x, y,
                 width, height);
        return true;
      }
    });
    graphics.dispose();
    return false;
  }

  @NotNull
  private BufferedImage createDiffImageTarget(@NotNull BufferedImage actualImage) {
    BufferedImage difference;
    if (actualImage.getColorModel() instanceof IndexColorModel) {
      difference =
          new BufferedImage(actualImage.getWidth(), actualImage.getHeight(), actualImage.getType(),
                            (IndexColorModel) actualImage.getColorModel());
    } else {
      difference =
          new BufferedImage(actualImage.getWidth(), actualImage.getHeight(), actualImage.getType());
    }
    return difference;
  }

  private boolean isComparableTo(@NotNull BufferedImage actualImage) {
    return typeIsEqualTo(actualImage)
           && widthIsEqualTo(actualImage)
           && heightIsEqualTo(actualImage)
           && colorModelIsEqualTo(actualImage);
  }

  private boolean typeIsEqualTo(@NotNull BufferedImage actualImage) {
    assert expectedImage != null;
    return actualImage.getType() == expectedImage.getType();
  }

  private boolean widthIsEqualTo(@NotNull RenderedImage actualImage) {
    assert expectedImage != null;
    return actualImage.getWidth() == expectedImage.getWidth();
  }

  private boolean heightIsEqualTo(@NotNull RenderedImage actualImage) {
    assert expectedImage != null;
    return actualImage.getHeight() == expectedImage.getHeight();
  }

  private boolean colorModelIsEqualTo(@NotNull RenderedImage actualImage) {
    assert expectedImage != null;
    return actualImage.getColorModel().equals(expectedImage.getColorModel());
  }

}
