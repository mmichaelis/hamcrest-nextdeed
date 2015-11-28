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

package com.github.mmichaelis.hamcrest.nextdeed.incubator;

import static com.github.mmichaelis.hamcrest.nextdeed.NextDeedMatchers.applying;
import static com.github.mmichaelis.hamcrest.nextdeed.incubator.SystemPropertyChanger.SYSTEM_PROPERTY_CHANGER;
import static com.google.common.collect.Maps.difference;
import static com.google.common.collect.Maps.filterEntries;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;

import com.github.mmichaelis.hamcrest.nextdeed.MdcAccess;
import com.github.mmichaelis.hamcrest.nextdeed.glue.DescribedFunction;

import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Tests {@link SystemPropertyChanger}.
 *
 * @since SINCE
 */
@SuppressWarnings("AccessOfSystemProperties")
public class SystemPropertyChangerTest {
  private static final Matcher<Map<?, ?>>
      EMPTY_MAP =
      applying(DescribedFunction.describe(new MapSize()).as("map size"), equalTo(0));
  private static final String PROPERTY_ADDED_BEFORE = "property.added.before";
  private static final String VALUE_BEFORE = "value before";
  private final ErrorCollector errorCollector = new ErrorCollector();
  private final SystemPropertyValidatorRule
      systemPropertyValidatorRule = new SystemPropertyValidatorRule(errorCollector);
  private final SystemPropertyChanger systemPropertyChangerUnderTest = SYSTEM_PROPERTY_CHANGER;
  private final ExpectedException expectedException = ExpectedException.none();
  private final MdcAccess mdcAccess = new MdcAccess();

  @Rule
  public RuleChain
      ruleChain =
      RuleChain
          .outerRule(errorCollector)
          .around(new TestWatcher() {
            @Override
            protected void starting(Description description) {
              System.setProperty(PROPERTY_ADDED_BEFORE, VALUE_BEFORE);
            }

            @Override
            protected void finished(Description description) {
              System.clearProperty(PROPERTY_ADDED_BEFORE);
            }
          })
          .around(systemPropertyValidatorRule)
          .around(expectedException)
          .around(mdcAccess)
          .around(systemPropertyChangerUnderTest);

  @Test
  public void doNothingIfNoPropertiesWereChanged() throws Exception {
    // Test is actually performed within rule.
  }

  @Test
  public void dontRevertSystemPropertiesNotAddedViaRule() throws Exception {
    String key = "property.added.without.rule";
    String value = "you shouldn't have done this.";
    System.setProperty(key, value);
    mdcAccess.startSuppressLogging();
    systemPropertyValidatorRule.addExpectAdded(key, value);
  }

  @Test
  public void dontRevertSystemPropertiesNotRemovedViaRule() throws Exception {
    System.clearProperty(PROPERTY_ADDED_BEFORE);
    mdcAccess.startSuppressLogging();
    systemPropertyValidatorRule.addExpectRemoved(PROPERTY_ADDED_BEFORE, VALUE_BEFORE);
  }

  @Test
  public void dontRevertSystemPropertiesNotChangedViaRule() throws Exception {
    String value = "new value";
    System.setProperty(PROPERTY_ADDED_BEFORE, value);
    mdcAccess.startSuppressLogging();
    systemPropertyValidatorRule.addExpectChanged(PROPERTY_ADDED_BEFORE, VALUE_BEFORE, value);
  }

