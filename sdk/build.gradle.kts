import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
plugins {
    id("maven-publish")
    id("signing")
}

group = rootProject.group
version = rootProject.version

val javaVersion = JavaVersion.current()
val ossrhUsername = System.getenv("OSSRH_USERNAME") ?: properties["ossrhUsername"] as String?
val ossrhPassword = System.getenv("OSSRH_PASSWORD") ?: properties["ossrhPassword"] as String?

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("dev.dejvokep:boosted-yaml:1.3")
    implementation("com.google.code.gson:gson:2.10.1")
    compileOnly("com.google.guava:guava:30.1.1-jre")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    maxHeapSize = "1G"

    testLogging {
        events("passed")
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked"))
}

// auto minimize shadowjar
tasks.withType<ShadowJar> {
    minimize()
}

java {
    withSourcesJar()
    withJavadocJar()
}

val sdkJar by tasks.registering(Jar::class) {
    archiveClassifier.set("")
    dependsOn("shadowJar")
}

publishing {
    publications {
        create<MavenPublication>("sdk") {
            from(components["java"])

            pom {
                name.set("Tebex SDK")
                description.set("The official Java SDK for Tebex.")
                url.set("https://github.com/tebexio/BuycraftX")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://github.com/track/plugin/blob/main/LICENSE")
                    }
                }

                scm {
                    url.set("https://github.com/track/plugin")
                    connection.set("scm:https://github.com/track/plugin.git")
                    developerConnection.set("scm:git@github.com:track/plugin.git")
                }

                developers {
                    developer {
                        id.set("charliejoseph")
                        name.set("Charlie Joseph")
                        email.set("charlie.joseph@overwolf.com")
                    }
                }
            }
        }
    }
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
}

signing {
    sign(publishing.publications["sdk"])
}