pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        maven { url = uri("https://repo.spring.io/milestone") }
        maven { url = uri("https://repo.spring.io/snapshot") }
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "oris"

include("app")
include("user")
include("chat")
include("notification")
include("common")