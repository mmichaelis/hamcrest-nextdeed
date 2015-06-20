# Hamcrest &mdash; Next Deed

[![Circle CI][circleci-badge]][circle-ci]

I am a fan of Hamcrest and its logo which actually emphasizes the clever wordplay. I like the ease
of use of Hamcrest, its easy extensibility and its great approach of creating failure reports
which sometimes do not even require to set an assertion message.

*Hamcrest &mdash; Next Deed* is an extension to Hamcrest and especially its library for even
more matchers you may find useful.

The first version of *Hamcrest &mdash; Next Deed* contains especially one matcher:
 
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

## Release

The release process of *Hamcrest &mdash; Next Deed* has been adopted according to
[Yennick Trevels' helpful hints][Trevels-2011]. Thus the first thing you need to do is
to add your signing information to `gradle.properties` located in your `.gradle` folder
in your user home folder.

Deployment can afterwards just be done with:

```
$ gradle uploadArchives
```

## Side Note

*Hamcrest &mdash; Next Deed* is just another wordplay (if clever or not is up to you). Do you get
the meaning?

## References

* [Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository][Trevels-2011]

<!-- Links -->

[Hamcrest]: <http://hamcrest.org/> "Hamcrest - Matchers that can be combined to create flexible expressions of intent"
[haste-minds]: <http://minds.coremedia.com/2012/11/29/haste-makes-waste/> "Haste makes waste | Minds"
[Trevels-2011]: <http://jedicoder.blogspot.de/2011/11/automated-gradle-project-deployment-to.html> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository"

<!-- Badges -->

[circleci]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed>
[circleci-badge]: <https://circleci.com/gh/mmichaelis/hamcrest-nextdeed.svg?style=svg>
