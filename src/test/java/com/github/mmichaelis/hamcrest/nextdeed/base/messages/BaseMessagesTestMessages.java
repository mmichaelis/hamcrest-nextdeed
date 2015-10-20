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

package com.github.mmichaelis.hamcrest.nextdeed.base.messages;

import com.github.mmichaelis.hamcrest.nextdeed.base.MessagesProxyProviderTest;

import org.jetbrains.annotations.NotNull;

/**
 * Support for {@link MessagesProxyProviderTest}.
 *
 * @since SINCE
 */
public interface BaseMessagesTestMessages {

  @NotNull
  String undefinedBundleProperty();

  @NotNull
  String backedBundleProperty();

  @NotNull
  String withArgumentProperty(@NotNull String theArgument);

  @NotNull
  String invalidMessagePattern(@NotNull String theArgument);

  @NotNull
  String withIntegerArgumentProperty(@NotNull String theArgument);
}
