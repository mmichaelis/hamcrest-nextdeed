package com.github.mmichaelis.hamcrest.nextdeed.util;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;
import java.util.ResourceBundle;

import static com.github.mmichaelis.hamcrest.nextdeed.util.ResourceBundleContainsKey.resourceBundleContainsKey;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.Assert.assertThat;

/**
 * Tests {@link ResourceBundleContainsKey}.
 */
public class ResourceBundleContainsKeyTest {

  private ResourceBundle bundle;

  @Before
  public void setUp() throws Exception {
    bundle = ResourceBundle.getBundle(
        this.getClass().getName(),
        Locale.ROOT
    );
  }

  @Test
  public void passesForAvailableKey() throws Exception {
    assertThat(bundle, resourceBundleContainsKey("keyAndValue"));
  }

  @Test
  public void failsForUnavailableKey() throws Exception {
    assertThat(bundle, Matchers.not(resourceBundleContainsKey("unavailableKey")));
  }

  @Test
  public void toStringProvidesInformation() throws Exception {
    Matcher<ResourceBundle> matcher = resourceBundleContainsKey("keyAndValue");
    assertThat(matcher, hasToString(
        containsString("keyAndValue")
    ));
  }
}
