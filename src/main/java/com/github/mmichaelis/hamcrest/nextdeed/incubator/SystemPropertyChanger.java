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

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * <p>
 * Modifies a system property and restores it after the test to the value before the test
 * execution. Iff. all tests use this system property changer it ensures that no other tests
 * modify a property at the same time. Otherwise it will not be possibly to reliably restore
 * system properties.
 * </p>
 *
 * @since SINCE
 */
@SuppressWarnings("AccessOfSystemProperties")
public enum SystemPropertyChanger implements TestRule {
  /**
   * Instance of the system property changer. Name duplication in order to support static
   * imports.
   *
   * @since SINCE
   */
  SYSTEM_PROPERTY_CHANGER;

  private static final Logger LOG = getLogger(SystemPropertyChanger.class);
  private static final ThreadLocal<Description> TEST_CONTEXT = new ThreadLocal<>();
  private static final String NEWLINE = System.getProperty("line.separator");
  private static final String INDENT = "    ";
  private static final String UNSET = "<unset>";
  private final Map<String, Description> propertyOwners = new HashMap<>();
  private final Map<String, String> originalPropertyValues = new HashMap<>();

  private static void dumpUntrackedSystemPropertyChanges(Map<Object, Object> propertiesBefore,
                                                         Description description) {
    Map<Object, Object> propertiesAfter = new HashMap<>(System.getProperties());
    Set<Object> keysBefore = propertiesBefore.keySet();
    Set<Object> keysAfter = propertiesAfter.keySet();
    Collection<Object> mergedKeys = new TreeSet<>(keysBefore);
    mergedKeys.addAll(keysAfter);

    MapDifference<Object, Object> difference = Maps.difference(propertiesBefore, propertiesAfter);

    dumpDiffering(description, difference);
    dumpRemoved(description, difference);
    dumpAdded(description, difference);
  }

  private static void dumpAdded(Description description, MapDifference<Object, Object> difference) {
    Map<Object, Object> addedEntries = difference.entriesOnlyOnRight();

    if (!addedEntries.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (Entry<Object, Object> entry : addedEntries.entrySet()) {
        addDifference(builder, entry.getKey(), UNSET, entry.getValue());
      }
      LOG.warn("System properties added without notice during test {}: {}{}",
               description.getMethodName(), NEWLINE, builder);
    }
  }

  private static void dumpRemoved(Description description,
                                  MapDifference<Object, Object> difference) {
    Map<Object, Object> removedEntries = difference.entriesOnlyOnLeft();

    if (!removedEntries.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (Entry<Object, Object> entry : removedEntries.entrySet()) {
        addDifference(builder, entry.getKey(), entry.getValue(), UNSET);
      }
      LOG.warn("System properties removed without notice during test {}: {}{}",
               description.getMethodName(), NEWLINE, builder);
    }
  }

  private static void dumpDiffering(Description description,
                                    MapDifference<Object, Object> difference) {
    Map<Object, ValueDifference<Object>> differingEntries = difference.entriesDiffering();

    if (!differingEntries.isEmpty()) {
      StringBuilder builder = new StringBuilder();
      for (Entry<Object, ValueDifference<Object>> entry : differingEntries.entrySet()) {
        addDifference(builder, entry.getKey(), entry.getValue().leftValue(),
                      entry.getValue().rightValue());
      }
      LOG.warn("System properties changed without notice during test {}: {}{}",
               description.getMethodName(), NEWLINE, builder);
    }
  }


  private static void addDifference(@NotNull StringBuilder builder, Object mergedKey,
                                    Object valueBefore, Object valueAfter) {
    builder.append(INDENT).append(mergedKey).append('=').append(valueAfter)
        .append(" (was: ").append(valueBefore).append(')')
        .append(NEWLINE);
  }

