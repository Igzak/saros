plugins {
  id("saros.gradle.eclipse.plugin")
}

val versionQualifier = ext.get("versionQualifier") as String

configurations {
    val testConfig by getting {}
    val testImplementation by getting {
        extendsFrom(testConfig)
    }
}

sarosEclipse {
    manifest = file("META-INF/MANIFEST.MF")
    excludeManifestDependencies = listOf("saros.core", "org.junit", "org.eclipse.gef")
    isAddPdeNature = true
    isCreateBundleJar = true
    isAddDependencies = true
    pluginVersionQualifier = versionQualifier
}

sourceSets {
    main {
        java.srcDirs("src", "ext-src")
        resources.srcDirs("src")
        resources.exclude("**/*.java")
    }
    test {
        java.srcDirs("test/junit")
    }
}

dependencies {
    implementation(project(":saros.core"))
    testImplementation(project(path = ":saros.core", configuration = "testing"))
}

tasks {

    jar {
        into("assets") {
            from("assets")
        }
        into("icons") {
            from("icons")
        }
        from(".") {
            include("*.properties")
            include("readme.html")
            include("plugin.xml")
            include("LICENSE")
            include("CHANGELOG")
        }
	    from(rootProject.file("saros_log4j2.xml"))
	    from(rootProject.file("log4j2.xml"))
    }

    val testJar by registering(Jar::class) {
        classifier = "tests"
        from(sourceSets["test"].output)
    }

    artifacts {
        add("testing", testJar)
    }

    /*
     * Copy the log4j2 files into the eclipse project dir
     * to make them available for PDE.
     */
    register("copyLogFiles", Copy::class) {
      into("${project.projectDir}/")
      from(rootProject.file("log4j2.xml"))
      from(rootProject.file("saros_log4j2.xml"))
    }
}
