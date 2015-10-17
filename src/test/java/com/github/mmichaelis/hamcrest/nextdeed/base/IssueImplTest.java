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

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * Tests {@link IssueImpl}.
 *
 * @since SINCE
 */
public class IssueImplTest {

  @Test
  public void messageStoredCorrectly() throws Exception {
    String message = "some message";
    Issue issue = new IssueImpl(message);
    assertThat("Message correctly set.", issue.getMessage(), equalTo(message));
  }

  @Test
  public void equalsHashCodeBehaveCorrectly() throws Exception {
    String message1 = "some first message";
    String message2 = "some second message";
    IssueImpl issue1a = new IssueImpl(message1);
    IssueImpl issue1b = new IssueImpl(message1);
    IssueImpl issue2 = new IssueImpl(message2);
    assertThat("Issue1 is equal to itself.", issue1a, equalTo(issue1a));
    assertThat("Issue1 is equal to issue with same message.", issue1a, equalTo(issue1b));
    assertThat("Issue1a and Issue1b share the same hash code.", issue1a.hashCode(),
               equalTo(issue1b.hashCode()));
    assertThat("Issue1 is not equal to issue with different message.", issue1a,
               not(equalTo(issue2)));
  }

  @Test
  public void messageContainedInToString() throws Exception {
    String message = "some message";
    Issue issue = new IssueImpl(message);
    assertThat("toString() contains message.",
               issue,
               hasToString(containsString(message)));
  }
}
