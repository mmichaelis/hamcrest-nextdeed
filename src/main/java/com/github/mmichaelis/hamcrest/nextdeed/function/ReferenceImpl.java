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

package com.github.mmichaelis.hamcrest.nextdeed.function;

import com.google.common.base.MoreObjects;

/**
 * Stores a value for later reference. Fails on {@link #get()} if
 * no value has been set before. The stored value might be {@code null}.
 *
 * @since SINCE
 */
public class ReferenceImpl<T> implements Reference<T> {

  private T value;
  private boolean set;

  /**
   * Retrieve the stored value.
   *
   * @return stored value; might be {@code null}
   * @throws IllegalStateException if value has not been set before
   */
  @Override
  public synchronized T get() {
    if (!set) {
      throw new IllegalStateException("Reference value not set.");
    }
    return value;
  }

  /**
   * Sets a value to store.
   *
   * @param value value to store; might be {@code null}
   * @return stored value, so that you might use it in a chained call
   */
  @Override
  public synchronized T set(T value) {
    set = true;
    this.value = value;
    return value;
  }

  /**
   * Validates if value is set.
   *
   * @return {@code true} if value is set, false {@code otherwise}
   */
  @Override
  public synchronized boolean isSet() {
    return set;
  }

  /**
   * Removes the stored value and sets the reference to unset state.
   *
   * @return previously stored value; might be {@code null}, especially if it has not been set
   * before
   */
  @Override
  public synchronized T remove() {
    T result = value;
    value = null;
    set = false;
    return result;
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("set", set)
        .add("value", value)
        .toString();
  }
}
