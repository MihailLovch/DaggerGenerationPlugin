plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.21"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "ru.generate.dagger"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    localPath = "/Users/mihail/Applications/Android Studio.app/Contents"
    plugins.set(listOf("android","Kotlin"))
}

dependencies {
    implementation("com.google.code.gson:gson:2.8.8")
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
    patchPluginXml {
        sinceBuild.set("231")
        untilBuild.set("241.*")
    }
    runIde {
        ideDir.set(file("/Users/mihail/Applications/Android Studio.app/Contents"))
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}
