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

package com.github.mmichaelis.hamcrest.nextdeed.config;

import com.github.mmichaelis.hamcrest.nextdeed.base.HamcrestNextdeedException;

/**
 * Exception during <em>Hamcrest &mdash; Next Deed</em> configuration.
 *
 * @since SINCE
 */
@SuppressWarnings("unused")
public class ConfigurationException extends HamcrestNextdeedException {

  private static final long serialVersionUID = -7236851784748043801L;

  public ConfigurationException() {
    // Default constructor
  }

  public ConfigurationException(String message) {
    super(message);
  }

  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public ConfigurationException(Throwable cause) {
    super(cause);
  }

  protected ConfigurationException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
