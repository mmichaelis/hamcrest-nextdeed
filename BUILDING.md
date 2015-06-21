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

... TODO ...

### Release Deployment

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

... TODO ...

## Release

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]


Deployment can afterwards just be done with:

```
$ gradle uploadArchives
```

## References

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

* [Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)][Trevels-2011]

    Update: [Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)][Trevels-2013]

    And even more code copied from [GradleFx's build.gradle][GradleFx-build].

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[Trevels-2011]: <http://jedicoder.blogspot.de/2011/11/automated-gradle-project-deployment-to.html> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2011)"
[Trevels-2013]: <http://yennicktrevels.com/blog/2013/10/11/automated-gradle-project-deployment-to-sonatype-oss-repository/> "Yennick Trevels: Automated Gradle project deployment to Sonatype OSS Repository (2013)"
[GradleFx-build]: <https://github.com/GradleFx/GradleFx/blob/develop/build.gradle> "GradleFx/build.gradle at develop · GradleFx/GradleFx"
[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>
