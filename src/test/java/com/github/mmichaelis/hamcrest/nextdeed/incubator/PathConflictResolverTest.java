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

import static java.lang.String.format;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import com.github.mmichaelis.hamcrest.nextdeed.base.HamcrestNextdeedException;
import com.github.mmichaelis.hamcrest.nextdeed.incubator.PathConflictResolver;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

/**
 * Tests {@link PathConflictResolver}.
 *
 * @since SINCE
 */
@RunWith(Parameterized.class)
public class PathConflictResolverTest {

  private final String fsTypeName;
  private final Configuration fileSystemConfiguration;
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  private FileSystem fileSystem;
  private Function<Path, Path> conflictResolver;

  public PathConflictResolverTest(String fsTypeName, Configuration fileSystemConfiguration) {
    this.fsTypeName = fsTypeName;
    this.fileSystemConfiguration = fileSystemConfiguration;
  }

  @Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {"unix", Configuration.unix()},
        {"windows", Configuration.windows()},
        {"osX", Configuration.osX()}
    });
  }

  @Before
  public void setUp() throws Exception {
    fileSystem = Jimfs.newFileSystem(format("%s%d", fsTypeName, System.currentTimeMillis()),
                                     fileSystemConfiguration);
    conflictResolver = PathConflictResolver.defaultConflictResolver();
  }

  @After
  public void tearDown() throws Exception {
    fileSystem.close();
  }

  @Test
  public void answerNullWithNull() throws Exception {
    Path result = conflictResolver.apply(null);
    assertThat("For null input the answer should be null.", result, nullValue());
  }

  @Test
  public void resolveNonConflictingToInput() throws Exception {
    Path path = fileSystem.getPath("file1.txt");
    Path result = conflictResolver.apply(path);
    assertThat("If there is no conflict, the original path should be returned.",
               result,
               equalTo(path));
  }

  @Test
  public void resolveConflictOnce() throws Exception {
    String filename = "file1";
    String extension = "txt";
    Path path = fileSystem.getPath(format("%s.%s", filename, extension));
    Files.createFile(path);
    Path result = conflictResolver.apply(path);
    assertThat("On conflict an alternative path should have been suggested.",
               result,
               not(equalTo(path))
    );
    assert result != null;
    assertThat("Base filename and extension should be kept as is.",
               result.getFileName().toString(),
               allOf(
                   startsWith(filename),
                   endsWith(extension)
               )
    );
  }

  @Test
  public void resolveConflictNoExtension() throws Exception {
    String filename = "file1";
    Path path = fileSystem.getPath(filename);
    Files.createFile(path);
    Path result = conflictResolver.apply(path);
    assertThat("On conflict an alternative path should have been suggested.",
               result,
               not(equalTo(path))
    );
    assert result != null;
    assertThat(
        "Base filename should be kept as is and no empty extension should have been introduced.",
        result.getFileName().toString(),
        allOf(
            startsWith(filename),
            not(containsString("."))
        )
    );
  }

  @Test
  public void resolveConflictEmptyExtension() throws Exception {
    String filename = "file1";
    String extension = "";
    Path path = fileSystem.getPath(format("%s.%s", filename, extension));
    Files.createFile(path);
    Path result = conflictResolver.apply(path);
    assertThat("On conflict an alternative path should have been suggested.",
               result,
               not(equalTo(path))
    );
    assert result != null;
    assertThat(
        "Base filename should be kept as is and empty extension should have been kept as is.",
        result.getFileName().toString(),
        allOf(
            startsWith(filename),
            endsWith(".")
        )
    );
  }

  @Test
  public void resolveConflictDotFilename() throws Exception {
    String filename = ".file1";
    Path path = fileSystem.getPath(".file1");
    Files.createFile(path);
    Path result = conflictResolver.apply(path);
    assertThat("On conflict an alternative path should have been suggested.",
               result,
               not(equalTo(path)));
    assert result != null;
    assertThat("Base filename and extension should be kept as is.",
               result.getFileName().toString(),
               startsWith(filename)
    );
  }

  @Test
  public void resolveConflictTwice() throws Exception {
    Path path = fileSystem.getPath("file1.txt");
    Files.createFile(path);
    Path result1 = conflictResolver.apply(path);
    assert result1 != null;
    Files.createFile(result1);
    Path result2 = conflictResolver.apply(path);
    assertThat("On re-occurring conflict an alternative path should have been suggested.",
               result2, not(equalTo(result1)));
    assertThat("On re-occurring conflict an alternative path should have been suggested.",
               result2, not(equalTo(path)));
  }

  @Test
  public void failOnLimitReached_noTolerance() throws Exception {
    Function<Path, Path> noToleranceResolver = new PathConflictResolver(0);
    Path path = fileSystem.getPath("file1.txt");
    Files.createFile(path);
    expectedException.expect(HamcrestNextdeedException.class);
    expectedException.expectMessage(path.toAbsolutePath().toString());
    noToleranceResolver.apply(path);
  }

  @Test
  public void toStringProvidesInformation() throws Exception {
    PathConflictResolver resolver = new PathConflictResolver(1331);
    assertThat(resolver, Matchers.hasToString(Matchers.containsString(Integer.toString(1331))));
  }
}