  @Override
  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Map<Object, Object> propertiesBefore = new HashMap<>(System.getProperties());
        starting(description);
        try {
          base.evaluate();
        } finally {
          finished(description);
          if (LOG.isWarnEnabled()) {
            dumpUntrackedSystemPropertyChanges(propertiesBefore, description);
          }
        }
      }
    };
  }

  /**
   * <p>
   * Restore the given property. As a result this will also free the property for other tests which
   * might run in parallel.
   * </p>
   *
   * @param name name of the property
   * @throws IllegalStateException if called outside of test contexts or if the current test is not
   *                               eligible accessing the given property as it is used by another
   *                               test
   * @since SINCE
   */
  public synchronized void restoreProperty(@NotNull String name) {
    Description currentContext = validatedContext();
    checkPermission(currentContext, name);
    restorePropertyUnchecked(name);
  }

  /**
   * <p>
   * Restore all properties owned by the current test. As a result this will also free the
   * properties for other tests which might run in parallel.
   * </p>
   *
   * @throws IllegalStateException if called outside of test contexts
   * @since SINCE
   */
  public synchronized void restoreProperties() {
    restorePropertiesUnchecked(validatedContext());
  }

  /**
   * <p>
   * Removes the system property indicated by the specified key and stores it for later recovering
   * the original system property value.
   * </p>
   *
   * @param name property name to remove
   * @throws IllegalStateException if called outside of test contexts or if the current test is not
   *                               eligible accessing the given property as it is used by another
   *                               test
   * @since SINCE
   */
  public synchronized void clearProperty(@NotNull String name) {
    setProperty(name, null);
  }

  /**
   * <p>
   * Sets the system property indicated by the specified key to the given value and stores the
   * original value for later recovering original system property value.
   * </p>
   *
   * @param name  property name to remove
   * @param value value to set the property to; {@code null} will remove the property
   * @throws IllegalStateException if called outside of test contexts or if the current test is not
   *                               eligible accessing the given property as it is used by another
   *                               test
   * @since SINCE
   */
  public synchronized void setProperty(@NotNull String name, @Nullable String value) {
    Description currentContext = validatedContext();
    checkPermission(currentContext, name);
    setPropertyUnchecked(name, value, currentContext);
  }

  @Override
  public synchronized String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("originalPropertyValues", originalPropertyValues)
        .add("propertyOwners", propertyOwners)
        .add("super", super.toString())
        .toString();
  }

  /**
   * <p>
   * Sets the property without doing any checks which must have been done before.
   * </p>
   *
   * @param name  property name to remove
   * @param value value to set the property to; {@code null} will remove the property
   */
  private void setPropertyUnchecked(@NotNull String name,
                                    @Nullable String value,
                                    @NotNull Description currentContext) {
    String previousValue = setOrClearSystemProperty(name, value);
    if (!originalPropertyValues.containsKey(name)) {
      LOG.debug(
          "Setting system property '{}' to '{}' by {}. Will restore to previous value '{}' after test.",
          name, value, currentContext, previousValue);
      originalPropertyValues.put(name, previousValue);
      propertyOwners.put(name, currentContext);
    } else {
      LOG.debug("Setting system property '{}' to '{}' by {}.", name, value, currentContext);
    }
  }

  /**
   * <p>
   * Restores the property without doing any checks which must have been done before. As side
   * effect
   * also frees the property for use in other contexts.
   * </p>
   *
   * @param name property name to remove
   */
  private synchronized void restorePropertyUnchecked(@NotNull String name) {
    String originalValue = originalPropertyValues.remove(name);
    propertyOwners.remove(name);
    setOrClearSystemProperty(name, originalValue);
    LOG.debug("Restored system property '{}' to '{}'.", name, originalValue);
  }

  /**
   * <p>
   * Sets or removes the current system property and returns the previous value.
   * </p>
   *
   * @param name  property name to remove
   * @param value value to set the property to; {@code null} will remove the property
   * @return previous value; {@code null} if it was not set
   */
  @Nullable
  private static String setOrClearSystemProperty(@NotNull String name, @Nullable String value) {
    String previousValue;
    if (value == null) {
      previousValue = System.clearProperty(name);
    } else {
      previousValue = System.setProperty(name, value);
    }
    return previousValue;
  }

  /**
   * <p>
   * Validates that the current context is known.
   * </p>
   *
   * @return current context
   * @throws IllegalStateException if this rule is used outside of test lifecycle
   */
  @NotNull
  private static Description validatedContext() {
    Description currentContext = TEST_CONTEXT.get();
    if (currentContext == null) {
      throw new IllegalStateException(
          "Accessing system properties outside of test lifecycle. Possibly not (correctly) added as Rule to a test or using in multi-threaded context."
      );
    }
    return currentContext;
  }

  /**
   * <p>
   * Checks if the current context is eligible for accessing the given property. It is eligible if
   * either no other context is using this property or if the current test is the owner of the
   * property.
   * </p>
   *
   * @param currentContext context to validate
   * @param name           property to validate
   * @throws IllegalStateException if current context is not eligible for accessing the given
   *                               property
   */
  private void checkPermission(@NotNull Description currentContext, @NotNull String name) {
    if (propertyOwners.containsKey(name)) {
      Description currentOwner = propertyOwners.get(name);
      if (!currentContext.equals(currentOwner)) {
        throw new IllegalStateException(
            MessageFormat.format("System property ''{0}'' currently owned by test {1}.", name,
                                 currentOwner)
        );
      }
    }
  }

  /**
   * <p>
   * Restores all properties of the current context. Checks must have been done before.
   * </p>
   *
   * @param context context for which to restore the properties
   */
  private synchronized void restorePropertiesUnchecked(@NotNull final Description context) {
    Map<String, Description>
        ownedProperties =
        new HashMap<>(
            Maps.filterValues(propertyOwners, new Predicate<Description>() {
              @Override
              public boolean apply(@Nullable Description input) {
                return context.equals(input);
              }
            }));
    for (String propertyName : ownedProperties.keySet()) {
      restorePropertyUnchecked(propertyName);
    }
  }

  /**
   * <p>
   * Stores the current context.
   * </p>
   *
   * @param description current context
   * @throws IllegalStateException iff. a current context is already set
   */
  private static void starting(@NotNull Description description) {
    Description currentContext = TEST_CONTEXT.get();
    if (currentContext == null) {
      TEST_CONTEXT.set(description);
    } else {
      throw new IllegalStateException(MessageFormat.format(
          "{0} does not have permissions in context of {1}.", description, currentContext));
    }
  }

  /**
   * <p>
   * Frees and restores all system properties claimed by the current context.
   * </p>
   *
   * @param description current context
   * @throws IllegalStateException iff. the given context does not match the current context
   */
  private void finished(@NotNull Description description) {
    Description currentContext = validatedContext();
    if (!description.equals(currentContext)) {
      throw new IllegalStateException(MessageFormat.format(
          "{0} does not have permissions in context of {1}.", description, currentContext));
    }

    try {
      restorePropertiesUnchecked(description);
    } finally {
      TEST_CONTEXT.remove();
    }
  }

}
