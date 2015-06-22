# <a id="top"></a>Building

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

To debug gradle build, specify either `--debug` or `--info`.

## Local Build

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

To build locally just start the Gradle Wrapper (it will automatically retrieve the required
Gradle version for you):

```
$ ./gradlew build
```

## Uploading Artifacts

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

The deployment process of *Hamcrest &mdash; Next Deed* has been adopted according to
[Yennick Trevels' helpful hints][Trevels-2013]. Thus (unless your are not only doing a
local deployment) the first thing you need to do is to add your signing information to
`gradle.properties` located in your `.gradle` folder in your user home folder.

Just take the `gradle.properties` in this workspace as template to fill in.

### Local Deployment

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

For local deployment just call task `uploadArchives` without additional
settings and the artifacts will be uploaded to your local maven repository.

### Snapshot Deployment

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

:information_source: Snapshot deployment requires the `gradle.properties` above to be set.

To upload the artifacts after successful build to Sonatype OSS Snapshot Repository run:

```
$ ./gradlew uploadArchives -Pci
```

### Release Deployment

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

To upload the artifacts after successful build to Sonatype OSS Repository run:

```
$ ./gradlew uploadArchives -Prelease
```


## CI Build Services

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

For continuous integration *Hamcrest &mdash; Next Deed* uses several free services ranging
from CI build, coverage and dependency update checks.

### CircleCI

[CircleCI][] 

## References

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

* [Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)][Trevels-2011]

    Update: [Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)][Trevels-2013]

    And even more code copied from [GradleFx' build.gradle][GradleFx-build].

* [What is the difference between Bamboo, CircleCI, CIsimple/Ship.io, Codeship, Jenkins/Hudson, Semaphoreapp, Shippable, Solano CI, TravisCI and Wercker? - Quora (2014)][quora-cicompare]

    An interesting comparison on the different CI systems &mdash; especially the comments are worth reading.

* [Compare CircleCI vs Shippable vs Travis - Slant][slant-cicompare]

    Short list of pros and cons of these three services.

* [Maven Continuous Integration Best Practices | Sonatype Blog (2009)][sonatype-ci-best-practices]

* [Automatically Publish to Sonatype with Gradle and Travis CI | Ben Limmer (2014)][benlimmer-autopublish-sonatype]

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[Trevels-2011]: <http://jedicoder.blogspot.de/2011/11/automated-gradle-project-deployment-to.html> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)"
[Trevels-2013]: <http://yennicktrevels.com/blog/2013/10/11/automated-gradle-project-deployment-to-sonatype-oss-repository/> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2013)"
[GradleFx-build]: <https://github.com/GradleFx/GradleFx/blob/develop/build.gradle> "GradleFx/build.gradle at develop · GradleFx/GradleFx"
[CircleCI]: <http://circleci.com/> "CircleCI - Continuous Integration & Deployment"
[quora-cicompare]: <http://www.quora.com/What-is-the-difference-between-Bamboo-CircleCI-CIsimple-Ship-io-Codeship-Jenkins-Hudson-Semaphoreapp-Shippable-Solano-CI-TravisCI-and-Wercker> "What is the difference between Bamboo, CircleCI, CIsimple/Ship.io, Codeship, Jenkins/Hudson, Semaphoreapp, Shippable, Solano CI, TravisCI and Wercker? - Quora"
[slant-cicompare]: <http://www.slant.co/topics/186/compare/~circleci_vs_shippable_vs_travis> "Compare CircleCI vs Shippable vs Travis - Slant"
[sonatype-ci-best-practices]: <http://blog.sonatype.com/2009/01/maven-continuous-integration-best-practices> "Maven Continuous Integration Best Practices | Sonatype Blog (2009)"
[benlimmer-autopublish-sonatype]: <http://benlimmer.com/2014/01/04/automatically-publish-to-sonatype-with-gradle-and-travis-ci/> "Automatically Publish to Sonatype with Gradle and Travis CI | Ben Limmer (2014)"

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>
