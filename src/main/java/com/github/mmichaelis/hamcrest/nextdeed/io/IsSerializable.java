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

import static com.github.mmichaelis.hamcrest.nextdeed.io.Messages.messages;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;

import com.github.mmichaelis.hamcrest.nextdeed.base.Issue;
import com.github.mmichaelis.hamcrest.nextdeed.base.IssuesMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.hamcrest.TypeSafeMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Collection;

/**
 * <p>
 * Tests that a given value is serializable and might also validate that the deserialized result
 * matches the expectations.
 * </p>
 *
 * @see <a href="http://www.jguru.com/faq/view.jsp?EID=251942">jGuru: Are classes that implement
 * Serializable required to have no-argument constructors?</a>
 * @see <a href="http://www.jguru.com/faq/view.jsp?EID=31434">jGuru: What things are required for a
 * class that implements Serializable?</a>
 * @see <a href="https://studyprogramming.wordpress.com/2009/03/18/serialpersistentfields/">serialPersistentFields
 * | About java</a>
 * @see <a href="https://docs.oracle.com/javase/7/docs/platform/serialization/spec/serial-arch.html">Java
 * Object Serialization Specification: 1 - System Architecture</a>
 * @since SINCE
 */
public class IsSerializable<T> extends IssuesMatcher<T> {

  /**
   * Matcher for the deserialized object. If {@code null}, don't perform any match.
   *
   * @since SINCE
   */
  @Nullable
  private final Matcher<? super T> deserializedMatcher;

  /**
   * Bare serialization test without further validations of the deserialized object.
   *
   * @since SINCE
   */
  public IsSerializable() {
    this(null);
  }

  /**
   * <p>
   * Serialization test with possibly further validations of the deserialized object.
   * </p>
   *
   * @param deserializedMatcher matcher to apply to the deserialized object; {@code null} for no
   *                            validation to apply
   * @since SINCE
   */
  public IsSerializable(@Nullable Matcher<? super T> deserializedMatcher) {
    super(messages().isSerializable());
    this.deserializedMatcher = deserializedMatcher;
  }

  /**
   * <p>
   * Simple check that an object serializes and deserializes. You might want to extend
   * it with additional checks if the deserialized object matches your requirements
   * (see {@link BareSerializableMatcher#deserializedResultMatches(Matcher)}).
   * </p>
   *
   * @param <T> type to check
   * @return matcher
   * @see BareSerializableMatcher#deserializedResultMatches(Matcher)
   * @see BareSerializableMatcher#and()
   * @see #isSerializable(Class)
   * @since SINCE
   */
  @SuppressWarnings("MethodReturnOfConcreteClass")
  @NotNull
  public static <T> BareSerializableMatcher<T> isSerializable() {
    return new BareSerializableMatcher<>();
  }

  /**
   * <p>
   * Simple check that an object serializes and deserializes. You might want to extend
   * it with additional checks if the deserialized object matches your requirements
   * (see {@link BareSerializableMatcher#deserializedResultMatches(Matcher)}).
   * </p>
   *
   * @param <T>       type to check
   * @param typeToken token to successfully resolve {@code <T>}.
   * @return matcher
   * @see BareSerializableMatcher#deserializedResultMatches(Matcher)
   * @see BareSerializableMatcher#and()
   * @since SINCE
   */
  @SuppressWarnings({"MethodReturnOfConcreteClass", "UnusedParameters"})
  @NotNull
  public static <T> BareSerializableMatcher<T> isSerializable(Class<T> typeToken) {
    return new BareSerializableMatcher<>();
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void validate(@NotNull T item, @NotNull Collection<Issue> issues) {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    if (!validateSerialization(item, bytes, issues)) {
      return;
    }
    T newItem;
    try (ObjectInput in = new ObjectInputStream(
              new ByteArrayInputStream(bytes.toByteArray()))) {
      newItem = (T) in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      issues.add(issue(messages().serializationDeserializationFailure(e)));
      return;
    }
    if (deserializedMatcher != null && !deserializedMatcher.matches(newItem)) {
      Description description = new StringDescription();
      description.appendText("Expected: ")
                 .appendDescriptionOf(deserializedMatcher)
                 .appendText(" but: ");
      deserializedMatcher.describeMismatch(newItem, description);
      issues.add(issue(description.toString()));
    }
  }

  private static boolean validateSerialization(@NotNull Object item,
                                               @NotNull ByteArrayOutputStream bytes,
                                               @NotNull Collection<Issue> issues) {
    try (final ObjectOutput out = new ObjectOutputStream(bytes)) {
      out.writeObject(item);
    } catch (IOException e) {
      issues.add(issue(messages().serializationDeserializationFailure(e)));
      return false;
    }
    return true;
  }

  /**
   * This matcher is an intermediate result of the {@link IsSerializable} matcher. If no
   * additional matcher is applied it simply delegates all matches and descriptions to the
   * {@link IsSerializable} matcher. The matcher will resolve the delegate once a matcher
   * for the deserialized object is applied and thus hands over directly to {@link IsSerializable}.
   *
   * @param <T> type of the object to match
   * @since SINCE
   */
  public static final class BareSerializableMatcher<T> extends TypeSafeMatcher<T> {

    /**
     * Intermediate delegate matcher to possibly use.
     *
     * @since SINCE
     */
    @Nullable
    private IsSerializable<T> delegateMatcher;

    private BareSerializableMatcher() {
      delegateMatcher = new IsSerializable<>();
    }

    /**
     * Syntactic sugar.
     *
     * @return self-reference
     * @since SINCE
     */
    @SuppressWarnings("MethodReturnOfConcreteClass")
    public BareSerializableMatcher<T> and() {
      return this;
    }

    /**
     * <p>
     * Matcher to apply to the deserialized result.
     * </p>
     * <dl>
     * <dt><strong>Note:</strong></dt>
     * <dd>
     * If you happen to have problems to resolve the type for the matcher you might want to
     * explicitly set the type in {@link #isSerializable()} or use the type-tokened version
     * {@link #isSerializable(Class)}.
     * </dd>
     * </dl>
     *
     * @param deserializedMatcher matcher to apply
     * @return matcher
     * @since SINCE
     */
    public Matcher<T> deserializedResultMatches(@NotNull Matcher<? super T> deserializedMatcher) {
      delegateMatcher = null;
      return new IsSerializable<>(deserializedMatcher);
    }

    @Override
    public void describeTo(Description description) {
      getDelegate().describeTo(description);
    }

    @Override
    protected boolean matchesSafely(T item) {
      return getDelegate().matches(item);
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
      getDelegate().describeMismatch(item, mismatchDescription);
    }

    @NotNull
    private Matcher<T> getDelegate() {
      // You possibly used the bare matcher after already retrieving the final IsSerializable
      // matcher -- which is wrong :-)
      return requireNonNull(delegateMatcher, "Illegal State: delegateMatcher must not be null.");
    }
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("deserializedMatcher", deserializedMatcher)
        .add("super", super.toString())
        .toString();
  }
}
