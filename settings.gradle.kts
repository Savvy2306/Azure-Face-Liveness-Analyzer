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
        maven {
            url = uri("https://pkgs.dev.azure.com/msface/SDK/_packaging/AzureAIVision/maven/v1")
            name = "AzureAIVision"
            credentials {
                username = "enter your username here"
                password = "enter your password here"
            }
        }
    }
}

rootProject.name = "AzureFaceAnalyzerSample"
include(":app")
 