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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.io.Files;

import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;
import com.github.mmichaelis.hamcrest.nextdeed.incubator.FileConflictResolver;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

/**
 * @since SINCE
 */
public class WriteImageHandlerFunction implements BiFunction<ImageType, BufferedImage, String> {

  private static final Logger LOG = getLogger(WriteImageHandlerFunction.class);

  @NotNull
  private final Function<ImageType, File> fileProvider;

  public WriteImageHandlerFunction() {
    this(new DefaultImageFileProvider());
  }

  public WriteImageHandlerFunction(@NotNull Function<ImageType, File> fileProvider) {
    this.fileProvider = fileProvider;
  }

  @Override
  public String apply(ImageType imageType, BufferedImage bufferedImage) {
    File outFile = getOutFile(imageType);
    String result = outFile.getAbsolutePath();
    createParentDirs(outFile);
    if (!tryWriteImage(bufferedImage, outFile)) {
      result = format("<Failed writing image to to file %s>", result);
    }
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("fileProvider", fileProvider)
        .toString();
  }

  private boolean tryWriteImage(BufferedImage bufferedImage, File outFile) {
    boolean success = false;
    Iterator<ImageWriter> writerIterator = getImageWriterIteratorFor(outFile);
    while (writerIterator.hasNext() && !success) {
      ImageWriter next = writerIterator.next();
      success = tryWriteImage(bufferedImage, outFile, next, writerIterator.hasNext());
    }
    return success;
  }

  private boolean tryWriteImage(BufferedImage bufferedImage,
                                File outFile,
                                ImageWriter imageWriter,
                                boolean moreImageWritersAvailable) {
    boolean mySuccess = false;
    try (Closeable ios = new FileImageOutputStream(outFile)) {
      imageWriter.setOutput(ios);
      imageWriter.write(bufferedImage);
      LOG.info("Wrote file {} containing image {} via image writer {}.", outFile, bufferedImage,
               imageWriter);
      mySuccess = true;
    } catch (IOException e) {
      if (moreImageWritersAvailable) {
        LOG.warn(
            "Unable writing {} to contain image {} via image writer {}. Will try next image writer.",
            outFile, bufferedImage, imageWriter, e);
      } else {
        LOG.error(
            "Could write {} to contain image {} with any image writer found. Last one tried: {}.",
            outFile, bufferedImage, imageWriter, e);

      }
    }
    return mySuccess;
  }

  @NotNull
  private File getOutFile(ImageType imageType) {
    File providedFile = requireNonNull(fileProvider.apply(imageType),
                                       format("File provided by %s must not be null.",
                                              fileProvider));
    File outFile = FileConflictResolver.defaultConflictResolver().apply(providedFile);
    assert outFile != null : "Should never be null.";
    return outFile;
  }

  @NotNull
  private Iterator<ImageWriter> getImageWriterIteratorFor(@NotNull File outFile) {
    String outFileExtension = Files.getFileExtension(outFile.getName());
    Iterator<ImageWriter> writerIterator = ImageIO.getImageWritersBySuffix(outFileExtension);
    if (!writerIterator.hasNext()) {
      LOG.debug("Don't know how to write files with suffix '{}'.", outFileExtension);
    }
    return writerIterator;
  }

  private void createParentDirs(File outFile) {
    try {
      Files.createParentDirs(outFile);
    } catch (IOException e) {
      throw new ImageException(
          format("Unable to create parent directories for file %s.",
                 outFile.getAbsolutePath()), e);
    }
  }

}
