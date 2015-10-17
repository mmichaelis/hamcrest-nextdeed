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

import static java.util.Objects.requireNonNull;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;

import com.github.mmichaelis.hamcrest.nextdeed.base.HamcrestNextdeedException;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;

/**
 * Resolve conflicts in case of duplicate file names by appending increasing numbers to the
 * file.
 *
 * @since SINCE
 */
public class FileConflictResolver implements Function<File, File> {

  private static final int DEFAULT_LIMIT = 1000;
  private static final Function<File, File> defaultInstance = new FileConflictResolver();

  @NotNull
  private final Function<Path, Path> delegateResolver;

  public FileConflictResolver() {
    this(DEFAULT_LIMIT);
  }

  public FileConflictResolver(long limit) {
    delegateResolver = new PathConflictResolver(limit);
  }

  @NotNull
  public static Function<File, File> defaultConflictResolver() {
    return defaultInstance;
  }

  /**
   * Provides a non-conflicting file.
   *
   * @param input file to possibly resolve the conflict for
   * @return the original file, a non-conflicting alternative or null, iff. the input is null
   * @throws HamcrestNextdeedException if within a given range no non-conflicting file name could
   *                                   be found.
   */
  @Override
  @Contract("null -> null; !null -> !null")
  public File apply(@Nullable File input) {
    if (input == null) {
      return input;
    }
    Path delegateResult = requireNonNull(delegateResolver.apply(input.toPath()),
                                         "Unexpected: Should not have received null from delegate path resolver.");
    return delegateResult.toFile();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("delegateResolver", delegateResolver)
        .toString();
  }
}
