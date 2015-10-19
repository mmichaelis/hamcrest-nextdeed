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

import static com.github.mmichaelis.hamcrest.nextdeed.image.Messages.messages;
import static com.github.mmichaelis.hamcrest.nextdeed.image.internal.Helper.imageTypeString;

import com.google.common.base.Supplier;

import com.github.mmichaelis.hamcrest.nextdeed.base.Issue;
import com.github.mmichaelis.hamcrest.nextdeed.base.IssuesMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.DefaultSampleComparisonProcessor;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.PixelCountingSampleProcessingListener;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.SampleComparisonProcessor;
import com.github.mmichaelis.hamcrest.nextdeed.image.internal.SampleProcessingCompositeContext;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.util.Collection;

/**
 * Compares two images if they are equal.
 *
 * @since SINCE
 */
public class ImageIsEqual extends IssuesMatcher<BufferedImage> {

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
  public ImageIsEqual(@NotNull final BufferedImage expectedImage,
                      @Nullable final BiFunction<ImageType, BufferedImage, String> imageHandlerFunction) {
    super(new Supplier<String>() {
      @Override
      public String get() {
        if (imageHandlerFunction == null) {
          return String.valueOf(expectedImage);
        }
        return imageHandlerFunction.apply(ImageType.EXPECTED, expectedImage);
      }
    });
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
  protected void describeMismatchedItem(@NotNull BufferedImage item,
                                        @NotNull Description mismatchDescription) {
    if (imageHandlerFunction == null) {
      super.describeMismatchedItem(item, mismatchDescription);
    } else {
      mismatchDescription.appendText(imageHandlerFunction.apply(ImageType.ACTUAL, item));
    }
  }

  @Override
  protected void validate(@NotNull BufferedImage actualImage, @NotNull Collection<Issue> issues) {
    if (actualImage == expectedImage) {
      return;
    }
    boolean comparable = validateComparable(actualImage, issues);
    if (comparable) {
      validateImage(actualImage, issues);
    }
  }

  private boolean validateComparable(@NotNull BufferedImage actualImage,
                                     @NotNull Collection<Issue> issues) {
    return validateWidth(actualImage, issues) &&
           validateHeight(actualImage, issues) &&
           validateType(actualImage, issues) &&
           validateColorModel(actualImage, issues);
  }

  private boolean validateColorModel(@NotNull RenderedImage actualImage,
                                     @NotNull Collection<Issue> issues) {
    ColorModel actualImageColorModel = actualImage.getColorModel();
    ColorModel expectedImageColorModel = expectedImage.getColorModel();
    boolean comparable = actualImageColorModel.equals(expectedImageColorModel);
    if (!comparable) {
      issues.add(issue(messages().colorModelDiffers(expectedImageColorModel,
                                                    actualImageColorModel)));
    }
    return comparable;
  }

  private boolean validateType(@NotNull BufferedImage actualImage,
                               @NotNull Collection<Issue> issues) {
    int actualImageType = actualImage.getType();
    int expectedImageType = expectedImage.getType();
    boolean comparable = actualImageType == expectedImageType;
    if (!comparable) {
      issues.add(issue(messages().typeDiffers(imageTypeString(expectedImage),
                                              imageTypeString(actualImage))));
    }
    return comparable;
  }

  private boolean validateHeight(@NotNull RenderedImage actualImage,
                                 @NotNull Collection<Issue> issues) {
    int actualImageHeight = actualImage.getHeight();
    int expectedImageHeight = expectedImage.getHeight();
    boolean comparable = actualImageHeight == expectedImageHeight;
    if (!comparable) {
      issues
          .add(issue(messages().heightDiffers(expectedImageHeight, actualImageHeight)));
    }
    return comparable;
  }

  private boolean validateWidth(@NotNull RenderedImage actualImage,
                                @NotNull Collection<Issue> issues) {
    int actualImageWidth = actualImage.getWidth();
    int expectedImageWidth = expectedImage.getWidth();
    boolean comparable = actualImageWidth == expectedImageWidth;
    if (!comparable) {
      issues.add(issue(messages().widthDiffers(expectedImageWidth, actualImageWidth)));
    }
    return comparable;
  }

  private void validateImage(@NotNull BufferedImage actualImage, @NotNull Collection<Issue> issues) {
    final BufferedImage diffImage = createDiffImageTarget(actualImage);

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

    final long differentPixels = pixelProcessingListener.getDifferent();
    if (differentPixels != 0) {
      issues.add(issue(new Supplier<String>() {
        @Override
        public String get() {
          if (imageHandlerFunction == null) {
            return messages().imageDiffers(differentPixels, 0, null);
          } else {
            return messages().imageDiffers(differentPixels,
                                           1,
                                           imageHandlerFunction.apply(ImageType.DIFFERENCE, diffImage));
          }
        }
      }));
    }
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

}
