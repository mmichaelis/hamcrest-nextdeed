# <a id="top"></a>Releasing

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]

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
credentials and keys. Mind that you generate a personal access token for GitHub access. 

## Basic Usage

* **determine current version**

    ```
    $ ./gradlew currentVersion
    ```

* **dry run release**

    ```
    ./gradlew release -Prelease.dryRun [-Prelease.disableChecks]
    ```

    Mind that also the dry run already does some file replacement (SINCE keyword replaced by
    current version). So you might use this to do the replacement right before the release
    and commit the change.
    
* **release increment**

    Release the next incremental version (see [7 Understanding Maven Version Numbers][version-numbers]):

    ```
    $ ./gradlew release
    ```

    As pre-release hook the following tokens are replaced (and committed afterwards):
    
    * `README.md`: `Version: x.y.z` is replaced by current version
    * `**/*.java`: pattern `SINCE` is replaced by current version

* **publish release**

    To publish the release just use:
    
    ```
    $ ./gradlew uploadArchives publishGhPages
    ```

    `publishGhPages` will upload [Javadoc][]. 

    - - -

    > **Note:** `uploadArchives` will automatically determine by the evaluated version if this will
    be a snapshot release or a normal release. If you want to deploy a (snapshot) release only
    to your local maven repository add the property `local` to your Gradle run:
    >
    > ```
    > $ ./gradlew uploadArchives -Plocal
    > ```
    
    - - -
    
    > **Future:** As soon as the `maven-publish` plugin left the incubator this might be replaced with:
    >
    > ```
    > $ ./gradlew publish
    > ```
        
* **Optional:** *change to another minor or version number*

    ```
    $ ./gradlew markNextVersion -Prelease.nextVersion=1.0.0
    ```

\[[Home][]]&nbsp;\[[Building][]]&nbsp;\[[Releasing][]]&nbsp;\[[Javadoc][]]&nbsp;\[[License][]]&nbsp;\[[Top][]]

## References

* [Sonatype Nexus Professional][sonatype-nexus]

    Staging repository. Use to finish release.
 
<!-- Links -->

[axion-release-plugin]: <https://github.com/allegro/axion-release-plugin> "allegro/axion-release-plugin"
[axion-release-plugin-documentation]: <http://axion-release-plugin.readthedocs.org/en/latest/> "axion-release-plugin — axion-release-plugin latest documentation"
[version-numbers]: <https://docs.oracle.com/middleware/1212/core/MAVEN/maven_version.htm#MAVEN400> "7 Understanding Maven Version Numbers"
[sonatype-nexus]: <https://oss.sonatype.org/> "Sonatype Nexus Professional"

[gradle.properties]: <./gradle.properties>

<!-- Navigation -->

[Home]: <./README.md> "Home"
[Building]: <./BUILDING.md> "Building Hamcrest Next Deed"
[Releasing]: <./RELEASING.md> "Building Hamcrest Next Deed"
[Javadoc]: <//mmichaelis.github.io/hamcrest-nextdeed/> "Javadoc for Hamcrest Next Deed"
[License]: <./LICENSE.md> "License of Hamcrest Next Deed"
[Top]: <#top>
