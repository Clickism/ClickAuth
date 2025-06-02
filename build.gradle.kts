plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.5"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "de.clickism"
val pluginVersion = property("plugin_version").toString()
version = pluginVersion

repositories {
    mavenCentral()
    mavenLocal()
    maven {
        name = "spigotmc-repo"
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:26.0.2")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("at.favre.lib:bcrypt:0.10.2")
    // Configuration & Localization
    implementation("de.clickism:configured-core:0.2.2")
    implementation("de.clickism:configured-yaml:0.2.2")
    implementation("de.clickism:configured-json:0.2.2")
    implementation("de.clickism:configured-localization:0.2.2")
}

tasks.runServer {
    minecraftVersion("1.21.5")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

tasks.jar {
    enabled = false
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    isEnableRelocation = true
    relocationPrefix = "de.clickism.clickauth.shadow"
}

val targetJavaVersion = 17
java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.processResources {
    val props = mapOf(
        "version" to pluginVersion
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}