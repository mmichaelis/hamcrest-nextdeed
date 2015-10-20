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

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.declaresMethodWithName;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.resourceBundleContainsKey;
import static java.lang.String.format;

import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * Base test case testing message bundles and related interfaces.
 *
 * @since SINCE
 */
public abstract class MessagesTestCase {

  @NotNull
  private final Class<?> messageInterface;
  private final ResourceBundle bundle;

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  protected MessagesTestCase(@NotNull Class<?> messageInterface) {
    this.messageInterface = messageInterface;
    bundle = MessagesProxyProvider.getBundle(messageInterface);
  }

  @Test
  public void interfaceMessagesAreBackedByProperties() throws Exception {
    Method[] declaredMethods = messageInterface.getDeclaredMethods();

    for (Method declaredMethod : declaredMethods) {
      String methodName = declaredMethod.getName();
      errorCollector.checkThat(
          format("Interface method '%s' should be backed by property of same name in %s.",
                 methodName,
                 bundle),
          bundle,
          resourceBundleContainsKey(methodName));
    }
  }

  @Test
  public void resourceBundlePropertiesBackedByInterfaceMethod() throws Exception {
    Enumeration<String> keys = bundle.getKeys();
    while (keys.hasMoreElements()) {
      String key = keys.nextElement();
      errorCollector.checkThat(
          format("Key '%s' should be backed by interface method in %s.", key, messageInterface),
          messageInterface,
          declaresMethodWithName(key));
    }

  }
}
