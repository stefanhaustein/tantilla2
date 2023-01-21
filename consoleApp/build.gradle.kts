plugins {
    kotlin("jvm")
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_7
    targetCompatibility = JavaVersion.VERSION_1_7
}

repositories {
    mavenCentral()
    mavenLocal()
}



dependencies {
    implementation(project(":core"))
    implementation("org.kobjects.konsole:core:0.3.0")
    implementation("org.kobjects.parserlib:core:0.6.0'")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass.set("org.kobjects.tantilla2.console.TantillaConsoleKt")
}