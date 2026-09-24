---
description: >-
  Clone with submodules, build the shaded plugin jar, run a test server, and
  publish the API to Maven Central.
---

# Building from source

## What you need

Git, a JDK to run Gradle with, and nothing else—the Gradle wrapper fetches Gradle itself, and the build provisions the JDKs it actually compiles and runs with: Java 16 for compilation, and a JetBrains Runtime 21 for the test server.

## Clone

`gradle/libs.versions.toml` is a symlink into the `libs` submodule, which holds the version catalog shared across these plugins. A clone without it fails while Gradle configures the build:

```bash
git clone --recurse-submodules https://github.com/Wyne10/CustomItems-public.git
```

If you already cloned without `--recurse-submodules`, run `git submodule update --init`.

## Build

```bash
./gradlew shadowJar
```

The shaded plugin jar lands in `build/libs/CustomItems-<version>.jar`, ready to drop into a server's `plugins/` folder. The build relocates Guice, Guava, Adventure, EnhancedLegacyText and WUtils under `me.wyne.customitems.shadow`, so the plugin can't collide with another plugin bundling the same libraries.

| Command                            | What it does                                                                          |
| ---------------------------------- | ------------------------------------------------------------------------------------- |
| `./gradlew shadowJar -Pdebug=true` | Builds without relocating anything, which keeps stack traces readable while debugging |
| `./gradlew :api:build`             | Builds the extension API module on its own                                            |
| `./gradlew clean shadowJar`        | Rebuilds from scratch                                                                 |

The project version lives in `gradle.properties` and names both the jar and the published API artifact.

## The two modules

| Module | Contents                                                                                                                            |
| ------ | ----------------------------------------------------------------------------------------------------------------------------------- |
| `api`  | Plain Java against the Bukkit API—the consumer types and the [extension SPI](extending-customitems.md). Published to Maven Central. |
| root   | The plugin itself, in Kotlin. Shaded, and not published as a dependency.                                                            |

Nothing in `api` may reference WUtils, because WUtils is relocated into the plugin's shadow package and a relocated type in a published signature is unusable to anyone else.

## Run a test server

```bash
./gradlew runServer
```

This downloads Paper 1.16.5 along with CommandAPI, LuckPerms, PlaceholderAPI, ProtocolLib, NBT-API, ViaVersion, ViaBackwards and the sibling plugins CustomItems talks to, then starts a server in `run/` with the freshly built plugin installed. Add `-Pdebug=true` to run on 1.19.4 instead, with relocation off.

The shipped sample items are unpacked into `run/plugins/CustomItems/item/` on the first start—thirty-six of them, exercising most of the vocabulary. `/ci give @s <key>` is the fastest way to try one.

{% hint style="info" %}
`logLevel: DEBUG` in `run/plugins/CustomItems/config.yml` makes the server log every item key as it loads, which is what you want when checking whether a change broke anything.
{% endhint %}

## Publish the API

The `api` module is the only thing published; the plugin jar itself is distributed as a jar, not as a dependency.

To try a release locally, into `~/.m2`:

```bash
./gradlew :api:publishToMavenLocal
```
