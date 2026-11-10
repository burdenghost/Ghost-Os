pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}

rootProject.name = "GhostOS"

val runtimeRoot = file("third_party/pr/android")
include(":app", ":proot-engine", ":session-core", ":termlib")
project(":proot-engine").projectDir = file("$runtimeRoot/proot-engine")
project(":session-core").projectDir = file("$runtimeRoot/session-core")
project(":termlib").projectDir = file("$runtimeRoot/termlib")
