# <a id="top"></a>Releasing

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[License][]]

*Hamcrest &mdash; Next Deed* uses [axion-release-plugin][] to perform release via Gradle. It
provides an easy to use *auto-versioning* mechanism which derives the version from tags rather
than storing it in any repository files.

It is recommended to read through the
[documentation of the plugin][axion-release-plugin-documentation] but for convenience you will
find common usages within *Hamcrest &mdash; Next Deed* below.

\[[Top][]]

## Requirements

In order to work with the [axion-release-plugin][] you must copy the provided
[gradle.properties][] to your user home folder's `.gradle` sub-folder and fill in the
credentials and keys.

## Basic Usage

* determine current version

    ```
    $ ./gradlew currentVersion
    ```

* dry run release

    ```
    ./gradlew release -Prelease.dryRun [-Prelease.disableChecks]
    ```

* release with next incremental version (see [7 Understanding Maven Version Numbers][version-numbers])

    ```
    $ ./gradlew release
    ```

    As pre-release hook the following tokens are replaced (and committed afterwards):
    
    * `README.md`: `Version: x.y.z` is replaced by current version
    * `**/*.java`: pattern `SINCE` is replaced by current version

* publish release

    Currently to publish the release use:
    
    ```
    $ ./gradlew uploadArchives
    ```

    *Note:* `uploadArchives` will automatically determine by the evaluated version if this will
    be a snapshot release or a normal release. If you want to deploy a (snapshot) release only
    to your local maven repository add the property `local` to your Gradle run:
    
    ```
    $ ./gradlew uploadArchives -Plocal
    ```
    
    As soon as the `maven-publish` plugin left the incubator this might be replaced with:
    
    ```
    $ ./gradlew publish
    ```
    
* change to another minor or version number

    ```
    $ ./gradlew markNextVersion -Prelease.nextVersion=1.0.0
    ```

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

<!-- Links -->

[axion-release-plugin]: <https://github.com/allegro/axion-release-plugin> "allegro/axion-release-plugin"
[axion-release-plugin-documentation]: <http://axion-release-plugin.readthedocs.org/en/latest/> "axion-release-plugin — axion-release-plugin latest documentation"
[version-numbers]: <https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN400> "7 Understanding Maven Version Numbers"

[gradle.properties]: <./gradle.properties>

<!-- Navigation -->

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[Releasing]: <./RELEASING.md> "Building Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>
