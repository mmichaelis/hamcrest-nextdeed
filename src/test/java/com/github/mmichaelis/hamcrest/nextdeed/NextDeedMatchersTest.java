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

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.classModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.declaresNoArgumentsConstructor;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.isInstantiableWithNoArguments;
import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.memberModifierContains;
import static com.github.mmichaelis.hamcrest.nextdeed.reflect.ClassDeclaresMethod.declaresMethod;
import static java.lang.reflect.Modifier.isAbstract;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.everyItem;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.common.reflect.TypeToken;

import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashSet;

/**
 * Tests {@link NextDeedMatchers}.
 *
 * @since SINCE
 */
public class NextDeedMatchersTest {

  @Rule
  public ErrorCollector errorCollector = new ErrorCollector();

  @Test
  public void allFactoryClassesAvailableInFacade() throws Exception {
    Collection<Method> factoryMethods = getCandidateMethods();
    Class<NextDeedMatchers> inspectedClass = NextDeedMatchers.class;
    for (Method factoryMethod : factoryMethods) {
      errorCollector.checkThat("Factory method should be contained in facade.",
                               inspectedClass,
                               declaresMethod(
                                   factoryMethod.getName(),
                                   factoryMethod.getParameterTypes()
                               )
      );
    }
  }

  @Test
  public void isUtilityClass() throws Exception {
    errorCollector.checkThat(
        NextDeedMatchers.class,
        allOf(
            declaresNoArgumentsConstructor(),
            classModifierContains(Modifier.FINAL),
            isInstantiableWithNoArguments()
        )
    );

    errorCollector.checkThat(
        "Any constructors must be private.",
        asList(NextDeedMatchers.class.getDeclaredConstructors()),
        everyItem(memberModifierContains(Modifier.PRIVATE)));
  }


  @NotNull
  private Collection<Method> getCandidateMethods() throws IOException {
    TypeToken<Matcher> matcherType = TypeToken.of(Matcher.class);
    Collection<Class<?>> matcherClasses = getClassesExtending(matcherType);
    return getFactoryMethodsReturning(matcherClasses, matcherType);
  }

  @NotNull
  private Collection<Method> getFactoryMethodsReturning(Collection<Class<?>> matcherClasses,
                                                        TypeToken<Matcher> matcherType) {
    Collection<Method> factoryMethods = new HashSet<>(matcherClasses.size() * 2);
    for (Class<?> matcherClass : matcherClasses) {
      Method[] declaredMethods = matcherClass.getDeclaredMethods();
      for (Method declaredMethod : declaredMethods) {
        int modifiers = declaredMethod.getModifiers();
        if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers)) {
          if (matcherType.isAssignableFrom(declaredMethod.getReturnType())) {
            factoryMethods.add(declaredMethod);
          }
        }
      }
    }
    return factoryMethods;
  }

  @NotNull
  private Collection<Class<?>> getClassesExtending(TypeToken<Matcher> matcherType)
      throws IOException {
    Collection<Class<?>> matcherClasses = new HashSet<>();
    Package rootPackage = NextDeedMatchers.class.getPackage();
    ClassPath classpath = ClassPath.from(NextDeedMatchers.class.getClassLoader());
    for (ClassInfo classInfo : classpath.getTopLevelClassesRecursive(rootPackage.getName())) {
      Class<?> clazz = classInfo.load();
      TypeToken<?> typeToken = TypeToken.of(clazz);
      if (matcherType.isAssignableFrom(typeToken) && !isAbstract(clazz.getModifiers())) {
        matcherClasses.add(clazz);
      }
    }
    return matcherClasses;
  }

}
