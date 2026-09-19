import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.codehaus.plexus.util.Os

plugins {
    kotlin("jvm") version "2.4.20"
    alias(libs.plugins.shadow)
    alias(libs.plugins.runPaperFork)
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
    compileOnly(libs.lootPool)

    implementation(project(":api"))
    implementation(libs.guice)
    implementation(libs.enhancedLegacy)
    implementation(libs.adventureMini)
    implementation(libs.adventureBukkit)
    implementation(libs.adventurePlain)

    implementation(libs.wutilsConfig)
    implementation(libs.wutilsConfigurables)
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
        val viaVersion = "5.12.0"
        val commandApiVersion = "9.4.3"
        downloadPlugins {
            url("https://download.luckperms.net/1672/bukkit/loader/LuckPerms-Bukkit-5.5.85.jar")
            github("PlaceholderAPI", "PlaceholderAPI", "2.12.3", "PlaceholderAPI-2.12.3.jar")
            github("dmulloy2", "ProtocolLib", "5.4.0", "ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", viaVersion, "ViaVersion-$viaVersion.jar")
            github("ViaVersion", "ViaBackwards", viaVersion, "ViaBackwards-$viaVersion.jar")
            github("NeverMined-Entertainment", "CommandAPI", commandApiVersion, "CommandAPI-$commandApiVersion.jar")
            github("Wyne10", "ConnectionSource-public", "2.0.0", "ConnectionSource-2.0.0.jar")
            github("Wyne10", "InfPoints-public", "3.0.0", "InfPoints-3.0.0.jar")
            github("Wyne10", "Storm-public", "1.2.0", "Storm-1.2.0.jar")
            github("Wyne10", "LootPool-public", "2.8.4", "LootPool-2.8.4.jar")
            modrinth("nbtapi", "2.15.0")
        }
        runDirectory(layout.projectDirectory.dir("run-$minecraftVersion").asFile)
        serverTemplates(layout.projectDirectory.dir("run-template").asFile)
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
    loadBefore = listOf("LootPool")
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