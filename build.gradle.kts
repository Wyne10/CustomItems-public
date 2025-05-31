import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

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
        relocate("com.google.inject", "org.bigcraft.customitems.shadow.google.guice")
        relocate("com.google.common", "org.bigcraft.customitems.shadow.google.common")
        relocate("net.kyori", "org.bigcraft.customitems.shadow.net.kyori")
        relocate("me.wyne.wutils", "org.bigcraft.customitems.shadow.wutils")
    }

    runServer {
        downloadPlugins {
            url("https://ci.extendedclip.com/view/Plugins/job/PlaceholderAPI/197/artifact/build/libs/PlaceholderAPI-2.11.6.jar")
            url("https://download.luckperms.net/1584/bukkit/loader/LuckPerms-Bukkit-5.5.0.jar")
            url("https://ci.dmulloy2.net/job/ProtocolLib/lastSuccessfulBuild/artifact/build/libs/ProtocolLib.jar")
            github("ViaVersion", "ViaVersion", "5.2.1", "ViaVersion-5.2.1.jar")
            github("ViaVersion", "ViaBackwards", "5.2.1", "ViaBackwards-5.2.1.jar")
            github("CommandAPI", "CommandAPI", "9.7.0", "CommandAPI-9.7.0.jar")
            modrinth("nbtapi", "2.15.0")
        }
        minecraftVersion("1.21.3")
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
    jvmArgs("-XX:+AllowEnhancedClassRedefinition")
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