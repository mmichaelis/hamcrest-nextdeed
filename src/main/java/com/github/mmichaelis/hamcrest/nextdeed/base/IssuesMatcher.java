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

import com.google.common.base.MoreObjects;

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

  private final Collection<Issue> issues = new HashSet<>();
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
    Collection<Issue> newIssues = new HashSet<>();
    validate(item, newIssues);
    synchronized (issues) {
      issues.clear();
      issues.addAll(newIssues);
    }
    return issues.isEmpty();
  }

  @Override
  protected final void describeMismatchSafely(T item, Description mismatchDescription) {
    Collection<Issue> mismatchIssues;
    synchronized (issues) {
      mismatchIssues = new HashSet<>(issues);
      issues.clear();
    }
    mismatchIssues = possiblyRecalculateIssues(item, mismatchIssues);
    mismatchDescription
        .appendText("was ")
        .appendValue(item);
    if (mismatchIssues.size() == 1) {
      mismatchDescription
          .appendText(" with 1 issue: ")
          .appendText(mismatchIssues.iterator().next().getMessage());
    } else {
      mismatchDescription
          .appendText(" with ")
          .appendText(Integer.toString(mismatchIssues.size()))
          .appendText(" issues: ")
          .appendText(System.lineSeparator());
      for (Issue issue : mismatchIssues) {
        mismatchDescription.appendText("    * ")
            .appendText(issue.getMessage())
            .appendText(System.lineSeparator());
      }
    }
  }

  protected abstract void validate(@NotNull T item, @NotNull Collection<Issue> issues);

  private Collection<Issue> possiblyRecalculateIssues(@NotNull T item,
                                                      @NotNull Collection<Issue> mismatchIssues) {
    if (mismatchIssues.isEmpty()) {
      LOG.warn("Unexpected state: No issues were recorded, but describeMismatch has been called. "
               + "Re-triggering match for retrieving issues. This might produce unexpected "
               + "results if the component under test changed its state meanwhile.");
      validate(item, mismatchIssues);
      if (!mismatchIssues.isEmpty()) {
        LOG.warn(
            "Seems the state of the component under test has changed meanwhile. No issues were found.");
      }
    }
    return mismatchIssues;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("issues", issues)
        .add("message", message)
        .add("super", super.toString())
        .toString();
  }
}
