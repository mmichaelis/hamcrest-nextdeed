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

import static com.google.common.reflect.Reflection.getPackageName;
import static com.google.common.reflect.Reflection.newProxy;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * <p>
 * Inspired by the blog post by Daniel Reuter in 2012 this class serves as access to resource
 * bundles backed by interfaces which again allows to use methods, typed parameters, etc. for
 * retrieving localized messages.
 * </p>
 * <p>
 * Unlike the example provided by Daniel Reuter this approach expects per package bundles
 * named {@code Bundle.properties}.
 * </p>
 * <dl>
 * <dt><strong>Usage:</strong></dt>
 * <dd>
 * <p>
 * To use this message providing class you need:
 * </p>
 * <ul>
 * <li>an interface with methods for each message, possibly with additional parameters,</li>
 * <li>
 * a file {@code Bundle.properties} (and possibly localized variants) in the very same package
 * of the interface and
 * </li>
 * <li>
 * (not required but recommended) a local class {@code Messages} in the same package which
 * encapsulates calls to this class.
 * </li>
 * </ul>
 * </dd>
 * </dl>
 *
 * @see <a href="https://blog.codecentric.de/2012/01/internationalisierung-mit-java-resourcebundle-und-kompilierabhangigkeiten/">codecentric
 * Blog: Internationalisierung mit Java ResourceBundle und Kompilierabhängigkeiten (2012-01-11 von
 * Daniel Reuter)</a>
 * @since SINCE
 */
public enum BaseMessages {
  MESSAGES;

  /**
   * The handler which maps method names to the corresponding resource bundle keys.
   *
   * @since SINCE
   */
  private static final InvocationHandler RESOLVER = new MessageResolver();
  /**
   * Resolver for testing purpose providing only raw messages showing the arguments and
   * nothing else.
   *
   * @since SINCE
   */
  private static final InvocationHandler RAW_RESOLVER = new RawResolver();
  /**
   * Base name of the bundle file.
   *
   * @since SINCE
   */
  private static final String BUNDLE_NAME = "Bundle";

  /**
   * Cache for the different proxies.
   *
   * @since SINCE
   */
  private final LoadingCache<Class<?>, Object> proxyCache =
      CacheBuilder
          .newBuilder()
          .build(
              new CacheLoader<Class<?>, Object>() {
                @Override
                public Object load(Class<?> key) throws Exception {
                  return newProxy(key, RESOLVER);
                }
              });

  public static <T> Callable<Void> withRawMessages(@NotNull final Class<T> messageClass,
                                                   final Callable<Void> delegateCallable) {
    return new Callable<Void>() {
      @Override
      public Void call() throws Exception {
        try (AutoCloseable ignored = MESSAGES.overrideAsRaw(messageClass)) {
          return delegateCallable.call();
        }
      }
    };
  }

  /**
   * Retrieve the bundle file within the context (thus path) of the given class.
   *
   * @param context context to search bundle file in
   * @return resource bundle
   * @throws java.util.MissingResourceException if resource bundle cannot be found
   * @since SINCE
   */
  @NotNull
  static ResourceBundle getBundle(@NotNull Class<?> context) {
    return ResourceBundle
        .getBundle(format("%s.%s", getPackageName(context), BUNDLE_NAME),
                   Locale.getDefault(),
                   context.getClassLoader());
  }

  /**
   * Get localized string within the given context (from the bundle in this context) with given
   * key. Possibly formatted via given arguments.
   *
   * @param context context where to locate the resource bundle
   * @param key     key within the resource bundle
   * @param args    arguments possibly added to the message
   * @return message
   * @since SINCE
   */
  @NotNull
  private static String getString(@NotNull Class<?> context, @NotNull String key, Object... args) {
    return MessageFormat.format(
        requireNonNull(getBundle(context).getString(key),
                       format("Value for key '%s' must not be null.", key)),
        args);
  }

  /**
   * Message method interface to get the proxy instance for.
   *
   * @param messageClass interface with message key methods
   * @param <T>          type of the interface
   * @return proxy for retrieving messages
   * @since SINCE
   */
  @SuppressWarnings("unchecked")
  public <T> T of(@NotNull Class<T> messageClass) {
    return (T) proxyCache.getUnchecked(messageClass);
  }

  /**
   * <p>
   * Overrides the resolver for the given message class to return only message arguments
   * as comma separated values (using {@code toString()} on the values.
   * </p>
   * <p>
   * The result should be closed afterwards to restore the message resolution for the next
   * calls.
   * </p>
   */
  public <T> AutoCloseable overrideAsRaw(@NotNull final Class<T> messageClass) {
    proxyCache.put(messageClass, newProxy(messageClass, RAW_RESOLVER));
    return new AutoCloseable() {
      @Override
      public void close() throws Exception {
        proxyCache.invalidate(messageClass);
      }
    };
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("hash", Integer.toHexString(System.identityHashCode(this)))
        .add("proxyCache", proxyCache)
        .toString();
  }

  /**
   * Resolver for messages derived from the invoked method name.
   *
   * @since SINCE
   */
  private static class MessageResolver implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      return getString(method.getDeclaringClass(), method.getName(), args);
    }
  }

  /**
   * Resolver for messages derived from the invoked method name.
   *
   * @since SINCE
   */
  private static class RawResolver implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
      return MessageFormat.format("{0}({1})", method.getName(),
                                  (args == null)
                                  ? ""
                                  : Joiner.on(',').useForNull("null").join(args)
      );
    }

  }

}
