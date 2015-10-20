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

import static com.github.mmichaelis.hamcrest.nextdeed.base.Messages.messages;

import com.google.common.base.MoreObjects;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

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
 * is not calculated twice. This behavior of the {@code DiagnosingMatcher} might be wrong for
 * integration tests, where between two matches the state might change.
 * </p>
 * </dd>
 * </dl>
 *
 * @see org.hamcrest.DiagnosingMatcher
 * @since SINCE
 */
public abstract class IssuesMatcher<T> extends TypeSafeMatcher<T> {

  private final Collection<Issue> issues = new ArrayList<>();
  private final Supplier<String> messageSupplier;

  /**
   * Constructor with default expectation message.
   */
  protected IssuesMatcher() {
    this(Suppliers.ofInstance(messages().hasNoIssues()));
  }

  protected IssuesMatcher(@NotNull final String description, final Object... args) {
    this(new Supplier<String>() {
      @Override
      public String get() {
        return MessageFormat.format(description, args);
      }
    });
  }

  protected IssuesMatcher(@NotNull Supplier<String> messageSupplier) {
    this.messageSupplier = messageSupplier;
  }

  public static Issue issue(@NotNull final String message, final Object... args) {
    return issue(new Supplier<String>() {
      @Override
      public String get() {
        return MessageFormat.format(message, args);
      }
    });
  }

  public static Issue issue(@NotNull Supplier<String> messageSupplier) {
    return new IssueImpl(messageSupplier);
  }

  @Override
  public final void describeTo(Description description) {
    description.appendText(messageSupplier.get());
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("issues", issues)
        .add("messageSupplier", messageSupplier)
        .add("super", super.toString())
        .toString();
  }

  @Override
  protected final boolean matchesSafely(T item) {
    Collection<Issue> newIssues = new ArrayList<>();
    validate(item, newIssues);
    synchronized (issues) {
      issues.clear();
      issues.addAll(newIssues);
    }
    return issues.isEmpty();
  }

  @Override
  protected final void describeMismatchSafely(@NotNull T item,
                                              @NotNull Description mismatchDescription) {
    Collection<Issue> mismatchIssues = clearIssues();
    mismatchDescription
        .appendText("was ");
    describeMismatchedItem(item, mismatchDescription);
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

  /**
   * Clear issues and return the old value of the issues.
   *
   * @return old issues
   */
  @NotNull
  protected Collection<Issue> clearIssues() {
    Collection<Issue> mismatchIssues;
    synchronized (issues) {
      mismatchIssues = new ArrayList<>(issues);
      // issues.clear();
    }
    return mismatchIssues;
  }

  /**
   * Describe the mismatched item. By default {@link Description#appendValue(Object)} is used.
   *
   * @param item                actual item to describe on mismatch
   * @param mismatchDescription description for mismatched item
   */
  protected void describeMismatchedItem(@NotNull T item, @NotNull Description mismatchDescription) {
    mismatchDescription.appendValue(item);
  }

  /**
   * Validate item and add issues to the provided collection. The matcher itself signals success
   * if no issues were collected.
   *
   * @param item   actual item to validate
   * @param issues issues
   */
  protected abstract void validate(@NotNull T item, @NotNull Collection<Issue> issues);

}
