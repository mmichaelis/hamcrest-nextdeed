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

import static com.github.mmichaelis.hamcrest.nextdeed.base.MessagesProxyProvider.withRawMessages;
import static com.github.mmichaelis.hamcrest.nextdeed.config.NextDeedConfiguration.propertyName;
import static com.github.mmichaelis.hamcrest.nextdeed.image.ImageIsEqual.imageEqualTo;
import static com.github.mmichaelis.hamcrest.nextdeed.image.handler.DefaultImagePathProvider.IMAGE_OUT_FILE_PATTERN;
import static com.github.mmichaelis.hamcrest.nextdeed.incubator.SystemPropertyChanger.SYSTEM_PROPERTY_CHANGER;
import static com.google.common.jimfs.Configuration.unix;
import static com.google.common.jimfs.Jimfs.newFileSystem;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.newDirectoryStream;
import static java.nio.file.Files.size;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Supplier;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;
import com.github.mmichaelis.hamcrest.nextdeed.config.PropagatedTestDetails;
import com.github.mmichaelis.hamcrest.nextdeed.image.handler.WriteImageHandlerFunction;
import com.github.mmichaelis.hamcrest.nextdeed.incubator.SystemPropertyChanger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.slf4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;


/**
 * Tests {@link ImageIsEqual}.
 *
 * @since SINCE
 */
public class ImageIsEqualTest {

  private static final Logger LOG = getLogger(ImageIsEqualTest.class);

  private static final int SIZE = 8;
  private static final int TILE_SIZE = 2;
  private static FileSystem fileSystem;
  @Rule
  public PropagatedTestDetails propagatedTestDetails = new PropagatedTestDetails();
  /*
  Idea: Add to test lifecycle to listen to property changes.
   */
  @Rule
  public SystemPropertyChanger systemPropertyChanger = SYSTEM_PROPERTY_CHANGER;
  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();
  private BufferedImage redWhiteImage;
  private BufferedImage anotherRedWhiteImage;
  private BufferedImage greenWhiteImage;
  private BufferedImage bigWidthImage;
  private BufferedImage bigHeightImage;
  private Path imageOutPath;

  @BeforeClass
  public static void setUpFileSystem() throws IOException {
    fileSystem = newFileSystem(unix());
    LOG.debug("Set up file system {}.", fileSystem);
  }

  @AfterClass
  public static void tearDownFileSystem() throws IOException, InterruptedException {
    fileSystem.close();
    LOG.debug("Closed file system {}.", fileSystem);
  }