  @Test
  public void revertPropertyAutomaticallyClearedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.clearProperty(PROPERTY_ADDED_BEFORE);
  }

  @Test
  public void revertPropertyAutomaticallyChangedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, "new value");
  }

  @Test
  public void revertPropertyAutomaticallyAddedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.setProperty("system.property.for.test", "some value");
  }

  @Test
  public void revertPropertyAutomaticallyChangedTwiceDuringTest() throws Exception {
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, "new value");
  }

  @Test
  public void revertSinglePropertyManuallyClearedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.clearProperty(PROPERTY_ADDED_BEFORE);
    systemPropertyChangerUnderTest.restoreProperty(PROPERTY_ADDED_BEFORE);
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(VALUE_BEFORE));
  }

  @Test
  public void revertSinglePropertyManuallyChangedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, "new value");
    systemPropertyChangerUnderTest.restoreProperty(PROPERTY_ADDED_BEFORE);
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(VALUE_BEFORE));
  }

  @Test
  public void revertSinglePropertyManuallyAddedDuringTest() throws Exception {
    String property = "system.property.for.test";
    systemPropertyChangerUnderTest.setProperty(property, "some value");
    systemPropertyChangerUnderTest.restoreProperty(property);
    errorCollector.checkThat(System.getProperty(property), nullValue());
  }

  @Test
  public void revertAllPropertiesManuallyClearedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.clearProperty(PROPERTY_ADDED_BEFORE);
    systemPropertyChangerUnderTest.restoreProperties();
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(VALUE_BEFORE));
  }

  @Test
  public void revertAllPropertiesManuallyChangedDuringTest() throws Exception {
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, "new value");
    systemPropertyChangerUnderTest.restoreProperties();
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(VALUE_BEFORE));
  }

  @Test
  public void revertAllPropertiesManuallyAddedDuringTest() throws Exception {
    String property = "system.property.for.test";
    systemPropertyChangerUnderTest.setProperty(property, "some value");
    systemPropertyChangerUnderTest.restoreProperties();
    errorCollector.checkThat(System.getProperty(property), nullValue());
  }

  @Test
  public void canAddPropertyDuringTest() throws Exception {
    String property = "system.property.for.test";
    String value = "some value";
    systemPropertyChangerUnderTest.setProperty(property, value);
    errorCollector.checkThat(System.getProperty(property), equalTo(value));
  }

  @Test
  public void canRemovePropertyDuringTest() throws Exception {
    systemPropertyChangerUnderTest.clearProperty(PROPERTY_ADDED_BEFORE);
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), nullValue());
  }

  @Test
  public void canChangePropertyDuringTest() throws Exception {
    String value = "some value";
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, value);
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(value));
  }

  @Test
  public void canChangePropertyTwiceDuringTest() throws Exception {
    String value1 = "some value";
    String value2 = "some other value";
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, value1);
    systemPropertyChangerUnderTest.setProperty(PROPERTY_ADDED_BEFORE, value2);
    errorCollector.checkThat(System.getProperty(PROPERTY_ADDED_BEFORE), equalTo(value2));
  }

  @Test
  public void containsRelevantInformationInToString() throws Exception {
    errorCollector.checkThat(systemPropertyChangerUnderTest, Matchers.hasToString(
        Matchers.allOf(
            Matchers.containsString("originalPropertyValues"),
            Matchers.containsString("propertyOwners")
        )
    ));
  }

  @Test
  public void denyAccessInSameThreadToSamePropertyForOtherTest() throws Throwable {
    final String key = "shared.property";
    String value1 = "some value";
    final String value2 = "some other value";
    systemPropertyChangerUnderTest.setProperty(key, value1);

    expectedException.expect(IllegalStateException.class);

    systemPropertyChangerUnderTest.apply(new Statement() {
      @Override
      public void evaluate() throws Throwable {
        systemPropertyChangerUnderTest.setProperty(key, value2);
      }
    }, Description.EMPTY).evaluate();
  }

  @Test
  public void allowAccessToOtherPropertyForOtherTest() throws Throwable {
    String key1 = "shared.property.1";
    final String key2 = "shared.property.2";
    String value1 = "some value";
    final String value2 = "some other value";
    systemPropertyChangerUnderTest.setProperty(key1, value1);

    final CountDownLatch propertySetLatch = new CountDownLatch(1);
    final CountDownLatch propertyCheckedLatch = new CountDownLatch(1);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Void> future = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          systemPropertyChangerUnderTest.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
              try {
                systemPropertyChangerUnderTest.setProperty(key2, value2);
              } finally {
                propertySetLatch.countDown();
                propertyCheckedLatch.await();
              }
            }
          }, Description.EMPTY).evaluate();
        } catch (Throwable throwable) {
          throw new RuntimeException(throwable);
        }
        return null;
      }
    });
    executor.shutdown();

    propertySetLatch.await();

    errorCollector.checkThat(System.getProperty(key1), equalTo(value1));
    errorCollector.checkThat(System.getProperty(key2), equalTo(value2));

    propertyCheckedLatch.countDown();

    executor.awaitTermination(5, TimeUnit.SECONDS);

    future.get();
  }

  @Test
  public void denyAccessToSomePropertyForOtherTest() throws Throwable {
    final String key = "shared.property.1";
    String value1 = "some value";
    final String value2 = "some other value";
    systemPropertyChangerUnderTest.setProperty(key, value1);

    final CountDownLatch propertySetLatch = new CountDownLatch(1);
    final CountDownLatch propertyCheckedLatch = new CountDownLatch(1);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<Void> future = executor.submit(new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        try {
          systemPropertyChangerUnderTest.apply(new Statement() {
            @Override
            public void evaluate() throws Throwable {
              try {
                systemPropertyChangerUnderTest.setProperty(key, value2);
              } finally {
                propertySetLatch.countDown();
                propertyCheckedLatch.await();
              }
            }
          }, Description.EMPTY).evaluate();
        } catch (Throwable throwable) {
          throw new RuntimeException(throwable);
        }
        return null;
      }
    });
    executor.shutdown();

    propertySetLatch.await();

    errorCollector.checkThat(System.getProperty(key), equalTo(value1));

    propertyCheckedLatch.countDown();

    executor.awaitTermination(5, TimeUnit.SECONDS);

    expectedException.expect(ExecutionException.class);
    expectedException.expectCause(new CustomTypeSafeMatcher<Throwable>("is IllegalStateException containing " + key + " in description") {
      @Override
      protected boolean matchesSafely(Throwable item) {
        Throwable nestedCause = item.getCause();
        return (nestedCause instanceof IllegalStateException)
               && nestedCause.getMessage().contains(key);
      }
    });
    future.get();
  }

  private static class SystemPropertyValidatorRule extends TestWatcher {

    private final ErrorCollector errorCollector;
    private final Map<Object, Object> expectAdded = new HashMap<>();
    private final Map<Object, Object> expectRemoved = new HashMap<>();
    private final Map<Object, Difference<?>> expectChanged = new HashMap<>();

    public SystemPropertyValidatorRule(ErrorCollector errorCollector) {
      this.errorCollector = errorCollector;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          Map<Object, Object> propertiesBefore = new HashMap<>(System.getProperties());
          base.evaluate();
          Map<Object, Object> propertiesAfter = new HashMap<>(System.getProperties());
          MapDifference<Object, Object> difference = difference(propertiesBefore, propertiesAfter);
          errorCollector.checkThat("No new properties should have been added.",
                                   filterEntries(difference.entriesOnlyOnRight(),
                                                 new Predicate<Entry<Object, Object>>() {
                                                   @Override
                                                   public boolean apply(
                                                       Entry<Object, Object> input) {
                                                     return !Objects
                                                         .equals(expectAdded.get(input.getKey()),
                                                                 input.getValue());
                                                   }
                                                 }),
                                   EMPTY_MAP);
          errorCollector.checkThat("No properties should have been removed.",
                                   filterEntries(difference.entriesOnlyOnLeft(),
                                                 new Predicate<Entry<Object, Object>>() {
                                                   @Override
                                                   public boolean apply(
                                                       Entry<Object, Object> input) {
                                                     return !Objects
                                                         .equals(expectRemoved.get(input.getKey()),
                                                                 input.getValue());
                                                   }
                                                 }),
                                   EMPTY_MAP);
          errorCollector.checkThat("No property values should have been changed.",
                                   filterEntries(difference.entriesDiffering(),
                                                 new Predicate<Entry<Object, ValueDifference<Object>>>() {
                                                   @Override
                                                   public boolean apply(Entry<Object, ValueDifference<Object>> input) {
                                                     Object diffKey = input.getKey();
                                                     ValueDifference<Object>
                                                         diffValue =
                                                         input.getValue();
                                                     return !(expectChanged.containsKey(diffKey)
                                                              && expectChanged.get(diffKey)
                                                                  .isEqualTo(diffValue));
                                                   }
                                                 }),
                                   EMPTY_MAP);
        }
      };
    }

    public void addExpectAdded(String key, String value) {
      expectAdded.put(key, value);
    }

    public void addExpectRemoved(String key, String value) {
      expectRemoved.put(key, value);
    }

    public void addExpectChanged(String key, String oldValue, String newValue) {
      expectChanged.put(key, new Difference<>(oldValue, newValue));
    }
  }

  private static class Difference<T> {
    private final T oldValue;
    private final T newValue;

    public Difference(T oldValue, T newValue) {
      this.oldValue = oldValue;
      this.newValue = newValue;
    }

    public boolean isEqualTo(ValueDifference<? super T> valueDifference) {
      return Objects.equals(valueDifference.leftValue(), oldValue)
             && Objects.equals(valueDifference.rightValue(), newValue);
    }
  }

  private static class MapSize implements Function<Map<?, ?>, Integer> {

    @Override
    public Integer apply(Map<?, ?> input) {
      return input.size();
    }
  }
}
