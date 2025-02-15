plugins {
    id("java")
}

group = "com.crimson"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.slf4j:slf4j-jdk14:2.0.10")
    implementation("org.slf4j:slf4j-api:2.0.10")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.16.1")
    implementation("net.sf.json-lib:json-lib:2.4:jdk15")
    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.12")
    implementation("org.reflections:reflections:0.10.2")
    implementation("io.netty:netty-all:4.1.104.Final")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
