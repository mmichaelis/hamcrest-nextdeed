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

package com.github.mmichaelis.hamcrest.nextdeed.reflect;

import com.google.common.base.MoreObjects;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.lang.reflect.Modifier;

/**
 * Matcher for modifiers of class members such as constructors, methods and fields.
 *
 * @see Modifier
 * @since SINCE
 */
public abstract class ModifierMatcherBase<T> extends TypeSafeMatcher<T> {

  private final int expectedModifier;
  private final String typeName;
  private final boolean strict;

  /**
   * Creates a matcher for member modifiers.
   *
   * @param expectedModifier modifiers to validate
   * @param typeName         what type (member, class) the modifiers are evaluated from
   * @param strict           if {@code true}, all given modifiers must be set, if {@code false} at
   *                         least these modifiers must be set
   * @see Modifier
   * @since SINCE
   */
  public ModifierMatcherBase(int expectedModifier, String typeName, boolean strict) {
    this.expectedModifier = expectedModifier;
    this.typeName = typeName;
    this.strict = strict;
  }

  @Override
  public void describeTo(Description description) {
    description.appendText(typeName).appendText(" with ");
    describeModifier(expectedModifier, description);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("expectedModifier", expectedModifier)
        .add("super", super.toString())
        .toString();
  }

  /**
   * Retrieve modifiers of the given item.
   *
   * @param item item to retrieve modifiers from
   * @return modifiers
   * @see Modifier
   * @since SINCE
   */
  protected abstract int getModifiers(T item);

  @Override
  protected boolean matchesSafely(T item) {
    if (strict) {
      return getModifiers(item) == expectedModifier;
    } else {
      return (getModifiers(item) & expectedModifier) == expectedModifier;
    }
  }

  @Override
  protected void describeMismatchSafely(T item, Description mismatchDescription) {
    int actualModifiers = getModifiers(item);
    describeModifier(actualModifiers,
                     mismatchDescription
                         .appendText("was ")
                         .appendText(typeName)
                         .appendText(" ")
                         .appendValue(item)
                         .appendText(" with ")
    );
    mismatchDescription.appendText(" (difference: ")
        .appendValue(Modifier.toString(actualModifiers ^ expectedModifier))
        .appendText(")");
  }

  private void describeModifier(int modifier, Description description) {
    String modifierString = Modifier.toString(modifier);
    String modifiersText =
        (modifierString.isEmpty() || (modifierString.indexOf(' ') != -1)) ? "modifiers"
                                                                          : "modifier";
    description
        .appendText(modifiersText)
        .appendText(strict ? "" : " containing ")
        .appendText(": ")
        .appendValue(modifierString);
  }
}
