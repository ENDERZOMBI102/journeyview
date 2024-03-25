import org.jetbrains.kotlin.konan.exec.Command
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id( "com.github.johnrengelman.shadow" ) version "8.1.1"

    kotlin( "jvm" ) version "1.9.23"
    kotlin( "kapt" ) version "1.9.23"

    idea
}

val shade = configurations.create("shade")

repositories {
    mavenLocal()
    mavenCentral()
    maven( url = "https://repsy.io/mvn/enderzombi102/mc" )
}

dependencies {
    implementation( libs.bundles.impl )
    runtimeOnly( libs.bundles.runtime )
    shade( libs.bundles.shade )

    // We have to do this to make sure native libs for each platform are included
    for ( platform in listOf( "linux", "mac", "win" ) ) {
        implementation( shade( "org.openjfx:javafx-base:14:$platform" )!! )
        implementation( shade( "org.openjfx:javafx-controls:14:$platform" )!! )
        implementation( shade( "org.openjfx:javafx-fxml:14:$platform" )!! )
        implementation( shade( "org.openjfx:javafx-graphics:14:$platform" )!! )
    }
}

java.toolchain.languageVersion.set( JavaLanguageVersion.of(8) )

kapt.arguments {
    arg( "project", "${project.group}/${project.name}" )
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<Jar>("jar") {
    from( "LICENSE" ) {
        rename { "${it}_$archiveBaseName" }
    }
}

tasks.withType<ProcessResources> {
    fun gitHash(): String {
        val res = Command( "git", "rev-parse", "--short", "HEAD" )
            .also( Command::execute )
            .getResult( false )

        return if ( res.exitCode == 0 )
            res.outputLines[0]
        else
            "0nohash"
    }

    // expand and replace the `$` formatted strings inside `assets/data/info.json5`
    val date = LocalDateTime.now().format( DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss") )
    inputs.property( "version", version )
    inputs.property( "build_date", date )
    inputs.property( "commit_hash", gitHash() )

    filesMatching( "BuildInfo.properties" ) {
        expand( "version" to version, "build_date" to date, "commit_hash" to gitHash() )
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    configurations = listOf( shade )
    archiveClassifier.set( "" )

    val date = LocalDateTime.now().format( DateTimeFormatter.ofPattern("dd-MM-yyyy'T'HH:mm:ss") )

    manifest.attributes += mapOf(
        Pair( "Main-Class"              , "com.enderzombi102.jv.JourneyViewKt" ),
        Pair( "Specification-Title"     , "JourneyMap-Tools" ),
        Pair( "Specification-Vendor"    , "TeamJM" ),
        Pair( "Specification-Version"   , "1.3-SNAPSHOT" ),
        Pair( "Implementation-Title"    , "JourneyView" ),
        Pair( "Implementation-Version"  , archiveVersion ),
        Pair( "Implementation-Vendor"   , "ENDERZOMBI102" ),
        Pair( "Implementation-Timestamp", date ),
    )
}


// task used to run the application
val launch by tasks.registering( JavaExec::class ) {
    group = "application"
    description = "Runs JourneyView."
    workingDir = file( "run" ).also( File::mkdir )

    jvmArgs( "-ea" )

    // only allow enhanced hot-swapping if we're using a DCEVM-enabled JVM
    if ( System.getProperty( "java.vm.vendor" ) == "JetBrains s.r.o." )
        jvmArgs( "-XX:+AllowEnhancedClassRedefinition" )

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set( "com.enderzombi102.jv.JourneyViewKt" )
}

artifacts {
    archives( tasks["shadowJar"] )
}


