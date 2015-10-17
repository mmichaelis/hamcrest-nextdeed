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

package com.github.mmichaelis.hamcrest.nextdeed;

import com.google.common.base.Function;

import com.github.mmichaelis.hamcrest.nextdeed.exception.IsJavaCompliantExceptionMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.exception.JavaComplianceLevel;
import com.github.mmichaelis.hamcrest.nextdeed.function.ApplyingMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.glue.BiFunction;
import com.github.mmichaelis.hamcrest.nextdeed.image.ImageIsEqual;
import com.github.mmichaelis.hamcrest.nextdeed.image.ImageType;
import com.github.mmichaelis.hamcrest.nextdeed.io.IsSerializable;
import com.github.mmichaelis.hamcrest.nextdeed.io.IsSerializable.BareSerializableMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresConstructor;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresMethod;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassModifierMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaConstructor;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.InstantiableViaDefaultConstructor;
import com.github.mmichaelis.hamcrest.nextdeed.reflect.MemberModifierMatcher;
import com.github.mmichaelis.hamcrest.nextdeed.util.ResourceBundleContainsKey;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.lang.reflect.Member;
import java.util.ResourceBundle;

/**
 * <p>
 * Convenience access to all fabric methods for matchers provided by
 * <em>Hamcrest &mdash; Next Deed</em>.
 * </p>
 *
 * @since 0.1.3
 */
public final class NextDeedMatchers {

  private NextDeedMatchers() {
  }

  /**
   * <p>
   * Applies a transformation to the value before comparing the transformed result with the given
   * matcher.
   * </p>
   *
   * @param function        the function to apply to convert the asserted value to the target value
   * @param delegateMatcher matcher to apply to the transformed value; typically the state of the
   *                        component under test
   * @param <F>             type to input into assertion
   * @param <T>             actual value type to compare
   * @return matcher which transforms input before comparison
   * @since 0.1.3
   */
  public static <F, T> Matcher<F> applying(@NotNull Function<F, T> function,
                                           @NotNull Matcher<? super T> delegateMatcher) {
    return ApplyingMatcher.applying(function, delegateMatcher);
  }

  /**
   * Validates that a declared constructor with the given parameters exists.
   *
   * @param parameterTypes the parameter array
   * @param <T>            the type of the class to check
   * @return matcher
   * @see #declaresNoArgumentsConstructor()
   */
  public static <T extends Class<?>> Matcher<T> declaresConstructor(
      @Nullable Class<?>... parameterTypes) {
    return ClassDeclaresConstructor.declaresConstructor(parameterTypes);
  }

  /**
   * Validates that a declared constructor with no parameters exists.
   *
   * @param <T> the type of the class to check
   * @return matcher
   * @see #declaresConstructor(Class[])
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresNoArgumentsConstructor() {
    return ClassDeclaresConstructor.declaresNoArgumentsConstructor();
  }

  /**
   * Matcher for modifiers of classes. Modifiers must be exactly as specified.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              the class whose modifiers shall be checked
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @see #classModifierContains(int)
   * @since 1.0.0
   */
  public static <T extends Class<?>> Matcher<T> classModifierIs(int expectedModifier) {
    return ClassModifierMatcher.classModifierIs(expectedModifier);
  }

  /**
   * Matcher for modifiers of classes. All defined modifiers must be set, but there may be more.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              the class whose modifiers shall be checked
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @since 1.0.0
   */
  public static <T extends Class<?>> Matcher<T> classModifierContains(int expectedModifier) {
    return ClassModifierMatcher.classModifierContains(expectedModifier);
  }

  /**
   * Matcher for modifiers of classes. All defined modifiers must be set, but there may be more.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              member type (field, method)
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @since 1.0.0
   */
  public static <T extends Member> Matcher<T> memberModifierContains(int expectedModifier) {
    return MemberModifierMatcher.memberModifierContains(expectedModifier);
  }

  /**
   * Matcher for modifiers of members. Modifiers must be exactly as specified.
   *
   * @param expectedModifier modifiers to validate
   * @param <T>              member type (field, method)
   * @return matcher
   * @see java.lang.reflect.Modifier
   * @see #memberModifierContains(int)
   * @since 1.0.0
   */
  public static <T extends Member> Matcher<T> memberModifierIs(int expectedModifier) {
    return MemberModifierMatcher.memberModifierIs(expectedModifier);
  }

