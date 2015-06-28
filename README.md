# <a id="top"></a>Hamcrest &mdash; Next Deed

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[License][]]

[![Circle CI][circleci-badge]][circleci-project]
[![Maven Central][mavencentral-badge]][mavencentral]
[![Apache License, Version 2.0][license-badge]][license]
[![Java Version][java-badge]][java]

Version: 0.1.2

I am a fan of Hamcrest and its logo which actually emphasizes the clever wordplay. I like the ease
of use of Hamcrest, its easy extensibility and its great approach of creating failure reports
which sometimes do not even require to set an assertion message.

*Hamcrest &mdash; Next Deed* is an extension to Hamcrest and especially its library for even
more matchers you may find useful.

The first version of *Hamcrest &mdash; Next Deed* contains especially one matcher:

\[[Top][]]

## WaitFor
 
The WaitFor matcher introduces matching for state changes which might take a while until they
become effective. It implements the wait pattern as introduced in [Haste makes waste][haste-minds].
The matcher will wait for the matched object to reach a certain state within a given timeout.

In addition, as standard matchers won't check a mutable aspect of the matched object the initial version of
*Hamcrest &mdash; Next Deed* contains a matcher which retrieves an aspect of the matched object and
hands it over to some standard Hamcrest matcher.

Thus a typical usage looks like this:

```java
ComponentUnderTest cut = ...;
State expectedState = ...;
Function<ComponentUnderTest,State> stateFunction = new Function<>() { ... };
assertThat(componentUnderTest,
           waitFor(applying(stateFunction, equalTo(expectedState)), 2, SECONDS));
```

In Java 8 this one will look even better &ndash; and would not require the preliminary function
interface which comes with the `ApplyingMatcher`.

This pattern has proven to work very well since 2012 with great success especially in UI tests where
it takes some time until a wanted state is reached.

The wait-algorithm is decelerating regarding polls to the component under test. This is a learning
when we first tried to poll very frequently that the component under test was busy answering the
polls instead of trying to reach the wanted state (in this case we were polling Solr for having
indexed a given document).

\[[Top][]]

## Side Note

*Hamcrest &mdash; Next Deed* is just another wordplay (if clever or not is up to you). Do you get
the meaning?

\[[Top][]]

## References

* [Olaf Kummer: Haste makes waste | Minds][haste-minds]
* [shields.io][]
    providing out-of-the-box badges like the license badge
* [circleci.com][]

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[Hamcrest]: <http://hamcrest.org/> "Hamcrest - Matchers that can be combined to create flexible expressions of intent"
[haste-minds]: <http://minds.coremedia.com/2012/11/29/haste-makes-waste/> "Haste makes waste | Minds"
[Trevels-2011]: <http://jedicoder.blogspot.de/2011/11/automated-gradle-project-deployment-to.html> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository"
[shields.io]: <http://shields.io/> "Shields.io: Quality metadata badges for open source projects"
[circleci.com]: <https://circleci.com/> "Continuous Integration and Deployment - CircleCI"

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[Releasing]: <./RELEASING.md> "Building Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>

<!-- Badges -->

[license]: <LICENSE.md> "Apache License, Version 2.0"
[license-badge]: <https://img.shields.io/badge/license-Apache%20License%2C%20Version%202.0-lightgrey.svg> "Apache License, Version 2.0"
[circleci-project]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed>
[circleci-badge]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed.svg?style=svg>
[java]: <http://www.oracle.com/technetwork/java/javase/downloads/index.html>
[java-badge]: <https://img.shields.io/badge/java-7-blue.svg>
[mavencentral]: <http://search.maven.org/#search|gav|1|g%3A%22com.github.mmichaelis%22%20AND%20a%3A%22hamcrest-nextdeed%22>
[mavencentral-badge]: <https://maven-badges.herokuapp.com/maven-central/com.github.mmichaelis/hamcrest-nextdeed/badge.svg>
