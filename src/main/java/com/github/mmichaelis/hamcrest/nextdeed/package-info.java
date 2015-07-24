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

/**
 * <p>
 * <em>Hamcrest &mdash; Next Deed</em>: Matchers extending Hamcrest's library.
 * </p>
 * <p>
 * The initial reason for creating this library was to make some code available
 * for waiting for events to happen &mdash; a very common use case in integration tests and
 * more specifically in UI tests where latency of the system is always to be expected.
 * The original code base exists since 2011 and this is an extract of the core functionality
 * of the wait algorithm.
 * </p>
 * <p>
 * This branch of <em>Hamcrest &mdash; Next Deed</em> is based upon
 * <em><a href="https://github.com/google/guava">Guava: Google Core Libraries for Java</a></em>
 * which allows to use SAMs (Single Abstract Methods) and its most prominent representatives
 * (Function, Predicate, Supplier; Consumer missing though) already in Java versions prior to
 * Java 8. You might expect that at a later state <em>Hamcrest &mdash; Next Deed</em> will be
 * released with natural Java 8 support.
 * </p>
 *
 * @since 0.1.3
 */
package com.github.mmichaelis.hamcrest.nextdeed;
