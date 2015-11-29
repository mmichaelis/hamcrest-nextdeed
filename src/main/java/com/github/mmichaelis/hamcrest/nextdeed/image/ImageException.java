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

import com.github.mmichaelis.hamcrest.nextdeed.base.HamcrestNextdeedException;

/**
 * Exception during image processing.
 *
 * @since SINCE
 */
public class ImageException extends HamcrestNextdeedException {

  private static final long serialVersionUID = -5411395398475572152L;

  public ImageException() {
    // Default constructor
  }

  public ImageException(String message) {
    super(message);
  }

  public ImageException(String message, Throwable cause) {
    super(message, cause);
  }

  public ImageException(Throwable cause) {
    super(cause);
  }

  protected ImageException(String message, Throwable cause, boolean enableSuppression,
                           boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
