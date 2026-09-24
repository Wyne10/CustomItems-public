---
description: >-
  Custom items as data, in YAML—and a vocabulary of item behaviour that other
  plugins extend in code, so a new key is a class in your jar rather than a fork
  of this one.
---

# CustomItems

CustomItems is a Bukkit/Paper plugin with two layers.

**Items are data.** One YAML file per item, dropped into `plugins/CustomItems/item/`, says what the item looks like, what a player may and may not do with it, and what happens when it is used. No code, no restart, no build step.

**The vocabulary those files are written in is code**—and it's open. Every key an item file can contain is registered into a registry at startup, and any plugin can register more. A behaviour CustomItems doesn't have is a class in your own jar, not a pull request against this one.

## Items are data

A complete item:

```yaml
name: '<red>Lifesteal'
material: WITHER_SKELETON_SKULL
restrictions:
  cancel-place: true
activators:
  player-hit-player:
    chance: 0.02
    player-potion-effects:
      regeneration:
        type: REGENERATION
        amplifier: 1
        duration: 20
```

Carry that skull, hit another player, and two percent of the time you regenerate. The item can't be placed as a block. It survives being renamed, enchanted, stacked and stored, because every stack is tagged with the item's key.

The file is built out of a fixed set of sections—[`restrictions`](writing-an-item.md#restrictions), [`activators`](writing-an-item.md#activators), [`block`](writing-an-item.md#block) and [`cooldowns`](writing-an-item.md#cooldowns)—filled with keys from a vocabulary of eighteen activators, eighteen restrictions, nine block behaviours, thirty-five effects and sixty-one conditions. [Writing an item](writing-an-item.md) walks through it; the [Item reference](item-reference.md) lists every key.

## The vocabulary is code

Eventually an item wants something the vocabulary doesn't have. Register the key from your own plugin's `onEnable`:

```java
CustomItemsApi.getRegistries()
        .activatorAttributes()
        .register("set-on-fire", (key, config) -> new SetOnFireAttribute(config.getInt(key)));
```

and it is a key like any other:

```yaml
activators:
  right-click:
    identifier: HOLD
    set-on-fire: 60
```

Nothing about it is second-class. The built-in keys are registered through the same registries, against the same interfaces, in the same window—`cancel-place` and `player-hit-player` are ordinary registrations that happen to ship in the jar. Extensions compile against `io.github.wyne10:customitems-api` from Maven Central, which is plain Java and Bukkit with no third-party types in its signatures.

Every layer is open, not just effects: activators, conditions, restrictions, block behaviours, cooldowns, the way a stack is built, and the placeholders its lore can render. See [Extending CustomItems](extending-customitems.md).

## What else it does

* **Custom blocks.** An item with a `block` section stays itself once placed—the placement is recorded and survives restarts, and breaking it runs the item's own drop rules rather than the material's.
* **Composition.** One item can absorb others on an anvil, carrying all of their effects on a single stack.
* **Cooldowns as a first-class thing.** Per-player, per-stack, global, or imposed on a different item entirely, each with its own message and hotbar grey-out.
* **Localization.** Names, lore and messages are rendered per player in their own client language, in MiniMessage or legacy markup.
* **Live reload.** `/ci reload` re-reads every item file without a restart and without double-registering a single listener.

## Requirements

|          |                                                                                                       |
| -------- | ----------------------------------------------------------------------------------------------------- |
| Server   | Bukkit/Paper 1.16 or later                                                                            |
| Java     | 16 or later                                                                                           |
| Optional | CommandAPI, PlaceholderAPI, NBT-API, WorldEdit, WorldGuard, AbstractMenus, Storm, AntiRelog, LootPool |

Every dependency is soft. Without CommandAPI there are no commands; without WorldGuard the region conditions never match; without NBT-API the `nbt` item attribute is unavailable. Everything else keeps working.

## How the two layers meet

1. CustomItems enables and registers its own vocabulary.
2. Plugins that extend it enable next and register theirs. `depend: [CustomItems]` is what guarantees the order.
3. Once the server has finished loading plugins, the registries close and CustomItems reads every file under `plugins/CustomItems/item/`. A key nobody registered is logged as a warning and skipped, so one bad line costs you that line rather than the item or the server.
4. Each item binds itself to the activators, restrictions and block behaviours it named. One Bukkit listener serves each kind, however many items use it.
5. `/ci reload` repeats steps 3 and 4. Registered vocabulary survives; only the items bound to it are rebuilt.

To compile the plugin yourself, see [Building from source](building-from-source.md).
