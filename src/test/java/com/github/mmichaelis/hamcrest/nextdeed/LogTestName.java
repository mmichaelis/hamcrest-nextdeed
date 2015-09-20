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

package com.github.mmichaelis.hamcrest.nextdeed;

import static com.github.mmichaelis.hamcrest.nextdeed.TestMarker.TEST;
import static org.slf4j.LoggerFactory.getLogger;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;

/**
 * Logs the test name and specifies an indent for subsequent log output.
 *
 * @since SINCE
 */
public class LogTestName extends TestWatcher {

  private static final Logger LOG = getLogger(LogTestName.class);
  private static final String LOG_INDENT = "log.indent";

  @Override
  protected void starting(Description description) {
    LOG.info(TEST, "Starting {}", description.getMethodName());
    System.setProperty(LOG_INDENT, "    ");
  }

  @Override
  protected void finished(Description description) {
    System.setProperty(LOG_INDENT, "");
    LOG.info(TEST, "Finished {}", description.getMethodName());
  }
}
