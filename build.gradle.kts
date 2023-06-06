import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.8.0"
    application
    `maven-publish`

}

val vv = "0.0.4"


group = "com.cool"
version = vv

repositories {
    mavenCentral()
    maven{
        setUrl("https://jitpack.io")
    }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("com.google.code.gson:gson:2.10.1")

    compileOnly("com.github.JackKing805:RtCore:0.7.4")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
//    implementation("com.github.JackKing805:RtCore:0.7.1")//todo remove
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")//todo remove
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

publishing{
    publications{
        create("maven_public",MavenPublication::class){
            groupId = "com.cool"
            artifactId = "RequestCoreDesktop"
            version = vv
            from(components.getByName("java"))
        }
    }
}
