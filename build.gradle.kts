import com.diffplug.spotless.LineEnding
import com.hypherionmc.modpublisher.plugin.ModPublisherGradleExtension
import com.hypherionmc.modpublisher.properties.CurseEnvironment
import com.hypherionmc.modpublisher.properties.ModLoader
import com.hypherionmc.modpublisher.properties.ReleaseType
import org.apache.tools.ant.filters.ReplaceTokens
import java.text.SimpleDateFormat
import java.util.*
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogPluginExtension

plugins {
    id("java")
    id("java-library")
    id("idea")

    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.moddev)
    alias(libs.plugins.modPublisher)
    alias(libs.plugins.changelog)
    alias(libs.plugins.spotless)
}

val modId = Constants.Mod.id
val mcVersion: String = libs.versions.minecraft.get()
val forgeVersion: String = libs.versions.forge.get()
val jdkVersion = 17

val exportMixin = true

val changelogExtension = extensions.getByType<ChangelogPluginExtension>()
val sourceSets = the<SourceSetContainer>()
val mainSourceSet = sourceSets.named("main")

fun parserChangelog(): String {
    if (!file("CHANGELOG.md").exists()) {
        throw GradleException("publish_with_changelog is true, but CHANGELOG.md does not exist in the workspace!")
    }
    val parsedChangelog = changelogExtension.renderItem(
        changelogExtension.get(Constants.Mod.version).withHeader(false).withEmptySections(false),
        Changelog.OutputType.MARKDOWN
    )
    if (parsedChangelog.isEmpty()) {
        throw GradleException("publish_with_changelog is true, but the changelog for the latest version is empty!")
    }
    return parsedChangelog
}

base {
    archivesName = "${project.name}-$mcVersion"
    version = Constants.Mod.version
    group = Constants.Mod.group
}

legacyForge {
    version = "$mcVersion-$forgeVersion"

    validateAccessTransformers = true

    file("src/main/resources/META-INF/accesstransformer.cfg").takeIf(File::exists)?.let {
        println("Adding access transformer: $it")
        setAccessTransformers(it)
    }

    parchment {
        mappingsVersion = libs.versions.parchmentmc.get()
        minecraftVersion = mcVersion
    }

    runs {
        register("client") {
            client()
            gameDirectory.set(file("run"))
            systemProperty("forge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=$exportMixin")
        }

        register("server") {
            server()
            gameDirectory.set(file("run-server"))
            programArgument("--nogui")
            systemProperty("forge.enabledGameTestNamespaces", modId)
            jvmArgument("-Dmixin.debug.export=$exportMixin")
        }

        register("data") {
            data()
            gameDirectory.set(file("run-data"))
            programArguments.addAll(
                "--mod",
                modId,
                "--all",
                "--output",
                file("src/generated/resources/").absolutePath,
                "--existing",
                file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")

            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create(modId) {
            sourceSet(sourceSets["main"])
        }
    }
}

mixin {
    add(sourceSets["main"], "${modId}.refmap.json")

    config("${modId}.mixins.json")
}

repositories {
    mavenCentral()
    maven {
        name = "Sponge / Mixin"
        url = uri("https://repo.spongepowered.org/repository/maven-public/")
    }
    maven {
        name = "Mod Maven"
        url = uri("https://modmaven.dev/")
        content {
            includeGroup("appeng")
            includeGroup("mezz.jei")
        }
    }
    maven {
        name = "TerraformersMC"
        url = uri("https://maven.terraformersmc.com/")
        content {
            includeGroup("dev.emi")
        }
    }
    maven {
        name = "Curse Maven"
        url = uri("https://www.cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
    maven {
        url = uri("https://maven.theillusivec4.top/")
    }
    maven {
        url = uri("https://maven.architectury.dev/")
    }
    maven {
        url = uri("https://maven.shedaniel.me/")
    }
}

dependencies {
    // Mandatory
    modImplementation(libs.ae2)
    modRuntimeOnly(libs.guideme)
    modCompileOnly(libs.jei)
    modCompileOnly(libs.emi)
    modImplementation(libs.ae2lib)
    jarJar(libs.ae2lib)
    modCompileOnly(libs.ae2wtlib)

    // Optional
    modRuntimeOnly(libs.ae2wtlib)
    modRuntimeOnly(libs.curios)         // depends on ae2wtlib
    modRuntimeOnly(libs.architectury)   // depends on ae2wtlib
    modRuntimeOnly(libs.cloth.config)   // depends on ae2wtlib

    // Utility
    modRuntimeOnly(libs.jei)
    modRuntimeOnly(libs.emi)
    modRuntimeOnly(libs.jade)

    annotationProcessor(variantOf(libs.mixin, "processor"))
}

val modDependencies = listOf(
    ModDep("forge", extractVersionSegments(forgeVersion)),
    ModDep("minecraft", mcVersion),
    ModDep("ae2", extractVersionSegments(libs.versions.ae2), ordering = Order.AFTER),
    ModDep("ae2addonlib", libs.versions.ae2lib.get(), ordering = Order.AFTER),
    ModDep("ae2wtlib", libs.versions.ae2wtlib.range.get(), ordering = Order.AFTER)
)

val generateModMetadata by tasks.registering(ProcessResources::class) {
    val replaceProperties = mapOf(
        "version" to version,
        "group" to project.group,
        "minecraft_version" to mcVersion,
        "mod_loader" to "javafml",
        "mod_loader_version_range" to "[47,)",
        "mod_name" to Constants.Mod.name,
        "mod_author" to Constants.Mod.author,
        "mod_id" to modId,
        "license" to Constants.Mod.license,
        "description" to Constants.Mod.description,
        "display_url" to Constants.Mod.repositoryUrl,
        "issue_tracker_url" to Constants.Mod.issueTrackerUrl,
        "logo_file" to "logo.png",

        "dependencies" to buildDeps(*modDependencies.toTypedArray())
    )

    inputs.properties(replaceProperties)
    from("src/main/templates") {
        filter<ReplaceTokens>("beginToken" to "\${", "endToken" to "}", "tokens" to replaceProperties)
        rename("template(\\..+)?.mixins.json", "${modId}$1.mixins.json")
    }
    into("build/generated/sources/modMetadata")

    // GuideME guide
    from("guidebook") {
        into("assets/ae2peat/guides/ae2peat/guide")
    }
}

tasks {
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release = jdkVersion
    }

    java {
        withSourcesJar()
        toolchain {
            languageVersion = JavaLanguageVersion.of(jdkVersion)
            vendor = JvmVendorSpec.JETBRAINS
        }
        JavaVersion.toVersion(jdkVersion).let {
            sourceCompatibility = it
            targetCompatibility = it
        }
    }

    processResources {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.id}" }
        }
        dependsOn(generateModMetadata)
    }

    jar {
        manifest {
            attributes(
                "Specification-Title" to Constants.Mod.name,
                "Specification-Vendor" to Constants.Mod.author,
                "Specification-Version" to version,
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to Constants.Mod.author,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date()),
                "Timestamp" to System.currentTimeMillis(),
                "Built-On-Java" to "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})",
                "Built-On-Minecraft" to mcVersion,
                "MixinConfigs" to "${modId}.mixins.json"
            )
        }
    }

    named<Jar>("sourcesJar") {
        from(rootProject.file("LICENSE")) {
            rename { "LICENSE_${Constants.Mod.id}" }
        }
    }

    named<Wrapper>("wrapper").configure {
        distributionType = Wrapper.DistributionType.BIN
    }

    named { it.startsWith("publish") }.forEach {
        it.notCompatibleWithConfigurationCache("ModPublisher plugin is not compatible with configuration cache")
    }
}

