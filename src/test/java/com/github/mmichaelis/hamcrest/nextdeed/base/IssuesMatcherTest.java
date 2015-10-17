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

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.Assert.assertThat;

import com.github.mmichaelis.hamcrest.nextdeed.ExceptionValidator;

import org.hamcrest.Matchers;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Tests {@link IssuesMatcher}
 *
 * @since SINCE
 */
public class IssuesMatcherTest {

  @Rule
  public TestName testName = new TestName();

  @Test
  public void passWithoutIssues() throws Exception {
    IssuesMatcherUnderTest<String> matcherUnderTest = new IssuesMatcherUnderTest<>();
    String probeString = testName.getMethodName();
    assertThat(probeString, matcherUnderTest);
  }

  @Test
  public void failForNullValue() throws Exception {
    IssuesMatcherUnderTest<String> matcherUnderTest = new IssuesMatcherUnderTest<>();
    assertThat(null, not(matcherUnderTest));
  }

  @Test
  public void canHaveCustomDescription() throws Exception {
    String descriptionPart1 = "Custom";
    String descriptionPart2 = "Description";
    final IssuesMatcherUnderTest<String> matcherUnderTest =
        new IssuesMatcherUnderTest<>("{0} {1}",
                                     descriptionPart1,
                                     descriptionPart2);
    String issueMessage = "some issue";
    matcherUnderTest.addIssue(issueMessage);
    final String probeString = testName.getMethodName();

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                assertThat(probeString, matcherUnderTest);
                return null;
              }
            },
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               stringContainsInOrder(
                   "Expected:",
                   descriptionPart1,
                   descriptionPart2,
                   "but:"
               )
    );

  }

  @Test
  public void failHavingAnIssue() throws Exception {
    final IssuesMatcherUnderTest<String> matcherUnderTest = new IssuesMatcherUnderTest<>();
    String issueMessage = "some issue";
    matcherUnderTest.addIssue(issueMessage);
    final String probeString = testName.getMethodName();

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                assertThat(probeString, matcherUnderTest);
                return null;
              }
            },
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               stringContainsInOrder(
                   "Expected:",
                   "has no issues",
                   "but:",
                   probeString,
                   issueMessage
               )
    );

  }

  @Test
  public void failHavingMultipleIssue() throws Exception {
    final IssuesMatcherUnderTest<String> matcherUnderTest = new IssuesMatcherUnderTest<>();
    String issueMessage1 = "some issue";
    String issueMessage2 = "some other issue";
    matcherUnderTest.addIssue(issueMessage1);
    matcherUnderTest.addIssue(issueMessage2);
    final String probeString = testName.getMethodName();

    Supplier<AssertionError> validator =
        new ExceptionValidator<>(
            new Callable<Void>() {
              @Override
              public Void call() throws Exception {
                assertThat(probeString, matcherUnderTest);
                return null;
              }
            },
            AssertionError.class
        );
    AssertionError e = validator.get();
    String errorMessage = e.getMessage();
    assertThat("Expected and actual should be contained in order.",
               errorMessage,
               Matchers.allOf(
                   stringContainsInOrder(
                       "Expected:",
                       "has no issues",
                       "but:",
                       probeString,
                       issueMessage1
                   ),
                   stringContainsInOrder(
                       "Expected:",
                       "has no issues",
                       "but:",
                       probeString,
                       issueMessage2
                   )
               )
    );
  }

  private static final class IssuesMatcherUnderTest<T> extends IssuesMatcher<T> {

    private final Collection<Issue> testIssues = new HashSet<>();

    public IssuesMatcherUnderTest() {
    }

    public IssuesMatcherUnderTest(@NotNull String description, Object... args) {
      super(description, args);
    }

    public void addIssue(String message) {
      testIssues.add(issue(message));
    }

    @Override
    protected void validate(@NotNull T item, @NotNull Collection<Issue> issues) {
      issues.addAll(testIssues);
    }
  }
}
