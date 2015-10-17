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

package com.github.mmichaelis.hamcrest.nextdeed.util;

import static com.github.mmichaelis.hamcrest.nextdeed.util.Messages.messages;

import com.google.common.base.MoreObjects;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;

import java.util.ResourceBundle;

/**
 * Validates that a resource bundle provides the given key.
 *
 * @since SINCE
 */
public class ResourceBundleContainsKey extends CustomTypeSafeMatcher<ResourceBundle> {

  @NotNull
  private final String expectedKey;

  public ResourceBundleContainsKey(@NotNull String expectedKey) {
    super(messages().resourceBundleContainsKey(expectedKey));
    this.expectedKey = expectedKey;
  }

  /**
   * Validates that a resource bundle contains the given key.
   *
   * @param expectedKey expected key
   * @return matcher
   */
  @NotNull
  public static Matcher<ResourceBundle> resourceBundleContainsKey(@NotNull String expectedKey) {
    return new ResourceBundleContainsKey(expectedKey);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("expectedKey", expectedKey)
        .add("super", super.toString())
        .toString();
  }

  @Override
  protected boolean matchesSafely(ResourceBundle item) {
    return item.containsKey(expectedKey);
  }
}
