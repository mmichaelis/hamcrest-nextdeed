# <a id="top"></a>Hamcrest &mdash; Next Deed

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]

[![Circle CI][circleci-badge]][circleci-project]
[![Maven Central][mavencentral-badge]][mavencentral]
[![Coverage Status][coveralls-badge]][coveralls-project]
[![Dependency Status][versioneye-badge]][versioneye-project]
[![Project Status][stillmaintained-badge]][stillmaintained-url]
[![Apache License, Version 2.0][license-badge]][license]
[![Java Version][java-badge]][java]

I am a fan of [Hamcrest][] and its logo which actually emphasizes the clever wordplay. I like the
ease of use of [Hamcrest][], its easy extensibility and its great approach of creating failure
reports which sometimes do not even require to set an assertion message.

*Hamcrest &mdash; Next Deed* is an extension to Hamcrest and especially its library for even
more matchers you may find useful.

The first version of *Hamcrest &mdash; Next Deed* was dedicated to a strategy to wait for the expected value for a certain amount of time and was inspired by an algorithm used successfully for several years now at [CoreMedia][] especially but not only for UI tests. The pattern has shown that it is quite common, especially for any integration test, that you might have to wait and that you might to take some surprises into account as mentioned in the blog post [Haste makes waste][haste-minds].

\[[Top][]]

## Available Matchers

\[[Top][]]

### Probe &mdash;  Waiting For State

*Probe* is the result of the wait pattern adopted for general use with Hamcrest. A typical example looks like this:

```java
Probe.<System, State>probing(system)
     .withinMs(1L)
     .assertThat(
         new Function<System, State>() {
           @Override
           public State apply(System input) {
             return input.getState();
           }
         },
         equalTo(State.RUNNING)
     );
```

The algorithm ensures that the system has enough time to actually reach this state.

The API makes use of [Guava: Google Core Libraries for Java][Guava] to make Java 8 features (functions, predicates, etc.) already available for Java 7.

\[[Top][]]

### Applying Matcher &mdash; Transform Before Comparison

A simpler approach than waiting is that you have an object to run assertions on where you have no matcher at hand. For example &ndash; as above &ndash; the state of a system. The example above using the *applying matcher* which does not wait:

```java
assertThat(system,
           applying(
               new Function<System, State>() {
                 @Override
                 public State apply(System input) {
                   return input.getState();
                 }
               },
               equalTo(State.RUNNING)
           )
);
```

In this example it is a little bit of overhead as you might directly do the assertion on `system.getState()`. You still might find this one useful as in this example:

```java
expectedException.expectCause(allOf(
    Matchers.<Throwable>instanceOf(AssertionError.class),
    applying((input) -> input.getMessage(),
             allOf(
                 containsString("lorem"),
                 containsString("ipsum")
             ))
));
```

\[[Top][]]

### Reflection Matchers

The following shows some of the reflection matchers which might be used to validate code style for utility classes &ndash; and in the same run ensures some more code coverage:

```java
@Test
public void probeIsUtilityClass() throws Exception {
  errorCollector.checkThat("Class must be final.",
                           Probe.class,
                           classModifierContains(Modifier.FINAL));
  errorCollector.checkThat("Any constructors must be private.",
                           asList(Probe.class.getDeclaredConstructors()),
                           everyItem(memberModifierContains(Modifier.PRIVATE)));
  assertThat("Default constructor must exist.",
             Probe.class,
             isInstantiableViaDefaultConstructor());
}
```

\[[Top][]]

## Side Note

*Hamcrest &mdash; Next Deed* is just another wordplay (if clever or not is up to you). Do you get
the meaning?

\[[Top][]]

## References

* [Olaf Kummer: Haste makes waste | Minds][haste-minds]
* [shields.io][] &mdash; providing out-of-the-box badges like the license badge
* [circleci.com][]

\[[Top][]]

## Bookmarks

* [StackEdit][stackedit] &mdash; online Markdown Editor

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[Hamcrest]: <http://hamcrest.org/> "Hamcrest - Matchers that can be combined to create flexible expressions of intent"
[haste-minds]: <http://minds.coremedia.com/2012/11/29/haste-makes-waste/> "Haste makes waste | Minds"
[Trevels-2011]: <http://jedicoder.blogspot.de/2011/11/automated-gradle-project-deployment-to.html> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository"
[shields.io]: <http://shields.io/> "Shields.io: Quality metadata badges for open source projects"
[circleci.com]: <https://circleci.com/> "Continuous Integration and Deployment - CircleCI"
[stackedit]: <https://stackedit.io/> "StackEdit – Editor"
[CoreMedia]: <http://www.coremedia.com/> "CoreMedia"
[Guava]: <https://github.com/google/guava> "Guava: Google Core Libraries for Java"

<!-- Project Links -->

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[Releasing]: <./RELEASING.md> "Building Hamcrest Next Deed"
[Javadoc]: <//mmichaelis.github.io/hamcrest-nextdeed/> "Javadoc for Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Pages]: <http://mmichaelis.github.io/hamcrest-nextdeed>
[Top]: <#top>

<!-- Badges -->

[circleci-project]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed>
[circleci-badge]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed.svg?style=shield>
[coveralls-project]: <https://coveralls.io/r/mmichaelis/hamcrest-nextdeed?branch=master>
[coveralls-badge]: <https://coveralls.io/repos/mmichaelis/hamcrest-nextdeed/badge.svg?branch=master>
[java]: <https://www.oracle.com/technetwork/java/javase/downloads/index.html>
[java-badge]: <https://img.shields.io/badge/java-7-blue.svg>
[license]: <./LICENSE.md> "Apache License, Version 2.0"
[license-badge]: <https://img.shields.io/badge/license-Apache%20License%2C%20Version%202.0-lightgrey.svg> "Apache License, Version 2.0"
[mavencentral]: <https://search.maven.org/#search|gav|1|g%3A%22com.github.mmichaelis%22%20AND%20a%3A%22hamcrest-nextdeed%22>
[mavencentral-badge]: <https://maven-badges.herokuapp.com/maven-central/com.github.mmichaelis/hamcrest-nextdeed/badge.svg>
[stillmaintained-url]: <https://stillmaintained.com/mmichaelis/hamcrest-nextdeed>
[stillmaintained-badge]: <http://stillmaintained.com/mmichaelis/hamcrest-nextdeed.png>
[versioneye-project]: <https://www.versioneye.com/user/projects/55981413616634002100003e>
[versioneye-badge]: <https://www.versioneye.com/user/projects/55981413616634002100003e/badge.svg?style=flat>
