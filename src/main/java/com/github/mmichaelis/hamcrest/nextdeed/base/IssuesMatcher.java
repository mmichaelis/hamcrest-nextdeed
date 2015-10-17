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

import static org.slf4j.LoggerFactory.getLogger;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashSet;

/**
 * <p>
 * A matcher collecting multiple issues during the match and adding them to the
 * mismatch description on failure.
 * </p>
 * <dl>
 * <dt><strong>Discussion:</strong></dt>
 * <dd>
 * <p>
 * This is similar to {@link org.hamcrest.DiagnosingMatcher} with the difference that the match
 * is not calculated twice -- which might be wrong for integration tests, where between two
 * matches the state might change. Nevertheless it feels weird to use a ThreadLocal internally
 * to store the state and rely on {@code describeMismatch} being called for sure after the match
 * has been tried.
 * </p>
 * </dd>
 * </dl>
 *
 * @see org.hamcrest.DiagnosingMatcher
 * @since SINCE
 */
public abstract class IssuesMatcher<T> extends TypeSafeMatcher<T> {

  private static final Logger LOG = getLogger(IssuesMatcher.class);

  private static final ThreadLocal<Collection<Issue>> ISSUES_THREAD_LOCAL =
      new ThreadLocal<Collection<Issue>>() {
        @Override
        protected Collection<Issue> initialValue() {
          return new HashSet<>();
        }
      };
  private String message;

  protected IssuesMatcher() {
    this("has no issues");
  }

  protected IssuesMatcher(@NotNull String description, Object... args) {
    message = MessageFormat.format(description, args);
  }

  public static Issue issue(@NotNull String message, Object... args) {
    return new IssueImpl(MessageFormat.format(message, args));
  }

  @Override
  public final void describeTo(Description description) {
    description.appendText(message);
  }

  @Override
  protected final boolean matchesSafely(T item) {
    ISSUES_THREAD_LOCAL.remove();
    Collection<Issue> issues = ISSUES_THREAD_LOCAL.get();
    validate(item, issues);
    boolean issuesEmpty = issues.isEmpty();
    if (issuesEmpty) {
      ISSUES_THREAD_LOCAL.remove();
    }
    return issuesEmpty;
  }

  @Override
  protected final void describeMismatchSafely(T item, Description mismatchDescription) {
    Collection<Issue> issues = ISSUES_THREAD_LOCAL.get();
    issues = possiblyRecalculateIssues(item, issues);
    ISSUES_THREAD_LOCAL.remove();
    mismatchDescription
        .appendText("was ")
        .appendValue(item);
    if (issues.size() == 1) {
      mismatchDescription
          .appendText(" with 1 issue: ")
          .appendText(issues.iterator().next().getMessage());
    } else {
      mismatchDescription
          .appendText(" with ")
          .appendText(Integer.toString(issues.size()))
          .appendText(" issues: ")
          .appendText(System.lineSeparator());
      for (Issue issue : issues) {
        mismatchDescription.appendText("    * ")
            .appendText(issue.getMessage())
            .appendText(System.lineSeparator());
      }
    }
  }

  protected abstract void validate(@NotNull T item, @NotNull Collection<Issue> issues);

  private Collection<Issue> possiblyRecalculateIssues(T item,
                                                      Collection<Issue> issues) {
    if (issues.isEmpty()) {
      // As this should not happen, it is o. k. to have no code-coverage here.
      LOG.warn("Unexpected state: No issues were recorded, but describeMismatch has been called. "
               + "Re-triggering match for retrieving issues. This might produce unexpected "
               + "results if the component under test changed its state meanwhile.");
      if (matchesSafely(item)) {
        LOG.warn(
            "Seems the state of the component under test has changed meanwhile. No issues were found.");
      }
      issues = ISSUES_THREAD_LOCAL.get();
    }
    return issues;
  }

}
