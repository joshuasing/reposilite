/*
 * Copyright (c) 2022 dzikoysk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    jacoco
    kotlin("jvm")
    kotlin("kapt")
    id("com.coditory.integration-test") version "1.4.4"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.gitlab.arturbosch.detekt").version("1.21.0")
}

application {
    mainClass.set("com.reposilite.ReposiliteLauncherKt")
}

dependencies {
    implementation(project(":reposilite-frontend"))
    detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.21.0")

    val kotlin = "1.7.20"
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlin")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlin")
    api("org.jetbrains:annotations:23.0.0")

    val expressible = "1.2.1"
    api("org.panda-lang:expressible:$expressible")
    api("org.panda-lang:expressible-kt:$expressible")
    testImplementation("org.panda-lang:expressible-junit:$expressible")

    val cdn = "1.14.1"
    api("net.dzikoysk:cdn:$cdn")
    api("net.dzikoysk:cdn-kt:$cdn")

    val awssdk = "2.18.6"
    implementation(platform("software.amazon.awssdk:bom:$awssdk"))
    implementation("software.amazon.awssdk:s3:$awssdk")
    testImplementation("com.amazonaws:aws-java-sdk-s3:1.12.332")

    val exposed = "0.40.1"
    implementation("org.jetbrains.exposed:exposed-core:$exposed")
    implementation("org.jetbrains.exposed:exposed-dao:$exposed")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposed")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposed")
    api("net.dzikoysk:exposed-upsert:1.0.3")
    implementation("com.zaxxer:HikariCP:5.0.1")
    // Drivers
    implementation("org.xerial:sqlite-jdbc:3.39.3.0")
    implementation("mysql:mysql-connector-java:8.0.31")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.0.8")
    implementation("org.postgresql:postgresql:42.5.0")
    implementation("com.h2database:h2:2.1.214")

    val springSecurityCrypto = "5.7.3"
    implementation("org.springframework.security:spring-security-crypto:$springSecurityCrypto")

    val ldap = "6.0.6"
    testImplementation("com.unboundid:unboundid-ldapsdk:$ldap")

    val javalin = "5.1.4-SNAPSHOT"
    api("io.javalin:javalin:$javalin")
    api("io.javalin.community.openapi:javalin-openapi-plugin:$javalin")
    kapt("io.javalin.community.openapi:openapi-annotation-processor:$javalin") { exclude(group = "ch.qos.logback") }
    api("com.reposilite.javalin-rfcs:javalin-routing:5.0.0-SNAPSHOT")
    api("io.javalin.community.ssl:ssl-plugin:5.1.3")

    val picocli = "4.7.0"
    kapt("info.picocli:picocli-codegen:$picocli")
    api("info.picocli:picocli:$picocli")

    val jackson = "2.13.4"
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson")
    implementation("com.github.victools:jsonschema-generator:4.28.0")

    val httpClient = "1.42.3"
    implementation("com.google.http-client:google-http-client:$httpClient") { exclude(group = "commons-codec", module = "commons-codec")}
    testImplementation("com.google.http-client:google-http-client-jackson2:$httpClient")

    val commonsCoded = "1.15"
    api("commons-codec:commons-codec:$commonsCoded")

    val commonsIO = "2.11.0"
    implementation("commons-io:commons-io:$commonsIO")

    val jline = "3.21.0"
    implementation("org.jline:jline:$jline")

    val jansi = "2.4.0"
    implementation("org.fusesource.jansi:jansi:$jansi")

    val journalist = "1.0.10"
    api("com.reposilite:journalist:$journalist")
    implementation("com.reposilite:journalist-slf4j:$journalist")
    implementation("com.reposilite:journalist-tinylog:$journalist")

    val tinylog = "2.5.0"
    implementation("org.tinylog:slf4j-tinylog:$tinylog")
    implementation("org.tinylog:tinylog-api:$tinylog")
    implementation("org.tinylog:tinylog-impl:$tinylog")

    val unirest = "3.13.12"
    testImplementation("com.konghq:unirest-java:$unirest")
    testImplementation("com.konghq:unirest-objectmapper-jackson:$unirest")

    val testcontainers = "1.17.5"
    testImplementation("org.testcontainers:postgresql:$testcontainers")
    testImplementation("org.testcontainers:mariadb:$testcontainers")
    testImplementation("org.testcontainers:testcontainers:$testcontainers")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainers")
    testImplementation("org.testcontainers:localstack:$testcontainers")
}

tasks.withType<ShadowJar> {
    archiveFileName.set("reposilite-${archiveVersion.get()}.jar")
    mergeServiceFiles()
    minimize {
        exclude(dependency("org.eclipse.jetty:.*"))
        exclude(dependency("org.eclipse.jetty.websocket:.*"))
        exclude(dependency("com.fasterxml.woodstox:woodstox-core:.*"))
        exclude(dependency("commons-logging:commons-logging:.*"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-reflect:.*"))
        exclude(dependency("org.jetbrains.exposed:.*"))
        exclude(dependency("org.xerial:sqlite-jdbc.*"))
        exclude(dependency("org.sqlite:.*"))
        exclude(dependency("mysql:.*"))
        exclude(dependency("org.postgresql:.*"))
        exclude(dependency("org.h2:.*"))
        exclude(dependency("com.h2database:.*"))
        exclude(dependency("org.tinylog:.*"))
        exclude(dependency("org.slf4j:.*"))
        exclude(dependency("software.amazon.awssdk:.*"))
    }
}

publishing {
    publications {
        create<MavenPublication>("bundle") {
            from(components.getByName("java"))
            artifactId = "reposilite"
            // Gradle generator does not support <repositories> section from Maven specification.
            // ~ https://github.com/gradle/gradle/issues/15932
            pom.withXml {
                val repositories = asNode().appendNode("repositories")
                project.repositories.findAll(closureOf<Any> {
                    if (this is MavenArtifactRepository && this.url.toString().startsWith("https")) {
                        val repository = repositories.appendNode("repository")
                        repository.appendNode("id", this.url.toString().replace("https://", "").replace(".", "-").replace("/", "-"))
                        repository.appendNode("url", this.url.toString())
                    }
                })
            }
        }
    }
}

tasks.register<Copy>("generateKotlin") {
    inputs.property("version", version)
    from("$projectDir/src/template/kotlin")
    into("$projectDir/src/generated/kotlin")
    filter(ReplaceTokens::class, "tokens" to mapOf("version" to version))
}

tasks.compileKotlin {
    dependsOn("generateKotlin")
}

kotlin.sourceSets.main {
    kotlin.srcDir("$projectDir/src/generated/kotlin")
}

kapt {
    arguments {
        arg("project", "${project.group}/${project.name}") // picocli requirement
    }
}

jacoco {
    toolVersion = "0.8.8"
}

tasks.test {
    extensions.configure(JacocoTaskExtension::class) {
        setDestinationFile(file("$buildDir/jacoco/jacoco.exec"))
    }

    finalizedBy("jacocoTestReport")
}

tasks.jacocoTestReport {
    reports {
        html.required.set(false)
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(file("./build/reports/jacoco/reposilite-backend-report.xml"))
    }

    executionData(fileTree(project.buildDir).include("jacoco/*.exec"))
    finalizedBy("jacocoTestCoverageVerification")
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.0".toBigDecimal()
            }
        }
        rule {
            enabled = true
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.0".toBigDecimal()
            }
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.0".toBigDecimal()
            }
            excludes = listOf()
        }
    }
}

val testCoverage by tasks.registering {
    group = "verification"
    description = "Runs the unit tests with coverage"

    dependsOn(
        ":reposilite-backend:test",
        ":reposilite-backend:integrationTest",
        ":reposilite-backend:jacocoTestReport",
        ":reposilite-backend:jacocoTestCoverageVerification"
    )

    tasks["integrationTest"].mustRunAfter(tasks["test"])
    tasks["jacocoTestReport"].mustRunAfter(tasks["integrationTest"])
    tasks["jacocoTestCoverageVerification"].mustRunAfter(tasks["jacocoTestReport"])
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config = files("$projectDir/detekt.yml")
    autoCorrect = true
}

tasks.withType<Detekt>().configureEach {
    jvmTarget = "11"
}

tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "11"
}