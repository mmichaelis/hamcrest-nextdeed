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

package com.github.mmichaelis.hamcrest.nextdeed.concurrent;

import com.google.common.base.MoreObjects;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of event when a WaitFunction times out.
 *
 * @since SINCE
 */
public class WaitTimeoutEventImpl<T, R> implements WaitTimeoutEvent<T, R> {

  private final WaitFunction<T, R> source;
  private final long consumedMs;
  private final T item;
  private final R lastResult;

  public WaitTimeoutEventImpl(@NotNull WaitFunction<T, R> source,
                              long consumedMs,
                              T item,
                              R lastResult) {
    this.source = source;
    this.consumedMs = consumedMs;
    this.item = item;
    this.lastResult = lastResult;
  }

  @Override
  @NotNull
  public WaitFunction<T, R> getSource() {
    return source;
  }

  @Override
  public long getConsumedMs() {
    return consumedMs;
  }

  @Override
  public T getItem() {
    return item;
  }

  @Override
  public R getLastResult() {
    return lastResult;
  }

  @Override
  @NotNull
  public String describe() {
    WaitFunction<T, R> source = getSource();
    return String.format(
        "%s applied to %s did not fulfill %s within %d %s (consumed %d %s) but was: %s",
        source.getDelegateFunction(),
        getItem(),
        source.getPredicate(),
        source.getTimeout(),
        source.getTimeoutTimeUnit().toString().toLowerCase(Locale.ROOT),
        getConsumedMs(),
        TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.ROOT),
        getLastResult()
    );
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("consumedMs", consumedMs)
        .add("item", item)
        .add("lastResult", lastResult)
        .add("source", source)
        .toString();
  }
}
