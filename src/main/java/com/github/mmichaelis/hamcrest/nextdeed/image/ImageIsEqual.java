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

import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.DefaultSampleComparisonProcessor;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.Helper;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.PixelCountingSampleProcessingListener;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.SampleComparisonProcessor;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.SampleProcessingCompositeContext;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;

/**
 * Compares two images if they are equal.
 *
 * @since SINCE
 */
public class ImageIsEqual extends TypeSafeMatcher<BufferedImage> {

  private static final ThreadLocal<BufferedImage> tlDiffImage = new ThreadLocal<>();

  @NotNull
  private final BufferedImage expectedImage;
  @Nullable
  private final BiFunction<ImageType, BufferedImage, String> imageHandlerFunction;

  /**
   * <p>
   * Comparison without image handler. On failure standard mismatch description is generated.
   * </p>
   *
   * @param expectedImage image to compare to
   * @since SINCE
   */
  public ImageIsEqual(@NotNull BufferedImage expectedImage) {
    this(expectedImage, null);
  }

  /**
   * <p>
   * Comparison with image handler. The image handler will be called upon mismatch description to
   * each image used during comparison and might provide more details of the image or even store
   * the images for later reference in a report folder.
   * </p>
   * <dl>
   * <dt><strong>Image Handler:</strong></dt>
   * <dd>
   * <p>
   * The image handler gets the type of the image (actual, expected or difference) and the
   * corresponding image. The output will be appended to the mismatch description and might
   * either provide some details on the image or if you store the files you might want to
   * output the location where to find the image.
   * </p>
   * </dd>
   * </dl>
   *
   * @param expectedImage        expected image
   * @param imageHandlerFunction image handler called during mismatch description
   * @since SINCE
   */
  public ImageIsEqual(@NotNull BufferedImage expectedImage,
                      @Nullable BiFunction<ImageType, BufferedImage, String> imageHandlerFunction) {
    this.expectedImage = expectedImage;
    this.imageHandlerFunction = imageHandlerFunction;
  }

  /**
   * <p>
   * Comparison without image handler. On failure standard mismatch description is generated.
   * </p>
   *
   * @param expectedImage image to compare to
   * @return matcher
   * @since SINCE
   */
  @NotNull
  public static Matcher<BufferedImage> imageEqualTo(@NotNull BufferedImage expectedImage) {
    return new ImageIsEqual(expectedImage);
  }

  /**
   * <p>
   * Comparison with image handler. The image handler will be called upon mismatch description to
   * each image used during comparison and might provide more details of the image or even store
   * the images for later reference in a report folder.
   * </p>
   * <dl>
   * <dt><strong>Image Handler:</strong></dt>
   * <dd>
   * <p>
   * The image handler gets the type of the image (actual, expected or difference) and the
   * corresponding image. The output will be appended to the mismatch description and might
   * either provide some details on the image or if you store the files you might want to
   * output the location where to find the image.
   * </p>
   * </dd>
   * </dl>
   *
   * @param expectedImage        expected image
   * @param imageHandlerFunction image handler called during mismatch description
   * @return matcher
   * @since SINCE
   */
  @NotNull
  public static Matcher<BufferedImage> imageEqualTo(@NotNull BufferedImage expectedImage,
                                                    @NotNull BiFunction<ImageType, BufferedImage, String> imageHandlerFunction) {
    return new ImageIsEqual(expectedImage, imageHandlerFunction);
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
  protected boolean matchesSafely(@NotNull BufferedImage actualImage) {
    disposeDiffImage();
    if (actualImage == expectedImage) {
      return true;
    }
    return isComparableTo(actualImage) && imagesAreEqual(actualImage) && disposeDiffImage();
  }

  @Override
  protected void describeMismatchSafely(@NotNull BufferedImage actualImage,
                                        @NotNull Description mismatchDescription) {
    String actualText =
        (imageHandlerFunction == null) ? String.valueOf(actualImage)
                                       : imageHandlerFunction.apply(ImageType.ACTUAL, actualImage);

    try {
      if (!widthIsEqualTo(actualImage) && !heightIsEqualTo(actualImage)) {
        mismatchDescription.appendText(actualText).appendText(" has different dimensions");
      } else if (!typeIsEqualTo(actualImage)) {
        mismatchDescription
            .appendText(actualText)
            .appendText(" has type ")
            .appendText(Helper.imageTypeString(actualImage))
            .appendText(" rather than expected ")
            .appendText(Helper.imageTypeString(expectedImage));
      } else if (!colorModelIsEqualTo(actualImage)) {
        mismatchDescription.appendText(actualText).appendText("has different color model");
      } else if (!isComparableTo(actualImage)) {
        mismatchDescription.appendText("was not comparable to ").appendText(actualText);
      } else if (imageHandlerFunction == null) {
        super.describeMismatchSafely(actualImage, mismatchDescription);
      } else {
        BufferedImage difference = tlDiffImage.get();
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
    final PixelCountingSampleProcessingListener
        pixelProcessingListener =
        new PixelCountingSampleProcessingListener();
    Graphics2D graphics = diffImage.createGraphics();
    graphics.drawImage(expectedImage, 0, 0, null);
    graphics.setComposite(new Composite() {
      @Override
      public CompositeContext createContext(ColorModel srcColorModel,
                                            ColorModel dstColorModel,
                                            RenderingHints hints) {
        SampleComparisonProcessor sampleComparisonProcessor =
            new DefaultSampleComparisonProcessor();
        SampleProcessingCompositeContext
            compositeContext =
            new SampleProcessingCompositeContext(sampleComparisonProcessor);
        compositeContext.addPixelListener(pixelProcessingListener);
        return compositeContext;
      }
    });
    graphics.drawImage(actualImage, 0, 0, null);
    graphics.dispose();
    return pixelProcessingListener.getDifferent() == 0;
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
    return actualImage.getType() == expectedImage.getType();
  }

  private boolean widthIsEqualTo(@NotNull RenderedImage actualImage) {
    return actualImage.getWidth() == expectedImage.getWidth();
  }

  private boolean heightIsEqualTo(@NotNull RenderedImage actualImage) {
    return actualImage.getHeight() == expectedImage.getHeight();
  }

  private boolean colorModelIsEqualTo(@NotNull RenderedImage actualImage) {
    return actualImage.getColorModel().equals(expectedImage.getColorModel());
  }

}
