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

package com.github.mmichaelis.hamcrest.nextdeed.base;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.jetbrains.annotations.NotNull;

/**
 * Implementation for {@link Issue}.
 *
 * @since SINCE
 */
final class IssueImpl implements Issue {

  @NotNull
  private final Supplier<String> messageSupplier;

  IssueImpl(@NotNull String message) {
    messageSupplier = Suppliers.ofInstance(message);
  }

  IssueImpl(@NotNull Supplier<String> messageSupplier) {
    this.messageSupplier = messageSupplier;
  }

  @NotNull
  @Override
  public String getMessage() {
    return messageSupplier.get();
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("messageSupplier", messageSupplier)
        .toString();
  }
}
