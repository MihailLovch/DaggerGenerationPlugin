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
    localPath = "C:\\Users\\MihailLovch\\AppData\\Local\\JetBrains\\Toolbox\\apps\\AndroidStudio\\ch-0\\232.10300.40.2321.11567975"
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
        ideDir.set(file("C:\\Users\\MihailLovch\\AppData\\Local\\JetBrains\\Toolbox\\apps\\AndroidStudio\\ch-0\\232.10300.40.2321.11567975"))
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
