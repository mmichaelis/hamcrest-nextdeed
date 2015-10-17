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

package com.github.mmichaelis.hamcrest.nextdeed.concurrent;

/**
 * Exception thrown when a wait operation times out.
 * In contrast to {@link java.util.concurrent.TimeoutException} this is an
 * unchecked exception.
 *
 * @since 1.0.0
 */
@SuppressWarnings("unused")
public class WaitTimeoutException extends RuntimeException {

  private static final long serialVersionUID = 575405375016958830L;

  public WaitTimeoutException() {
  }

  public WaitTimeoutException(String message) {
    super(message);
  }

  public WaitTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }

  public WaitTimeoutException(Throwable cause) {
    super(cause);
  }

  protected WaitTimeoutException(String message,
                                 Throwable cause,
                                 boolean enableSuppression,
                                 boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