  @Before
  public void setUp() throws Exception {
    imageOutPath =
        fileSystem.getPath(propagatedTestDetails.getMethodName()).toAbsolutePath();
    createDirectories(imageOutPath);

    propagatedTestDetails.setProperty(propertyName(IMAGE_OUT_FILE_PATTERN),
                                      imageOutPath.toUri() + "/${testTimestamp}_${testFullNameEncoded}_${imageType}.png");

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
    bigWidthImage = new CheckersImageBuilderImpl()
        .width(SIZE * 2)
        .height(SIZE)
        .build();
    bigHeightImage = new CheckersImageBuilderImpl()
        .width(SIZE)
        .height(SIZE * 2)
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
  public void failureReportForDifferentImagesWithoutImageHandler() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ImageMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(redWhiteImage, imageEqualTo(greenWhiteImage));
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               allOf(
                   stringContainsInOrder(
                       "Expected:",
                       String.valueOf(greenWhiteImage),
                       "but:"
                   ),
                   stringContainsInOrder(
                       "but:",
                       String.valueOf(redWhiteImage),
                       "imageDiffers",
                       "32"
                   )
               )
    );
  }

  @Test
  public void expectedActualDifferenceImageWrittenOnComparisonFailure() throws Exception {
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ImageMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(redWhiteImage,
                               imageEqualTo(greenWhiteImage, new WriteImageHandlerFunction()));
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    Collection<Path> imageFilePaths = new ArrayList<>(3);
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(imageOutPath)) {
      for (Path path : directoryStream) {
        imageFilePaths.add(path);
        errorCollector.checkThat(
            "File should not be empty: " + path.toAbsolutePath().toUri(),
            size(path), greaterThan(0L));
      }
    }
    errorCollector.checkThat(format(
        "Expected, Actual, Difference: 3 Images should have been written to directory %s.",
        imageOutPath.toAbsolutePath()),
                             imageFilePaths,
                             hasSize(3));
    String errorMessage = e.getMessage();
    errorCollector.checkThat(
        "Expected and actual should be contained in order.",
        errorMessage,
        allOf(
            stringContainsInOrder(
                "Expected:",
                "EXPECTED.png",
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "ACTUAL.png",
                "imageDiffers",
                "32",
                "DIFFERENCE.png"
            )
        )
    );
  }

  @Test
  public void expectedActualImageWrittenOnWidthPreconditionFailure() throws Exception {
    final BufferedImage actualImage = bigWidthImage;
    final BufferedImage expectedImage = greenWhiteImage;
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ImageMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(actualImage,
                               imageEqualTo(expectedImage, new WriteImageHandlerFunction()));
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    Collection<Path> imageFilePaths = new ArrayList<>(3);
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(imageOutPath)) {
      for (Path path : directoryStream) {
        imageFilePaths.add(path);
        errorCollector.checkThat(
            "File should not be empty: " + path.toAbsolutePath().toUri(),
            size(path), greaterThan(0L));
      }
    }
    errorCollector
        .checkThat(format(
            "Expected, Actual (but not: Difference): 2 Images should have been written to %s.",
            imageOutPath.toAbsolutePath().toUri()),
                   imageFilePaths,
                   hasSize(2));
    String errorMessage = e.getMessage();
    errorCollector.checkThat(
        "Expected and actual should be contained in order.",
        errorMessage,
        allOf(
            stringContainsInOrder(
                "Expected:",
                "EXPECTED.png",
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "ACTUAL.png",
                "widthDiffers",
                Integer.toString(expectedImage.getWidth()),
                Integer.toString(actualImage.getWidth())
            )
        )
    );
  }

  @Test
  public void expectedActualImageWrittenOnHeightPreconditionFailure() throws Exception {
    final BufferedImage actualImage = bigHeightImage;
    final BufferedImage expectedImage = greenWhiteImage;
    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            withRawMessages(
                ImageMessages.class,
                new Callable<Void>() {
                  @Override
                  public Void call() throws Exception {
                    assertThat(actualImage,
                               imageEqualTo(expectedImage, new WriteImageHandlerFunction()));
                    return null;
                  }
                }),
            AssertionError.class
        );
    AssertionError e = validator.get();
    Collection<Path> imageFilePaths = new ArrayList<>(3);
    try (DirectoryStream<Path> directoryStream = newDirectoryStream(imageOutPath)) {
      for (Path path : directoryStream) {
        imageFilePaths.add(path);
        errorCollector.checkThat(
            "File should not be empty: " + path.toAbsolutePath().toUri(),
            size(path), greaterThan(0L));
      }
    }
    errorCollector
        .checkThat(format(
            "Expected, Actual (but not: Difference): 2 Images should have been written to %s.",
            imageOutPath.toAbsolutePath().toUri()),
                   imageFilePaths,
                   hasSize(2));
    String errorMessage = e.getMessage();
    errorCollector.checkThat(
        "Expected and actual should be contained in order.",
        errorMessage,
        allOf(
            stringContainsInOrder(
                "Expected:",
                "EXPECTED.png",
                "but:"
            ),
            stringContainsInOrder(
                "but:",
                "ACTUAL.png",
                "heightDiffers",
                Integer.toString(expectedImage.getWidth()),
                Integer.toString(actualImage.getWidth())
            )
        )
    );
  }

}
