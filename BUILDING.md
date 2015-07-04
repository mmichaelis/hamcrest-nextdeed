# <a id="top"></a>Building

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]

To debug gradle build, specify either `--debug` or `--info`.

\[[Top][]]

## Local Build

To build locally just start the Gradle Wrapper (it will automatically retrieve the required
Gradle version for you):

```
$ ./gradlew build
```

\[[Top][]]

## Uploading Artifacts

The deployment process of *Hamcrest &mdash; Next Deed* has been adopted according to
[Yennick Trevels' helpful hints][Trevels-2013]. Thus (unless your are not only doing a
local deployment) the first thing you need to do is to add your signing information to
`gradle.properties` located in your `.gradle` folder in your user home folder.

Just take the `gradle.properties` in this workspace as template to fill in.

\[[Top][]]

## CI Build Services

For continuous integration *Hamcrest &mdash; Next Deed* uses several free services ranging
from CI build, coverage and dependency update checks.

### CircleCI

[CircleCI][] 

For CircleCI it is important to disable `axion-release-plugin` as you will otherwise get an error
when `axion-release-plugin` tries to determine the version. To do so set environment property
`CI_BUILD`.

\[[Top][]]

## References

* [What is the difference between Bamboo, CircleCI, CIsimple/Ship.io, Codeship, Jenkins/Hudson, Semaphoreapp, Shippable, Solano CI, TravisCI and Wercker? - Quora (2014)][quora-cicompare]

    An interesting comparison on the different CI systems &mdash; especially the comments are worth reading.

* [Compare CircleCI vs Shippable vs Travis - Slant][slant-cicompare]

    Short list of pros and cons of these three services.

* [Maven Continuous Integration Best Practices | Sonatype Blog (2009)][sonatype-ci-best-practices]

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[CircleCI]: <http://circleci.com/> "CircleCI - Continuous Integration & Deployment"
[quora-cicompare]: <http://www.quora.com/What-is-the-difference-between-Bamboo-CircleCI-CIsimple-Ship-io-Codeship-Jenkins-Hudson-Semaphoreapp-Shippable-Solano-CI-TravisCI-and-Wercker> "What is the difference between Bamboo, CircleCI, CIsimple/Ship.io, Codeship, Jenkins/Hudson, Semaphoreapp, Shippable, Solano CI, TravisCI and Wercker? - Quora"
[slant-cicompare]: <http://www.slant.co/topics/186/compare/~circleci_vs_shippable_vs_travis> "Compare CircleCI vs Shippable vs Travis - Slant"
[sonatype-ci-best-practices]: <http://blog.sonatype.com/2009/01/maven-continuous-integration-best-practices> "Maven Continuous Integration Best Practices | Sonatype Blog (2009)"

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[Releasing]: <./RELEASING.md> "Building Hamcrest Next Deed"
[Javadoc]: <//mmichaelis.github.io/hamcrest-nextdeed/> "Javadoc for Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>
