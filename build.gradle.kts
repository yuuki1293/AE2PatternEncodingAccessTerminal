import net.darkhax.curseforgegradle.TaskPublishCurseForge
import org.jetbrains.changelog.Changelog
import org.jetbrains.changelog.ChangelogPluginExtension
import java.text.SimpleDateFormat
import java.util.*

buildscript {
    repositories {
        maven {
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }
        mavenCentral()
    }
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7-SNAPSHOT")
    }
}

plugins {
    eclipse
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0.16,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
    id("net.darkhax.curseforgegradle") version "1.1.25"
    id("com.modrinth.minotaur") version "2.+"
    id("com.github.breadmoirai.github-release") version "2.4.1"
    id("org.jetbrains.changelog") version "2.2.1"
}

apply(plugin = "org.spongepowered.mixin")

val minecraftVersion = project.property("minecraft_version") as String
val minecraftVersionRange = project.property("minecraft_version_range") as String
val forgeVersion = project.property("forge_version") as String
val forgeVersionRange = project.property("forge_version_range") as String
val loaderVersionRange = project.property("loader_version_range") as String
val mappingChannelProp = project.property("mapping_channel") as String
val mappingVersionProp = project.property("mapping_version") as String
val ae2Version = project.property("ae2_version") as String
val jeiVersion = project.property("jei_version") as String
val emiVersion = project.property("emi_version") as String
val jadeVersion = project.property("jade_version") as String
val guidemeVersion = project.property("guideme_version") as String
val modId = project.property("mod_id") as String
val modName = project.property("mod_name") as String
val modLicense = project.property("mod_license") as String
val modVersion = project.property("mod_version") as String
val modGroupId = project.property("mod_group_id") as String
val modAuthors = project.property("mod_authors") as String
val modDescription = project.property("mod_description") as String
val versionType = project.property("version_type") as String
val curseforgeProjectId = project.property("curseforge_project_id").toString()
val modrinthProjectId = project.property("modrinth_project_id") as String
val githubReleaseRepo = project.property("github_release_repo") as String
val githubReleaseBranch = project.property("github_release_branch") as String

val changelogExtension = extensions.getByType<ChangelogPluginExtension>()
val sourceSets = the<SourceSetContainer>()
val mainSourceSet = sourceSets.named("main")

fun parserChangelog(): String {
    if (!file("CHANGELOG.md").exists()) {
        throw GradleException("publish_with_changelog is true, but CHANGELOG.md does not exist in the workspace!")
    }
    val parsedChangelog = changelogExtension.renderItem(
        changelogExtension.get(modVersion).withHeader(false).withEmptySections(false),
        Changelog.OutputType.MARKDOWN
    )
    if (parsedChangelog.isEmpty()) {
        throw GradleException("publish_with_changelog is true, but the changelog for the latest version is empty!")
    }
    return parsedChangelog
}

group = modGroupId
version = "$minecraftVersion-$modVersion"

base {
    archivesName.set(modId)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings(mappingChannelProp, mappingVersionProp)

    copyIdeResources.set(true)

    runs {
        configureEach {
            workingDirectory(project.file("run"))

            property("forge.logging.markers", "REGISTRIES")
            property("forge.logging.console.level", "debug")

            mods {
                create(modId) {
                    source(mainSourceSet.get())
                }
            }
        }

        register("client") {
            property("forge.enabledGameTestNamespaces", modId)
        }

        register("server") {
            property("forge.enabledGameTestNamespaces", modId)
            args("--nogui")
        }

        register("gameTestServer") {
            property("forge.enabledGameTestNamespaces", modId)
        }

        register("data") {
            workingDirectory(project.file("run-data"))

            args(
                "--mod", modId,
                "--all",
                "--output", file("src/generated/resources/"),
                "--existing", file("src/main/resources/")
            )
        }
    }
}

