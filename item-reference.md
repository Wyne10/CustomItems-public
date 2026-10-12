---
description: >-
  Every key an item file can contain—activators, conditions, effects,
  restrictions, block behaviour, cooldowns and placeholders.
---

# Item reference

The vocabulary an item file is written in. For how the pieces go together, see [Writing an item](writing-an-item.md). Plugins can add keys of their own to every list here—see [Extending CustomItems](extending-customitems.md).

## Value shapes

A few notations recur:

| Shape          | Written as                                | Meaning                                                         |
| -------------- | ----------------------------------------- | --------------------------------------------------------------- |
| **flag**       | `true` / `false`                          | `false` means the key is off, exactly as if it weren't written. |
| **list**       | `[A, B]`, or a YAML list                  | An empty list also means off.                                   |
| **duration**   | `30s`, `5m`, `1d2h`, `200ms`, `40t`, `40` | Tokens may be strung together. A bare number is **ticks**.      |
| **comparison** | `>=5`, `<10`, `==3`, `3`                  | Operators `<`, `>`, `<=`, `>=`, `==`. No operator means `==`.   |
| **operation**  | `+5`, `-1`, `*2`, `/2`, `**2`, `5`        | Applied to the current value. No operator means "set to".       |

## Activators

Each key opens a section of [conditions](item-reference.md#activator-conditions) and [effects](item-reference.md#activator-attributes), plus the optional `identifier` and `attributeType` keys.

| Key                        | Fires when                                                                                              |
| -------------------------- | ------------------------------------------------------------------------------------------------------- |
| `right-click`              | The player right-clicks air or a block, with the main hand.                                             |
| `player-kill-player`       | The player kills another player. Resolved on the killer.                                                |
| `player-death`             | The player dies. The item is looked for on the player who died.                                         |
| `player-hit-player`        | The player damages another player. Resolved on the attacker.                                            |
| `player-hit-entity`        | The player damages a non-player entity. Resolved on the attacker.                                       |
| `entity-hit-player`        | A non-player entity damages the player. Resolved on the victim.                                         |
| `projectile-hit-player`    | A projectile damages the player. Resolved on the victim.                                                |
| `player-take-damage`       | The player takes damage from any source.                                                                |
| `player-launch-projectile` | The player fires a projectile.                                                                          |
| `player-potion-effect`     | A potion effect is added to, or removed from, the player.                                               |
| `player-consume-item`      | The player finishes eating or drinking something.                                                       |
| `player-block-break`       | The player breaks a block.                                                                              |
| `player-block-place`       | The player places a block.                                                                              |
| `player-resurrect`         | A totem saves the player from death.                                                                    |
| `player-move`              | The player moves to a different block. Position changes only, not look direction.                       |
| `player-command`           | The player runs a command.                                                                              |
| `player-drop-item`         | The player drops an item. Fires for the dropped stack itself, so `identifier` is ignored.               |
| `aoe-finish`               | An [area of effect](item-reference.md#entities-and-the-world) expires. Fires once per player inside it. |

Activators run at `HIGH` priority and ignore already-cancelled events, except `right-click`, which also sees cancelled ones since this event doesn't work on air clicks otherwise.

## Item identifiers

The value of the `identifier` key inside an activator.

| Value              | Finds the stack                                                                                                                               |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------- |
| `PLAYER`           | Anywhere in the inventory—held slot first, then off hand and armour, then the rest. The default.                                              |
| `HOLD`             | In the main hand, else the off hand.                                                                                                          |
| `MAIN`             | In the main hand only.                                                                                                                        |
| `OFF`              | In the off hand only.                                                                                                                         |
| `EMPTY`            | Nowhere—the activator fires with an empty stack.                                                                                              |
| `PLAYER_OR_EFFECT` | As `PLAYER`, but works as `EMPTY` when [Storm](https://wyne.gitbook.io/wyne-docs/storm) effect named after this item is active on the player. |

## Activator conditions

Every condition in an activator's section has to pass before any effect runs. A condition that has nothing to say about the event that fired—an item-consumption condition on a right-click, say—is skipped rather than failed.

Ten of these have a `not-` twin that inverts the verdict: `not-is-target-block`, `not-is-on-block`, `not-is-in-biome`, `not-is-in-world`, `not-is-within-aoe`, `block-not-is-within-aoe`, `not-is-aoe-affected`, `not-is-within-region`, `not-is-within-top-region`, `not-has-block-meta`.

### Player state

| Key            | Value       | Passes when                                                                           |
| -------------- | ----------- | ------------------------------------------------------------------------------------- |
| `chance`       | `0.0`–`1.0` | A roll succeeds. `0.02` is two percent.                                               |
| `is-sneaking`  | flag        | The player is (or isn't) sneaking.                                                    |
| `is-sprinting` | flag        | The player is sprinting.                                                              |
| `is-flying`    | flag        | The player is flying.                                                                 |
| `is-blocking`  | flag        | The player is blocking with a shield.                                                 |
| `is-gliding`   | flag        | The player is gliding with elytra.                                                    |
| `is-swimming`  | flag        | The player is swimming.                                                               |
| `is-on-fire`   | flag        | The player is on fire.                                                                |
| `is-in-air`    | flag        | The player is off the ground.                                                         |
| `is-pvp`       | flag        | AntiRelog reports the player in PvP, silent or not. Always `false` without AntiRelog. |

### Player stats

| Key              | Value      | Compares                               |
| ---------------- | ---------- | -------------------------------------- |
| `is-level`       | comparison | The player's experience level.         |
| `is-exp`         | comparison | The player's total experience points.  |
| `is-health`      | comparison | Current health, in half-hearts.        |
| `is-food-level`  | comparison | Current hunger.                        |
| `is-light-level` | comparison | Light level at the player's own block. |

### Permissions

| Key                   | Value | Passes when                      |
| --------------------- | ----- | -------------------------------- |
| `has-permissions`     | list  | The player has **all** of them.  |
| `not-has-permissions` | list  | The player has **none** of them. |

{% hint style="info" %}
These two are not each other's negation, which is why both exist. The opposite of "has all" is "is missing at least one"; `not-has-permissions` is the blacklist reading instead.
{% endhint %}

### Location

| Key                    | Value      | Passes when                                                                     |
| ---------------------- | ---------- | ------------------------------------------------------------------------------- |
| `is-in-world`          | list       | The player is in one of these worlds, by name.                                  |
| `is-in-biome`          | list       | The player is in one of these `Biome`s.                                         |
| `is-on-block`          | list       | The block under the player is one of these materials.                           |
| `is-target-block`      | list       | The block the player is looking at is one of these materials.                   |
| `is-within-region`     | list       | A WorldGuard region at the player matches one of these **regular expressions**. |
| `is-within-top-region` | list       | The _highest-priority_ region at the player matches one of them.                |
| `is-weather`           | list       | The world's weather is one of `CLEAR`, `RAIN`, `STORM`.                         |
| `is-day-time`          | comparison | The world's time, in ticks.                                                     |

{% hint style="warning" %}
Region names are matched as regular expressions, not literally. A plain name like `spawn` or `__global__` works unchanged, but a name containing `.`, `+`, `(` or other regex metacharacters has to be escaped.
{% endhint %}

### The item itself

| Key                         | Value      | Passes when                                                                   |
| --------------------------- | ---------- | ----------------------------------------------------------------------------- |
| `is-item-type`              | list       | The stack is one of these materials.                                          |
| `is-item-durability`        | comparison | Durability remaining on the stack.                                            |
| `is-item-cooldown`          | comparison | The vanilla item cooldown on the stack's material, in ticks.                  |
| `is-item-equipment`         | list       | The item is in one of these `EquipmentSlot`s.                                 |
| `is-item-composed`          | flag       | The stack has other items [composed](writing-an-item.md#composition) into it. |
| `is-custom-item-cooldowned` | flag       | Any of the item's own [cooldowns](item-reference.md#cooldowns) is running.    |
| `is-potion-type`            | list       | The stack is a potion carrying one of these effect types.                     |

### The event

| Key                       | Value | Passes when                                                           |
| ------------------------- | ----- | --------------------------------------------------------------------- |
| `is-block-type`           | list  | The block the event is about is one of these materials.               |
| `has-block-meta`          | list  | That block carries one of these metadata keys.                        |
| `is-entity-type`          | list  | The entity the event is about is one of these types.                  |
| `has-entity-meta`         | list  | That entity carries one of these metadata keys.                       |
| `is-damager-type`         | list  | The damaging entity is one of these types.                            |
| `has-damager-meta`        | list  | The damaging entity carries one of these metadata keys.               |
| `is-projectile-type`      | list  | The launched projectile is one of these types.                        |
| `is-damage-cause`         | list  | The damage cause is one of these `DamageCause`s.                      |
| `is-effect-type`          | list  | The potion effect being applied is one of these types.                |
| `is-effect-action`        | list  | The effect change is one of `ADDED`, `CHANGED`, `CLEARED`, `REMOVED`. |
| `is-effect-cause`         | list  | The effect's `Cause`—`POTION_DRINK`, `BEACON`, `COMMAND` and so on.   |
| `is-consumed-item-type`   | list  | The consumed item is one of these materials.                          |
| `is-consumed-item-custom` | flag  | The consumed item is this custom item.                                |
| `is-dropped-item-custom`  | flag  | The dropped item is this custom item.                                 |
| `command-whitelist`       | list  | The command the player typed contains one of these strings.           |

### Areas of effect

| Key                   | Value | Passes when                                                                         |
| --------------------- | ----- | ----------------------------------------------------------------------------------- |
| `is-within-aoe`       | list  | The player stands inside an active area with one of these keys, whoever started it. |
| `block-is-within-aoe` | list  | The block the event is about is inside one of them.                                 |
| `is-aoe-affected`     | list  | The player stands inside one of them that somebody **else** started.                |
| `is-aoe-finished`     | list  | The `aoe-finish` event is about one of these keys.                                  |

## Activator attributes

The effects. They run in the order below, whatever order you write them in—which is how `set-item-amount` and `damage-item` reliably happen before `update-view` re-renders the lore.

### Event control

| Key                      | Value | Effect                                                                                            |
| ------------------------ | ----- | ------------------------------------------------------------------------------------------------- |
| `condition-cancel-event` | flag  | Cancels the event **when a condition fails**—the only key that runs on failure.                   |
| `cooldown-cancel-event`  | flag  | Cancels the event when any of the item's cooldowns is running, and sends that cooldown's message. |
| `cancel-event`           | flag  | Cancels the event.                                                                                |
| `cooldown`               | list  | Starts these of the item's cooldowns, by key.                                                     |
| `commands`               | list  | Runs these commands from the console.                                                             |
| `update-view`            | flag  | Re-renders the stack's name and lore from its current state, a tick later.                        |

### Potion effects

| Key                     | Value   | Effect                                            |
| ----------------------- | ------- | ------------------------------------------------- |
| `player-potion-effects` | section | Applies effects to the player holding the item.   |
| `entity-potion-effects` | section | Applies effects to the other entity in the event. |
| `player-remove-effects` | list    | Removes these effect types from the player.       |
| `entity-remove-effects` | list    | Removes them from the other entity.               |

Each effect in a section is a named entry with `type`, `duration` in ticks, and an optional `amplifier`:

```yaml
player-potion-effects:
  regeneration:
    type: REGENERATION
    amplifier: 1
    duration: 20
```

### The player

| Key                  | Value       | Effect                                                                   |
| -------------------- | ----------- | ------------------------------------------------------------------------ |
| `set-player-exp`     | operation   | Changes the player's total experience points.                            |
| `steal-level`        | `0.0`–`1.0` | On a kill, transfers that fraction of the victim's levels to the killer. |
| `repair`             | list        | Fully repairs whatever is in these `EquipmentSlot`s, if it has Mending.  |
| `action-bar`         | string      | Sends a language-file key to the player's action bar.                    |
| `player-message`     | string      | Sends a language-file key to the player's chat.                          |
| `player-interaction` | section     | Sends a WUtils interaction list—message, title, sound, command, in one.  |
| `player-sound`       | section     | Plays a sound to the player alone.                                       |
| `local-sound`        | section     | Plays a sound at the player's location, audible to everyone nearby.      |

Both message keys receive `<key>`, this item's key, so one language entry can serve every item that names it.

### The stack

| Key                   | Value     | Effect                                                                           |
| --------------------- | --------- | -------------------------------------------------------------------------------- |
| `set-item-amount`     | operation | Changes the stack size. `-1` consumes one.                                       |
| `set-item-durability` | operation | Changes remaining durability, ignoring Unbreaking.                               |
| `damage-item`         | number    | Damages the item the vanilla way, respecting Unbreaking and breaking it at zero. |
| `set-item-owner`      | flag      | On a drop, marks the dropped item so only the dropper can pick it up.            |
| `lock-drop`           | duration  | On a death, re-drops everything owned by the killer for this long.               |

### Entities and the world

| Key                   | Value     | Effect                                                                                       |
| --------------------- | --------- | -------------------------------------------------------------------------------------------- |
| `call-entity-damage`  | string    | Fires a fresh damage event against the other entity, with this `DamageCause`.                |
| `cause-entity-damage` | section   | Deals damage to the other entity directly.                                                   |
| `set-damager-health`  | operation | Changes the damaging entity's health.                                                        |
| `consume-projectile`  | flag      | Removes the projectile from the world.                                                       |
| `set-projectile-meta` | string    | Tags the launched projectile, so a later activator can recognize it with `has-damager-meta`. |
| `set-custom-block`    | string    | Records the placed block as a different custom item, by key.                                 |
| `player-explosion`    | section   | Creates an explosion at the player.                                                          |
| `force`               | section   | Pushes the player, by `radius`, `velocity` and an `offset` vector.                           |
| `relative-force`      | section   | The same, relative to the player's facing.                                                   |
| `force-field`         | section   | Pushes every _other_ player away from a point near the player.                               |
| `create-aoe`          | section   | Starts an area of effect—`key`, `radius`, `duration` and a `particle` section.               |
| `locate-compass`      | section   | Points a compass at a matching structure or region. Compasses only.                          |

## Restrictions

Flat keys in the `restrictions` section. Unless noted, the value is a flag.

| Key                      | Value | Stops                                                                                                  |
| ------------------------ | ----- | ------------------------------------------------------------------------------------------------------ |
| `cancel-drop`            | flag  | Dropping the item.                                                                                     |
| `cancel-place`           | flag  | Placing it as a block.                                                                                 |
| `cancel-craft`           | flag  | Using it as an ingredient in any recipe.                                                               |
| `cancel-craft-vanilla`   | flag  | Using it in `minecraft:` recipes only, leaving plugin recipes alone.                                   |
| `cancel-smelt`           | flag  | Smelting it in a furnace.                                                                              |
| `cancel-consume`         | flag  | Eating or drinking it.                                                                                 |
| `cancel-despawn`         | flag  | The dropped item despawning.                                                                           |
| `cancel-enchant`         | flag  | Enchanting it.                                                                                         |
| `cancel-enchant-prepare` | flag  | The enchantment table offering it any options at all.                                                  |
| `cancel-rename`          | flag  | Renaming it on an anvil.                                                                               |
| `cancel-anvil`           | flag  | Any anvil operation on it.                                                                             |
| `cancel-projectile`      | flag  | Throwing it as a projectile.                                                                           |
| `cancel-entity-action`   | flag  | Right-clicking an entity with it.                                                                      |
| `cancel-storage`         | list  | Putting it into these `InventoryType`s.                                                                |
| `allow-storage`          | list  | Putting it into anything **except** these `InventoryType`s.                                            |
| `cancel-storage-title`   | regex | Moving it inside an inventory whose title matches this regular expression. The whole title must match. |
| `cancel-damage`          | list  | The dropped item being destroyed by these `DamageCause`s.                                              |
| `cancel-action`          | list  | These `Action`s—`LEFT_CLICK_AIR`, `RIGHT_CLICK_BLOCK` and so on.                                       |

Restrictions are checked at `LOWEST` priority, so a cancel is visible to every other plugin.

## Block attributes

Keys in the `block` section. Any of them makes the item a custom block.

| Key                 | Value  | Effect                                                                                                     |
| ------------------- | ------ | ---------------------------------------------------------------------------------------------------------- |
| `break-drop`        | string | What breaking it drops—a material name, another custom item's key, or a boolean to drop this custom item.  |
| `drop-drop`         | string | What breaking it drops, but only if the block was going to drop something anyway.                          |
| `break-magnet`      | string | As `break-drop`, but the drop goes straight into the breaker's inventory.                                  |
| `burn-drop`         | string | What it drops when it burns.                                                                               |
| `explode-drop`      | string | What it drops when an explosion destroys it.                                                               |
| `furnace-speed`     | number | Multiplies smelting speed, for a furnace block.                                                            |
| `unbreakable-anvil` | flag   | The anvil never degrades or breaks.                                                                        |
| `random-spawner`    | list   | Weighted spawner contents, as `ENTITY_TYPE:weight` entries.                                                |
| `disable-physics`   | flag   | The block ignores physics updates—sand that doesn't fall, torches that stay.                               |

## Cooldowns

Keys in the `cooldowns` section. Each takes a `duration` plus any of the feedback keys below.

| Key               | Held against                                                                   |
| ----------------- | ------------------------------------------------------------------------------ |
| `player-cooldown` | The player, so every stack of the item shares one timer.                       |
| `item-cooldown`   | The individual stack, so a second copy is usable immediately.                  |
| `global-cooldown` | The server, so the item is on cooldown for everyone at once.                   |
| `other-cooldown`  | Other items: takes an `items` list of keys, and puts each of them on cooldown. |

| Feedback key | Value  | Shows                                                                                |
| ------------ | ------ | ------------------------------------------------------------------------------------ |
| `message`    | string | A language-file key, sent when an activation is turned away. Receives `<remaining>`. |
| `visual`     | flag   | Greys the item out in the hotbar for as long as the cooldown runs.                   |

```yaml
cooldowns:
  player-cooldown:
    duration: 30s
    message: 'error-item-cooldown'
    visual: true
  other-cooldown:
    duration: 50s
    items: ['explosion']
```

## Item attributes

Extra top-level keys CustomItems adds to the standard WUtils item definition.

| Key                 | Value   | Effect                                                                            |
| ------------------- | ------- | --------------------------------------------------------------------------------- |
| `nbt`               | string  | Raw NBT merged into the stack. Needs NBT-API.                                     |
| `pdc-tag`           | section | One persistent-data entry, as `type` and `value`, stored under the key `pdc-tag`. |
| `pdc-tags`          | section | Several of them, each named entry stored under its own name.                      |
| `compositor`        | list    | Item keys this one will [absorb](writing-an-item.md#composition) on an anvil.     |
| `random-amount`     | range   | A stack size rolled per stack, as `1..5`.                                         |
| `random-durability` | range   | Durability rolled per stack, as `100..250`.                                       |

## Placeholders

Usable in an item's `name` and `lore`, and re-evaluated whenever the item's view is refreshed.

| Placeholder        | Renders                                                                                                                                                   |
| ------------------ | --------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `<durability>`     | Durability remaining on the stack.                                                                                                                        |
| `<max-durability>` | The material's maximum durability.                                                                                                                        |
| `<amount>`         | The stack size.                                                                                                                                           |
| `<cooldown-KEY>`   | Time left on the item's cooldown named `KEY`, in seconds. One placeholder per declared cooldown.                                                          |
| `<composed-list>`  | One line per [composed](writing-an-item.md#composition) item, formatted by `format-composable-entry`. Expands the lore rather than substituting in place. |

These are filled in when the stack is built and whenever the [`update-view`](item-reference.md#event-control) attribute runs—so a durability counter in the lore needs `update-view` on whatever activator changes it.

PlaceholderAPI placeholders work here too, including CustomItems' own `%ci_<key>_name%`.
