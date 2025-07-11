import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import org.codehaus.plexus.util.Os

plugins {
    kotlin("jvm") version "2.1.20"
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

    implementation(libs.guice)
    implementation(libs.adventureMini)
    implementation(libs.adventureBukkit)

    implementation(libs.wutilsConfig)
    implementation(libs.wutilsConfigurables)
    implementation(libs.wutilsLog)
    implementation(libs.wutilsI18nKotlin)
    implementation(libs.wutilsCommon)
    implementation(libs.wutilsJson)
}

tasks {
    shadowJar {
        archiveBaseName.set(findProperty("name").toString())
        archiveClassifier.set("")
        minimize()
        if ((findProperty("debug") ?: false) == false) {
            relocate("com.google.inject", "org.bigcraft.customitems.shadow.google.guice")
            relocate("com.google.common", "org.bigcraft.customitems.shadow.google.common")
            relocate("net.kyori", "org.bigcraft.customitems.shadow.net.kyori")
            relocate("me.wyne.wutils", "org.bigcraft.customitems.shadow.wutils")
        }
    }

    runServer {
        val minecraftVersion: String = if (Os.isFamily(Os.FAMILY_WINDOWS)) "1.21.3" else "1.16.5"
        val commandApiVersions = mapOf("1.21.3" to "9.7.0", "1.16.5" to "9.4.2")
        val commandApiVersion = commandApiVersions[minecraftVersion] ?: "9.7.0"
        downloadPlugins {
            url("https://ci.extendedclip.com/view/Plugins/job/PlaceholderAPI/197/artifact/build/libs/PlaceholderAPI-2.11.6.jar")
            url("https://download.luckperms.net/1594/bukkit/loader/LuckPerms-Bukkit-5.5.9.jar")
            url("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/build/libs/ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", "5.2.1", "ViaVersion-5.2.1.jar")
            github("ViaVersion", "ViaBackwards", "5.2.1", "ViaBackwards-5.2.1.jar")
            github("CommandAPI", "CommandAPI", commandApiVersion, "CommandAPI-${commandApiVersion}.jar")
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
    softDepend = listOf("PlaceholderAPI", "CommandAPI", "NBTAPI")
    permissions {
        register("customitems.*") {
            children = listOf("customitems.reload")
            default = BukkitPluginDescription.Permission.Default.OP
        }
        register("customitems.reload") {
            description = "Allows to reload plugin"
        }
    }
}