import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

repositories { mavenCentral() }

dependencies {
    implementation("com.sparkjava:spark-core:2.9.4")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.5.8")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
}

application {
    mainClass.set("com.cardpayment.App")
}

tasks.test { useJUnitPlatform() }

tasks.withType<ShadowJar>().configureEach {
    archiveClassifier.set("all")
}
