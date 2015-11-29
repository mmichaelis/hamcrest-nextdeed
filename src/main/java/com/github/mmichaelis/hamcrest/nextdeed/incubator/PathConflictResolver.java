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

package com.github.mmichaelis.hamcrest.nextdeed.incubator;

import com.github.mmichaelis.hamcrest.nextdeed.config.ConfigurationException;
import com.google.common.base.Function;
import com.google.common.io.Files;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

import static com.google.common.base.MoreObjects.toStringHelper;
import static java.lang.String.format;
import static java.nio.file.Files.exists;

/**
 * Resolve conflicts in case of duplicate file names by appending increasing numbers to the
 * file.
 *
 * @since SINCE
 */
public class PathConflictResolver implements Function<Path, Path> {

  private static final int DEFAULT_LIMIT = 1000;
  private static final Function<Path, Path> DEFAULT_INSTANCE = new PathConflictResolver();
  private final long limit;

  public PathConflictResolver() {
    this(DEFAULT_LIMIT);
  }

  public PathConflictResolver(long limit) {
    this.limit = limit;
  }

  @NotNull
  public static Function<Path, Path> defaultConflictResolver() {
    return DEFAULT_INSTANCE;
  }

  /**
   * Provides a non-conflicting path.
   *
   * @param input path to possibly resolve the conflict for
   * @return the original path, a non-conflicting alternative or null, iff. the input is null
   * @throws ConfigurationException if within a given range no non-conflicting file name could
   *                                be found.
   */
  @Override
  @Contract("null -> null; !null -> !null")
  public Path apply(@Nullable Path input) {
    if ((input == null) || !exists(input)) {
      return input;
    }

    Path filenamePath = input.getFileName();
    String completeFileName = filenamePath.toString();

    boolean hasDotPrefix = false;

    if (completeFileName.startsWith(".")) {
      hasDotPrefix = true;
      completeFileName = completeFileName.substring(1);
    }
    Path probe = getNonConflictingPath(input, completeFileName, hasDotPrefix);
    if (probe == null) {
      throw new ConfigurationException(
          format("Unable to resolve conflict for %s: Too many conflicting paths.",
              input.toAbsolutePath()));
    }
    return probe;
  }

  @Nullable
  private Path getNonConflictingPath(@NotNull Path input, @NotNull String completeFileName, boolean hasDotPrefix) {
    String fileName = Files.getNameWithoutExtension(completeFileName);
    String fileExtension = Files.getFileExtension(completeFileName);
    boolean hasDotExtension = !fileExtension.isEmpty() || completeFileName.contains(".");
    for (int i = 0; i < limit; i++) {
      String
          fileNameProbe =
          format("%s%s (%d)%s%s", hasDotPrefix ? "." : "", fileName, i + 1,
              hasDotExtension ? "." : "", fileExtension);
      Path probe = input.resolveSibling(fileNameProbe);
      if (!exists(probe)) {
        return probe;
      }
    }
    return null;
  }


  @Override
  @NotNull
  public String toString() {
    return toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("limit", limit)
        .toString();
  }
}
