# kotlin-springboot-angular (KSA)

This skeleton is meant as a template to bootstrap new projects in our standard stack:

* Java / Kotlin
* Spring Boot
* Angular

That all backed by the [Cloudflight Platform](https://git.internal.catalysts.cc/catalysts/cloudflight-platform) and built by the
[Cloudflight Gradle Plugin](https://git.internal.catalysts.cc/catalysts/cloudflight-gradle-plugin).

## Quickstart

1. Copy this locally
2. Search for `CHANGEME` tokens and replace them with your projects information
3. Change the package (`io.cloudflight.skeletons`) to your specific package
4. Run `gradlew clean build` to check if you missed something (and fix errors if any)
5. Add your project's readme instead of this
6. Commit and push to YOUR repository
7. Happy coding! 💙
8. Maybe add yourself to the list of projects who use this. idk. just say'n.

## How to ...

### ... dev this locally

Import in IntelliJ using `gradle` and you will see 3 run configs (you should rename those):

 - `Frontend` start the angular build watch (hot-reload, will run on `:4200`)
 - `Application` start backend (will run on `:8080`)

### ... deploy to prod

Please use our [Teamcity](https://teamcity.internal.catalysts.cc) for this.
Basically run `gradlew clean build` and run the jar found under `./backend/build/libs`.

## Codestyle

Import `./idea/Catalysts codestyle.xml` to your IntelliJ and use it.
