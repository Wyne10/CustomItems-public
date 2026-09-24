---
description: >-
  One file, one item: the appearance keys at the top, then restrictions,
  activators, block behaviour and cooldowns.
---

# Writing an item

Every file under `plugins/CustomItems/item/` is one custom item, and the file name is its key. `lifesteal.yml` defines the item `lifesteal`, which is what `/ci give` and every cross-reference between items use. Subdirectories are allowed and are only for your own tidiness—`item/weapons/hunter.yml` is still the item `hunter`.

{% hint style="warning" %}
The key is the file name alone, so two files called `hunter.yml` in different subdirectories are the same key, and one of them silently wins. Keep names unique across the whole tree.
{% endhint %}

## The shape of a file

```yaml
name: '<gold>Hunter'          # appearance
material: DIAMOND_SWORD
glow: true

restrictions:                 # what players may not do with it
  cancel-craft: true

activators:                   # what happens when it is used
  player-kill-player:
    chance: 0.5
    steal-level: 0.5

block:                        # how it behaves once placed
  drop-drop: true

cooldowns:                    # how often it may be used
  player-cooldown:
    duration: 30s
```

`material` is the only required key. The four sections are optional, and an item with none of them is simply a decorated vanilla item.

## Appearance

The top level of the file is a standard [WUtils](https://app.gitbook.com/s/ZQ50kJ36ioYPFvndW1LL/) item definition—the same keys any WUtils-based plugin uses for a configured `ItemStack`. The ones you will reach for most:

| Key                                                                            | Value                                                                                                                                                      |
| ------------------------------------------------------------------------------ | ---------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `material`                                                                     | A Bukkit `Material` name. Required.                                                                                                                        |
| `name`                                                                         | Display name, in the markup the [`serializer`](configuration.md#serializers) setting chose. Omit it and the item keeps the material's own translated name. |
| `lore`                                                                         | A list of lore lines, same markup.                                                                                                                         |
| `amount`                                                                       | Stack size.                                                                                                                                                |
| `glow`                                                                         | `true` for the enchantment shimmer without an enchantment.                                                                                                 |
| `unbreakable`                                                                  | `true` to make the item unbreakable.                                                                                                                       |
| `enchantment`, `enchantments`                                                  | One enchantment, or a list of them.                                                                                                                        |
| `attribute`, `attributes`                                                      | Vanilla attribute modifiers.                                                                                                                               |
| `flags`                                                                        | A list of `ItemFlag` names, to hide attributes or enchantments from the tooltip.                                                                           |
| `durability`, `damage`                                                         | Remaining durability, or damage taken.                                                                                                                     |
| `model`                                                                        | Custom model data.                                                                                                                                         |
| `repairCost`                                                                   | The anvil repair cost stored on the item.                                                                                                                  |
| `skull`, `skull64`, `skullPlayer`                                              | A head texture by name, by base64 value, or by player.                                                                                                     |
| `potionType`, `potionModifier`, `potionColor`, `potionEffect`, `potionEffects` | Potion contents and tint.                                                                                                                                  |
| `armorColor`                                                                   | Leather armour tint.                                                                                                                                       |

On top of those CustomItems adds a few of its own—`nbt`, `pdc-tag`, `pdc-tags`, `compositor`, `random-amount`, `random-durability`. They are listed under [item attributes](item-reference.md#item-attributes).

Names and lore may also carry [placeholders](item-reference.md#placeholders) that CustomItems fills in from the stack itself, like `<durability>` or `<cooldown-player-cooldown>`.

## Restrictions

`restrictions` is a flat map of things the item may not take part in. Each key is a [restriction](item-reference.md#restrictions); most take `true`, some take a list that narrows them.

```yaml
restrictions:
  cancel-craft: true              # can't be used as a crafting ingredient
  cancel-storage: [ANVIL]         # can't be put into an anvil
  cancel-enchant-prepare: true    # can't be enchanted
  cancel-damage: [FIRE, LAVA]     # doesn't burn up as a dropped item
```

A restriction set to `false`, or given an empty list, is the same as not writing it at all.

Restrictions are checked before anything else on the server sees the event, so a cancelled action is cancelled for every plugin downstream.

## Activators

`activators` is where the item does something. Each key names an [activator](item-reference.md#activators)—a moment the item reacts to—and its body mixes two vocabularies:

* [**Conditions**](item-reference.md#activator-conditions), which all have to pass, and
* [**attributes**](item-reference.md#activator-attributes), the effects that then run.

```yaml
activators:
  right-click:
    identifier: HOLD              # which stack this is about
    is-sneaking: true             # condition
    chance: 0.25                  # condition
    set-item-amount: -1           # attribute
    player-potion-effects:        # attribute
      speed:
        type: SPEED
        amplifier: 1
        duration: 200
```

Conditions and attributes share one flat section, so the two vocabularies never use the same key. Everything that isn't a recognized key of either—apart from `identifier` and `attributeType`—is logged as a warning and skipped.

### `identifier`

Most activators fire on an event about a player, not about an item, so the item has to be found on that player. `identifier` chooses how:

| Value              | Finds                                                                                                                                                       |
| ------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| `PLAYER`           | The first matching stack anywhere in the inventory. The default.                                                                                            |
| `HOLD`             | Main hand, then off hand.                                                                                                                                   |
| `MAIN`             | Main hand only.                                                                                                                                             |
| `OFF`              | Off hand only.                                                                                                                                              |
| `EMPTY`            | Nothing—for an activator that reacts to an event without needing a stack.                                                                                   |
| `PLAYER_OR_EFFECT` | As `PLAYER`, unless a [Storm](https://app.gitbook.com/o/XsU6IzUhP3OqlEVR7PIo/s/d4n8yKwdTizFMMRWlWAU/) effect named after this item is active on the player. |

If no stack is found, the activator doesn't fire. `PLAYER` is generous—an effect fires from the item sitting in a backpack slot—so use `HOLD` for anything that should require the item in hand.

### Order of operations

1. The event fires, and the activator's identifier looks for a stack.
2. Every condition is tested. Conditions with nothing to say about this particular event are skipped rather than failed.
3. If they all pass, every attribute runs. If any fails, only `condition-cancel-event` runs.

Attributes run in the order CustomItems registered their keys, **not** the order they appear in your file. That ordering is deliberate—`set-item-amount`, `set-item-durability` and `damage-item` all run before `update-view`, so a lore line showing durability is re-rendered after the damage rather than before.

### Declaring an activator twice

Two right-click behaviours on one item would collide, because YAML has one `right-click` key per section. Give the second one any name you like and point it back with `attributeType`:

```yaml
activators:
  right-click:
    identifier: MAIN
    commands: ['say main hand']
  right-click-offhand:
    attributeType: right-click
    identifier: OFF
    commands: ['say off hand']
```

The same trick works for attributes and conditions inside an activator.

## Block

An item with a non-empty `block` section becomes a custom block: placing it records the block, and breaking it runs the item's own rules rather than the material's.

```yaml
name: '<gold>Mysterious Spawner'
material: SPAWNER
block:
  drop-drop: true                 # breaking it drops the custom item back
  random-spawner:
    - 'PIGLIN_BRUTE:25'
    - 'WITCH:7'
    - 'BLAZE:20'
```

Placed blocks are recorded in `data/blocks.json` and survive a restart. The available keys are the [block attributes](item-reference.md#block-attributes).

## Cooldowns

`cooldowns` declares how often the item may be used. Each key is a [kind of cooldown](item-reference.md#cooldowns), and its body carries both the duration and what the player sees:

```yaml
cooldowns:
  player-cooldown:
    duration: 30s
    message: 'error-item-cooldown'   # a key from your language file
    visual: true                     # grey the item out in the hotbar
```

Durations are written as `<amount><unit>`, with `ms`, `s`, `m`, `h`, `d` or `t` for ticks, and several tokens may be strung together: `1d2h30m` is one day, two hours and thirty minutes. A bare number with no unit is read as **ticks**, not milliseconds.

Declaring a cooldown doesn't by itself stop anything. Two activator keys do the work, and you pick which:

```yaml
activators:
  right-click:
    identifier: HOLD
    is-custom-item-cooldowned: false   # a condition: fire only when off cooldown
    cooldown: [player-cooldown]        # an attribute: start it
    commands: ['say used']
```

* `is-custom-item-cooldowned: false` is a condition, so an active cooldown stops the whole activator—including the `cooldown` attribute that would restart it.
* `cooldown-cancel-event: true` is an attribute instead, which cancels the underlying Bukkit event when a cooldown is running. Use it when the point is to stop the vanilla action rather than the item's effect.

Either way the cooldown's `message` is sent exactly once, at the moment an activation is turned away.

## A worked example

A throwable charge that explodes ahead of the player, consumes itself, and can't be crafted:

```yaml
name: '<red>Explosive Charge'
material: FIRE_CHARGE
glow: true
restrictions:
  cancel-craft: true
activators:
  right-click:
    identifier: HOLD
    cancel-event: true
    set-item-amount: -1
    player-explosion:
      offset: 0,0,3
      power: 5
      break-blocks: false
```

`cancel-event` stops the vanilla fire-charge throw, `set-item-amount: -1` takes one from the stack, and the explosion goes off three blocks in front of the player without breaking anything.

And a snowball that slows whoever it hits—two activators on one item, cooperating through a projectile tag:

```yaml
name: '<aqua>Snowball'
material: SNOWBALL
glow: true
activators:
  player-launch-projectile:
    identifier: HOLD
    is-projectile-type: [SNOWBALL]
    set-projectile-meta: snowball
  projectile-hit-player:
    identifier: EMPTY
    is-damager-type: [SNOWBALL]
    has-damager-meta: [snowball]
    player-potion-effects:
      slow:
        type: SLOW
        amplifier: 5
        duration: 200
    call-entity-damage: 'PROJECTILE'
```

The second activator uses `identifier: EMPTY` because it otherwise would require hit player to have a snowball in their inventory in order for activator to work; the `snowball` tag set on launch is what ties the two together.

## Composition

An item with a `compositor` list absorbs the items it names on an anvil, and keeps their effects:

```yaml
name: 'Artifact'
material: CONDUIT
compositor:
  - 'hunter'
  - 'lifesteal'
  - 'illuminator'
lore:
  - '<gray>Combined effects:<composed-list>'
restrictions:
  cancel-place: true
```

Put the artifact in the anvil's first slot and one of the listed items in the second, and the result is an artifact that is now also that item—every restriction, activator and cooldown of both applies to the one stack. Repeat to add more.

`<composed-list>` expands into one lore line per absorbed item, formatted by the `format-composable-entry` key in your [language file](configuration.md#languages). Where the marker sits is where the list goes: the rest of that line is kept, the expansion follows it, and if nothing has been composed yet the line disappears entirely.

## When something is wrong

CustomItems logs and skips rather than refusing to start:

* An unrecognized activator, restriction, block attribute or cooldown key logs `Unknown ... on item '<key>'` and is ignored. The item still loads without it.
* A key whose value is the wrong shape logs `Failed loading '<key>' from '<origin>'` with a stack trace, and that whole item is skipped.
* Set [`logLevel`](configuration.md) to `DEBUG` to see a `Loading key` line for every item, which tells you which file a message belongs to.
