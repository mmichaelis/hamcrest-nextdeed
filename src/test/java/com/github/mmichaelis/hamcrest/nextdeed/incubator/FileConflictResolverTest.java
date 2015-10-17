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

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;
import com.google.common.io.Files;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

/**
 * Tests {@link FileConflictResolver}.
 *
 * @since SINCE
 */
public class FileConflictResolverTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Function<File, File> conflictResolver;

  @Before
  public void setUp() throws Exception {
    conflictResolver = FileConflictResolver.defaultConflictResolver();
  }

  @Test
  public void resolveConflictingFilename() throws Exception {
    String fileName = "file1.txt";
    File file = temporaryFolder.newFile(fileName);
    Files.touch(file);
    File result = conflictResolver.apply(file);
    assertThat("Conflicting filename must have been resolved.", result, not(equalTo(file)));
  }

  @Test
  public void returnNullsUnhandled() throws Exception {
    assertThat("Should just return null on null input.",
               conflictResolver.apply(null),
               nullValue());

  }

  @Test
  public void hasToStringMethod() throws Exception {
    assertThat("Resolver has toString with relevant information.", conflictResolver,
               hasToString(Matchers.containsString("delegateResolver")));
  }
}
