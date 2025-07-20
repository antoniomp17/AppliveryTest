pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AppliveryTest"
include(":app")

include(":features:device-info:domain")
include(":features:device-info:data")
include(":features:device-info:presentation")

include(":features:installed-apps:domain")
include(":features:installed-apps:data")
include(":features:installed-apps:presentation")

include(":features:device-location:domain")
include(":features:device-location:data")
include(":features:device-location:presentation")
