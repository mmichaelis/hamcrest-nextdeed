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

package com.github.mmichaelis.hamcrest.nextdeed.image.handler;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;
import com.github.mmichaelis.hamcrest.nextdeed.image.ImageException;
import com.github.mmichaelis.hamcrest.nextdeed.image.ImageType;
import com.github.mmichaelis.hamcrest.nextdeed.incubator.PathConflictResolver;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;

/**
 * @since SINCE
 */
public class WriteImageHandlerFunction implements BiFunction<ImageType, BufferedImage, String> {

  private static final Logger LOG = getLogger(WriteImageHandlerFunction.class);

  @NotNull
  private final Function<ImageType, Path> pathProvider;

  public WriteImageHandlerFunction() {
    this(new DefaultImagePathProvider());
  }

  public WriteImageHandlerFunction(@NotNull Function<ImageType, Path> pathProvider) {
    this.pathProvider = pathProvider;
  }

  @Override
  public String apply(ImageType imageType, BufferedImage bufferedImage) {
    Path outPath = getOutPath(imageType);
    String result = outPath.toAbsolutePath().toUri().toString();
    createParentDirs(outPath);
    if (!tryWriteImage(bufferedImage, outPath)) {
      result = format("<Failed writing image to to file %s>", result);
    }
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("pathProvider", pathProvider)
        .toString();
  }

  private static boolean tryWriteImage(BufferedImage bufferedImage, Path outPath) {
    boolean success = false;
    Iterator<ImageWriter> writerIterator = getImageWriterIteratorFor(outPath);
    while (writerIterator.hasNext() && !success) {
      ImageWriter next = writerIterator.next();
      success = tryWriteImage(bufferedImage, outPath, next, writerIterator.hasNext());
    }
    return success;
  }

  private static boolean tryWriteImage(BufferedImage bufferedImage,
                                Path outPath,
                                ImageWriter imageWriter,
                                boolean moreImageWritersAvailable) {
    boolean mySuccess = false;

    try (OutputStream os = java.nio.file.Files.newOutputStream(outPath);
         Closeable ios = ImageIO.createImageOutputStream(os)) {
      imageWriter.setOutput(ios);
      imageWriter.write(bufferedImage);
      LOG.debug("Wrote file {} containing image {} via image writer {}.",
                outPath.toUri(),
                bufferedImage,
                imageWriter);
      mySuccess = true;
    } catch (IOException e) {
      if (moreImageWritersAvailable) {
        LOG.warn(
            "Unable writing {} to contain image {} via image writer {}. Will try next image writer.",
            outPath.toUri(),
            bufferedImage,
            imageWriter,
            e);
      } else {
        LOG.error(
            "Could write {} to contain image {} with any image writer found. Last one tried: {}.",
            outPath.toUri(),
            bufferedImage,
            imageWriter,
            e);

      }
    }
    return mySuccess;
  }

  @NotNull
  private Path getOutPath(ImageType imageType) {
    Path providedFile = requireNonNull(pathProvider.apply(imageType),
                                       format("Path provided by %s must not be null.",
                                              pathProvider));
    Path outPath = PathConflictResolver.defaultConflictResolver().apply(providedFile);
    assert outPath != null : "Should never be null.";
    return outPath;
  }

  @NotNull
  private static Iterator<ImageWriter> getImageWriterIteratorFor(@NotNull Path outPath) {
    String outFileExtension = Files.getFileExtension(outPath.getFileName().toString());
    Iterator<ImageWriter> writerIterator = ImageIO.getImageWritersBySuffix(outFileExtension);
    if (!writerIterator.hasNext()) {
      LOG.debug("Don't know how to write files with suffix '{}'.", outFileExtension);
    }
    return writerIterator;
  }

  private static void createParentDirs(Path path) {
    try {
      java.nio.file.Files.createDirectories(path.getParent());
    } catch (IOException e) {
      throw new ImageException(
          format("Unable to create parent directories for path %s.",
                 path.toAbsolutePath()), e);
    }
  }

}
