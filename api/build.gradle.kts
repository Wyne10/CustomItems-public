plugins {
    id("java-library")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

dependencies {
    compileOnly(libs.paperApi)
    compileOnly("org.jetbrains:annotations:26.0.2")
    compileOnlyApi(libs.lootPool)
}

java {
    withSourcesJar()
    toolchain.languageVersion.set(JavaLanguageVersion.of(16))
}

tasks.named("publish") {
    dependsOn("publishToMavenLocal")
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()

    coordinates(findProperty("centralGroup").toString(), "customitems-api", version.toString())

    pom {
        name.set("CustomItems API")
        description.set("Consumer and extension API for the CustomItems Bukkit/Paper plugin, which adds data-driven custom items defined in YAML and lets other plugins register their own activators, attributes, conditions, restrictions and cooldowns.")
        inceptionYear.set("2025")
        url.set("https://github.com/Wyne10/CustomItems-public")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
                distribution.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("Wyne10")
                name.set("Wyne")
                email.set("izmodenov1997@gmail.com")
                organization.set("BigTeam")
                organizationUrl.set("https://github.com/NeverMined-Entertainment")
            }
        }
        scm {
            url.set("https://github.com/Wyne10/CustomItems-public")
            connection.set("scm:git:git://github.com/Wyne10/CustomItems-public.git")
            developerConnection.set("scm:git:ssh://git@github.com/Wyne10/CustomItems-public.git")
        }
    }
}
