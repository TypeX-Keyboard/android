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
            url = uri("https://maven.pkg.github.com/trustwallet/wallet-core")
            credentials {
                val localProperties = java.util.Properties()
                val localPropertiesFile = file("local.properties")
                if (localPropertiesFile.exists()) {
                    localPropertiesFile.inputStream().use { stream ->
                        localProperties.load(stream)
                    }
                }

                username = localProperties.getProperty("github.username") ?: ""
                password = localProperties.getProperty("github.token") ?: ""
            }
        }
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.squareup.okhttp3:okhttp:4.12.0")
        classpath("com.google.code.gson:gson:2.10.1")
    }
}

rootProject.name = "TypeX-Hackathon"
include(":app")
 