  /**
   * Validates that the exception (or object) is compliant to standard Java Exceptions at
   * a certain compliance level. For example an exception cause exists since Java 1.1.
   *
   * @param level Java level to check
   * @param <T>   type of the exception/object
   * @return matcher
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isJavaCompliantException(
      @NotNull JavaComplianceLevel level) {
    return IsJavaCompliantExceptionMatcher.isJavaCompliantException(level);
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
    return IsSerializable.isSerializable();
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
    return IsSerializable.isSerializable(typeToken);
  }

  /**
   * Validates that a resource bundle contains the given key.
   *
   * @param expectedKey expected key
   * @return matcher
   */
  @NotNull
  public static Matcher<ResourceBundle> resourceBundleContainsKey(@NotNull String expectedKey) {
    return ResourceBundleContainsKey.resourceBundleContainsKey(expectedKey);
  }

  /**
   * Validates that a declared method with the possibly given parameters exists.
   *
   * @param methodName     name of the method
   * @param parameterTypes the parameter array; {@code null} for ignoring parameters and just search
   *                       for name
   * @param <T>            the type of the class to check
   * @return matcher
   * @see #declaresMethodWithName(String)
   * @see #declaresNoArgumentsMethod(String)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresMethod(
      @NotNull String methodName,
      @Nullable Class<?>... parameterTypes) {
    return ClassDeclaresMethod.declaresMethod(methodName, parameterTypes);
  }

  /**
   * Validates that a declared method with no arguments exists.
   *
   * @param methodName name of the method
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #declaresMethod(String, Class[])
   * @see #declaresMethodWithName(String)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresNoArgumentsMethod(
      @NotNull String methodName) {
    return ClassDeclaresMethod.declaresNoArgumentsMethod(methodName);
  }

  /**
   * Validates that a declared method with the given name exists, no matter what parameters the
   * method has.
   *
   * @param methodName name of the method
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #declaresMethod(String, Class[])
   * @see #declaresNoArgumentsMethod(String)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> declaresMethodWithName(
      @NotNull String methodName) {
    return ClassDeclaresMethod.declaresMethodWithName(methodName);
  }

  /**
   * Validates that a declared constructor with the given parameters exists and can be
   * instantiated.
   *
   * @param parameters the parameter array
   * @param <T>        the type of the class to check
   * @return matcher
   * @see #isInstantiableWithNoArguments()
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isInstantiableWith(
      @Nullable Object... parameters) {
    return InstantiableViaConstructor.isInstantiableWith(parameters);
  }

  /**
   * Validates that a declared constructor with no arguments exists and can be
   * instantiated.
   *
   * @param <T> the type of the class to check
   * @return matcher
   * @see #isInstantiableWith(Object...)
   * @since SINCE
   */
  @NotNull
  public static <T extends Class<?>> Matcher<T> isInstantiableWithNoArguments() {
    return InstantiableViaConstructor.isInstantiableWithNoArguments();
  }

  /**
   * Validates that the given class is instantiable via default constructor. This means that
   * the default constructor must exist and that it must be able to be called without exceptions.
   * Depending on the security manager protected as well as private default constructors will be
   * found.
   *
   * @param <T> class to validate
   * @return matcher
   * @since SINCE
   * @deprecated Use {@link InstantiableViaConstructor#isInstantiableWithNoArguments()} instead.
   */
  @SuppressWarnings("deprecation")
  @NotNull
  @Deprecated
  public static <T extends Class<?>> Matcher<T> isInstantiableViaDefaultConstructor() {
    return InstantiableViaDefaultConstructor.isInstantiableViaDefaultConstructor();
  }

  /**
   * <p>
   * Comparison without image handler. On failure standard mismatch description is generated.
   * </p>
   *
   * @param expectedImage image to compare to
   * @return matcher
   * @since SINCE
   */
  @NotNull
  public static Matcher<BufferedImage> imageEqualTo(@NotNull BufferedImage expectedImage) {
    return ImageIsEqual.imageEqualTo(expectedImage);
  }

  /**
   * <p>
   * Comparison with image handler. The image handler will be called upon mismatch description to
   * each image used during comparison and might provide more details of the image or even store
   * the images for later reference in a report folder.
   * </p>
   * <dl>
   * <dt><strong>Image Handler:</strong></dt>
   * <dd>
   * <p>
   * The image handler gets the type of the image (actual, expected or difference) and the
   * corresponding image. The output will be appended to the mismatch description and might
   * either provide some details on the image or if you store the files you might want to
   * output the location where to find the image.
   * </p>
   * </dd>
   * </dl>
   *
   * @param expectedImage        expected image
   * @param imageHandlerFunction image handler called during mismatch description
   * @return matcher
   * @since SINCE
   */
  @NotNull
  public static Matcher<BufferedImage> imageEqualTo(@NotNull BufferedImage expectedImage,
                                                    @NotNull BiFunction<ImageType, BufferedImage, String> imageHandlerFunction) {
    return ImageIsEqual.imageEqualTo(expectedImage, imageHandlerFunction);
  }

}