afterEvaluate {
    extensions.getByName("mixin").withGroovyBuilder {
        "config"("${modId}.mixins.json")
        "add"(sourceSets.getByName("main"), "${modId}.refmap.json")
    }
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

repositories {
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
}

dependencies {
    minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // Mandatory
    implementation(fg.deobf("appeng:appliedenergistics2-forge:$ae2Version"))
    compileOnly(fg.deobf("org.appliedenergistics:guideme:$guidemeVersion:api"))
    runtimeOnly(fg.deobf("org.appliedenergistics:guideme:$guidemeVersion"))

    // Utility
    runtimeOnly(fg.deobf("mezz.jei:jei-$minecraftVersion-forge:$jeiVersion"))
    runtimeOnly(fg.deobf("dev.emi:emi-forge:$emiVersion"))
    runtimeOnly(fg.deobf("curse.maven:jade-324717:$jadeVersion"))
}

val replaceProperties = mapOf(
    "minecraft_version" to minecraftVersion,
    "minecraft_version_range" to minecraftVersionRange,
    "forge_version" to forgeVersion,
    "forge_version_range" to forgeVersionRange,
    "loader_version_range" to loaderVersionRange,
    "mod_id" to modId,
    "mod_name" to modName,
    "mod_license" to modLicense,
    "mod_version" to modVersion,
    "mod_authors" to modAuthors,
    "mod_description" to modDescription
)

val jarTask = tasks.named<Jar>("jar")

tasks.named<Copy>("processResources") {
    inputs.properties(replaceProperties)

    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(replaceProperties + mapOf("project" to project))
    }
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifact(jarTask.get())
        }
    }
    repositories {
        maven {
            url = uri("file://${project.projectDir}/mcmodsrepo")
        }
    }
}

tasks.register<TaskPublishCurseForge>("publishCurseForge") {
    apiToken = System.getenv("CURSEFORGE_API_KEY") ?: "XXX"

    val projectId = curseforgeProjectId.toInt()
    val mainFile = upload(projectId, jarTask.get())
    mainFile.releaseType = versionType
    val changelogFile = file("changelog.txt")
    if (changelogFile.exists()) {
        mainFile.changelog = changelogFile
        mainFile.changelogType = "text"
    }
    mainFile.displayName = "${rootProject.name} ${project.version}"
    mainFile.addGameVersion(minecraftVersion)
    mainFile.addJavaVersion("Java 17")
    mainFile.addModLoader("forge")
    mainFile.addRequirement("applied-energistics-2")
}

modrinth {
    val modrinthToken = System.getenv("MODRINTH_TOKEN") ?: "XXX"
    token.set(modrinthToken)
    projectId.set(modrinthProjectId)
    versionNumber.set(project.version.toString())
    versionName.set("${rootProject.name} ${project.version}")
    versionType.set(versionType)
    uploadFile.set(jarTask.get())
    gameVersions.set(listOf(minecraftVersion))
    loaders.set(listOf("forge"))
    changelog.set(parserChangelog())
    dependencies {
        required.project("ae2")
    }
}

githubRelease {
    token(System.getenv("GITHUB_TOKEN") ?: "")
    owner("yuuki1293")
    repo(githubReleaseRepo)
    tagName("v${project.version}")
    targetCommitish(githubReleaseBranch)
    releaseName("${rootProject.name} ${project.version}")
    body(parserChangelog())
    draft.set(true)
    prerelease.set(false)
    releaseAssets(jarTask.get())
    allowUploadToExisting.set(false)
    overwrite.set(false)
    dryRun.set(false)
    apiEndpoint("https://api.github.com")
}

tasks.register("uploadAll") {
    group = "publishing"
    description = "Uploads the mod to CurseForge, Modrinth, and GitHub using configured credentials."
    dependsOn("publishCurseForge", "modrinth", "githubRelease")
}

jarTask.configure {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to modId,
                "Specification-Vendor" to modAuthors,
                "Specification-Version" to "1",
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to modAuthors,
                "Implementation-Timestamp" to SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
            )
        )
    }

    finalizedBy("reobfJar")
}

tasks.withType(JavaCompile::class.java).configureEach {
    options.encoding = "UTF-8"
}