sourceSets {
    main {
        resources {
            srcDirs(
                "src/generated/resources",
                generateModMetadata.get().outputs.files
            )
            exclude("**/.cache")
        }
    }
}

legacyForge.ideSyncTask(generateModMetadata)

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true

        resourceDirs.add(file("src/main/templates"))
    }
}

fun ModPublisherGradleExtension.Dependencies.fromModDependencies(modDependencies: List<ModDep>) {
    modDependencies.filter {
        it.id != "minecraft" && it.id != "forge"
    }.forEach {
        if (it.mandatory) {
            required(it.id)
        } else {
            optional(it.id)
        }
    }
}

publisher {
    apiKeys {
        curseforge(System.getenv("CURSEFORGE_TOKEN"))
        modrinth(System.getenv("MODRINTH_TOKEN"))
        github(System.getenv("GITHUB_TOKEN"))
    }

    setReleaseType(ReleaseType.RELEASE)
    setLoaders(ModLoader.FORGE, ModLoader.NEOFORGE)
    setCurseEnvironment(CurseEnvironment.BOTH)

    curseID.set(Constants.Publisher.curseforgeProjectId)
    modrinthID.set(Constants.Publisher.modrinthProjectId)
    changelog.set(parserChangelog())
    projectVersion.set("${project.version}")
    displayName.set("[$mcVersion] v${project.version}")
    setGameVersions(mcVersion)
    setJavaVersions(jdkVersion)
    artifact.set(tasks.named("reobfJar"))
    addAdditionalFile(tasks.named("sourcesJar"))

    curseDepends {
        required("applied-energistics-2")
        optional("applied-energistics-2-wireless-terminals")
        embedded("ae2addonlib")
    }

    modrinthDepends {
        required("ae2")
        optional("applied-energistics-2-wireless-terminals")
    }

    github {
        repo("yuuki1293/ProgrammedCircuitCard")
        tag("v${mcVersion}-${project.version}")
        displayName("[$mcVersion] v${project.version}")
        createTag(true)
        createRelease(true)
        updateRelease(true)
        target("forge/${mcVersion}")
    }
}

spotless {
    java {
        target("/src/**/java/**/*.java")
        endWithNewline()
        indentWithTabs(1)
        removeUnusedImports()
        palantirJavaFormat()
        toggleOffOn()
        setLineEndings(LineEnding.UNIX)

        bumpThisNumberIfACustomStepChanges(1)
    }

    json {
        target("src/**/resources/**/*.json")
        biome()
        indentWithSpaces(2)
        endWithNewline()
        setLineEndings(LineEnding.UNIX)
    }
}
