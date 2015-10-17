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

package com.github.mmichaelis.hamcrest.nextdeed.io;

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.classModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.declaresNoArgumentsConstructor;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.isInstantiableWithNoArguments;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.memberModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.io.Messages.messages;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertThat;

import com.github.mmichaelis.hamcrest.nextdeed.base.MessagesTestCase;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.Modifier;

/**
 * Tests {@link IoMessages}.
 *
 * @since SINCE
 */
public class IoMessagesTest extends MessagesTestCase {

  public IoMessagesTest() {
    super(IoMessages.class);
  }

  @Test
  public void messagesProvidesMessageInstance() throws Exception {
    assertThat(messages(), Matchers.instanceOf(IoMessages.class));
  }

  @Test
  public void messagesProviderIsUtilityClass() throws Exception {
    assertThat(Messages.class,
               allOf(
                   declaresNoArgumentsConstructor(),
                   classModifierContains(Modifier.FINAL),
                   isInstantiableWithNoArguments()
               )
    );

    assertThat("Any constructors must be private.",
               asList(Messages.class.getDeclaredConstructors()),
               everyItem(memberModifierContains(Modifier.PRIVATE)));
  }

}
