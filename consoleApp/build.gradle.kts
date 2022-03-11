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
    implementation("org.kobjects.parserlib:core:0.2.1")
    implementation("org.kobjects.greenspun:core:0.1.3")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass.set("org.kobjects.tantilla2.console.TantillaConsoleKt")
}