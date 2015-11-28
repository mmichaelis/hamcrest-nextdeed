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

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.MDC;

/**
 * @since SINCE
 */
public class MdcAccess extends TestWatcher {

  private static final String SUPRESSION_KEY = "suppression";
  private static final String SUPPRESSED_VALUE = "suppressed";

  @Override
  protected void finished(Description description) {
    MDC.clear();
  }

  public void startSuppressLogging() {
    MDC.put(SUPRESSION_KEY, SUPPRESSED_VALUE);
  }

  public void endSuppressLogging() {
    MDC.remove(SUPRESSION_KEY);
  }
}
