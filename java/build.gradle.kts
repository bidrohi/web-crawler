plugins {
    id("java")
}

group = "com.bidyut.tech.crawler"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
    implementation("org.jsoup:jsoup:1.15.3")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}

task("run", JavaExec::class) {
    main = "${project.group}.Main"
    classpath = sourceSets["main"].runtimeClasspath
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
