import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.codehaus.plexus.util.Os

plugins {
    kotlin("jvm") version "2.3.20"
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaper)
    alias(libs.plugins.pluginYml)
}

kotlin {
    jvmToolchain(16)
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly(libs.placeholderApi)
    compileOnly(libs.commandApi)
    compileOnly(libs.nbtApi)
    compileOnly(libs.worldEdit)
    compileOnly(libs.worldGuard)
    compileOnly(libs.abstractMenus)
    compileOnly(libs.storm)
    compileOnly(libs.antiRelog)

    implementation(project(":api"))
    implementation(libs.guice)
    implementation(libs.enhancedLegacy)
    implementation(libs.adventureMini)
    implementation(libs.adventureBukkit)
    implementation(libs.adventurePlain)

    implementation(libs.wutilsConfig)
    implementation(libs.wutilsConfigurables)
    implementation(libs.wutilsLog)
    implementation(libs.wutilsI18nKotlin)
    implementation(libs.wutilsCommonKotlin)
    implementation(libs.wutilsAnimation)
    implementation(libs.wutilsJson)
}

tasks {
    val isDebug = findProperty("debug")?.toString()?.toBoolean() ?: false

    shadowJar {
        archiveBaseName.set(findProperty("name").toString())
        archiveClassifier.set("")
        minimize()
        if (!isDebug) {
            relocate("com.google.inject", "org.bigcraft.customitems.shadow.google.guice")
            relocate("com.google.common", "org.bigcraft.customitems.shadow.google.common")
            relocate("net.kyori", "org.bigcraft.customitems.shadow.net.kyori")
            relocate("dev.vankka", "org.bigcraft.customitems.shadow.dev.vankka")
            relocate("me.wyne.wutils", "org.bigcraft.customitems.shadow.wutils")
        }
    }

    runServer {
        val minecraftVersion: String = if (Os.isFamily(Os.FAMILY_WINDOWS) || isDebug) "1.19.4" else "1.16.5"
        val viaVersion = "5.7.2"
        val commandApiVersion = "9.4.2"
        downloadPlugins {
            // https://dev.bukkit.org/projects/worldedit/files/4793142/download
            // https://dev.bukkit.org/projects/worldguard/files/3342964/download
            url("https://download.luckperms.net/1620/bukkit/loader/LuckPerms-Bukkit-5.5.32.jar")
            github("PlaceholderAPI", "PlaceholderAPI", "2.12.2", "PlaceholderAPI-2.12.2.jar")
            github("dmulloy2", "ProtocolLib", "5.4.0", "ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", viaVersion, "ViaVersion-$viaVersion.jar")
            github("ViaVersion", "ViaBackwards", viaVersion, "ViaBackwards-$viaVersion.jar")
            github("CommandAPI", "CommandAPI", commandApiVersion, "CommandAPI-$commandApiVersion.jar")
            modrinth("nbtapi", "2.15.0")
        }
        minecraftVersion(minecraftVersion)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
    }
}

tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
    javaLauncher = javaToolchains.launcherFor {
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-DPaper.IgnoreJavaVersion=true")
}

bukkit {
    name = findProperty("name").toString()
    version = getVersion().toString()
    website = findProperty("website").toString()
    author = findProperty("author").toString()
    main = "org.bigcraft.customitems.CustomItems"
    apiVersion = "1.16"
    softDepend = listOf("PlaceholderAPI", "CommandAPI", "NBTAPI", "CustomEnchants", "WorldEdit", "WorldGuard", "AbstractMenus", "Storm", "AntiRelog")
    permissions {
        register("customitems.*") {
            children = listOf("customitems.reload", "customitems.give")
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("customitems.reload") {
            description = "Allows to reload plugin"
        }
        register("customitems.give") {
            description = "Allows to give custom items"
        }
    }
}

publishing {
    repositories {
        val repoUrl = findProperty("myMavenRepoWriteUrl").toString()
        if (repoUrl.isNotEmpty()) {
            maven {
                url = uri(repoUrl)

                credentials {
                    username = findProperty("myMavenRepoWriteUsername").toString()
                    password = findProperty("myMavenRepoWritePassword").toString()
                }
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = findProperty("group").toString()
            artifactId = "CustomItems-core"
            version = findProperty("version").toString()

            from(components["java"])
        }
    }
}