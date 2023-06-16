import com.google.protobuf.gradle.id

plugins {
    // Apply the application plugin to add support for building a CLI application in Java.
    java
    application
    id("com.google.protobuf") version "0.9.3"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val grpcVersion = "1.56.0"
val protobufVersion = "3.22.3"
val protocVersion = protobufVersion
val helidonVersion = "4.0.0-ALPHA6"

repositories {
    maven { // The google mirror is less flaky than mavenCentral()
        url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
    }
    mavenCentral()
    mavenLocal()
}

java {                                      
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
    }
}

dependencies {
    implementation("com.google.protobuf:protobuf-java:$protobufVersion")
    implementation(platform("io.grpc:grpc-bom:$grpcVersion"))
    implementation("io.grpc:grpc-netty-shaded:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-stub:$grpcVersion")

    implementation("javax.annotation:javax.annotation-api:1.3.2")

    // Use TestNG framework, also requires calling test.useTestNG() below
    testImplementation("org.testng:testng:7.4.0")


    // This dependency is used by the application.
    implementation("com.google.guava:guava:30.1.1-jre")
}

sourceSets {
    main {
        java {
            srcDir("build/generated/source/proto/main/java")
        }
    }
}

    protobuf {
        protoc {
            artifact = "com.google.protobuf:protoc:${protocVersion}"
        }
        plugins {
            id("grpc") {
                artifact = "io.grpc:protoc-gen-grpc-java:${grpcVersion}"
            }
        }
        generateProtoTasks {
            all().forEach {
                it.plugins {
                    id("grpc")
                }
            }
        }
    }


application {
    mainClass.set("io.github.efagerho.loom.Benchmark")
}

tasks {
    jar {
    }

    withType<JavaCompile>() {
        options.compilerArgs.add("--enable-preview")
        options.compilerArgs.add("-Xlint:preview")
        options.release.set(20)
    }

    named<JavaExec>("run") {
        jvmArgs = listOf(
            "--enable-preview"
        )
        args = listOf(
            "run",
            "io.github.efagerho.loom.Benchmark",
        )
    }
    named<Test>("test") {
        useTestNG()
    }
}
