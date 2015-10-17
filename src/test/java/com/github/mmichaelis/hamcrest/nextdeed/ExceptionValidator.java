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

import static java.lang.String.format;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.concurrent.Callable;

/**
 * Validates assertion/assumption errors which cannot be tested with
 * {@link org.junit.rules.ExpectedException}.
 *
 * @since SINCE
 */
public class ExceptionValidator<T extends Throwable> implements Supplier<T> {

  private static final Logger LOG = getLogger(ExceptionValidator.class);

  @NotNull
  private final Callable<Void> exceptionRaiser;
  private final Class<?> expectedExceptionType;

  public ExceptionValidator(@NotNull Callable<Void> exceptionRaiser,
                            @NotNull Class<T> expectedExceptionType) {
    this.exceptionRaiser = exceptionRaiser;
    this.expectedExceptionType = expectedExceptionType;
  }

  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    T result = null;
    boolean exceptionRaised = true;
    try {
      exceptionRaiser.call();
      exceptionRaised = false;
    } catch (Throwable th) {
      LOG.debug("Caught exception of type {} expecting exception of type {}.", th.getClass(),
                expectedExceptionType, th);
      assertThat("Is not expected exception type.", th, Matchers.instanceOf(expectedExceptionType));
      result = (T) th;
    }
    if (!exceptionRaised) {
      fail(format("Expected to raise an exception of type %s but no exception was thrown.",
                  expectedExceptionType));
    }
    return result;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("exceptionRaiser", exceptionRaiser)
        .toString();
  }
}
