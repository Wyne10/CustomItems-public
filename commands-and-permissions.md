---
description: >-
  The /customitems command, its four subcommands, and the permissions that guard
  them.
---

# Commands and permissions

## Commands

`/customitems`, aliased `/ci`, has four subcommands.

| Command                                           | Permission             | What it does                                                                                                      |
| ------------------------------------------------- | ---------------------- | ----------------------------------------------------------------------------------------------------------------- |
| `/ci reload`                                      | `customitems.reload`   | Re-reads the config, the language files and every item file.                                                      |
| `/ci give <targets> <key> [amount]`               | `customitems.give`     | Gives a custom item to one or more players.                                                                       |
| `/ci exchange <target> <key> <amount> <command>`  | `customitems.exchange` | Takes items from a player and runs a command if they had enough.                                                  |
| `/ci loot <key> [minAmount] [maxAmount] [weight]` | `lootpool.create`      | Registers the item as a [LootPool](https://app.gitbook.com/o/XsU6IzUhP3OqlEVR7PIo/s/EtRS1JKomP9416pitD9j/) entry. |

`<key>` completes from the loaded item keys, so tab completion is the quickest way to see what's available. `<targets>` is a vanilla entity selector—`@p`, `@a[distance=..10]`, or a player name.

### `/ci reload`

Re-reads everything under `plugins/CustomItems/` and rebinds items to their activators. Items added, changed or deleted while the server runs take effect immediately; Bukkit listeners are not registered a second time, so nothing fires twice.

One thing does not survive a reload: the plugin's cooldowns, which are rebuilt empty, so an item on a long cooldown becomes usable again. Stacks in players' inventories are untouched—they keep their key and pick up the new definition the next time they're used. Placed custom blocks and any area of effect currently running carry on as they were.

### `/ci give`

```
/ci give @a lifesteal 3
```

Builds the item fresh for each target, so per-player attributes and placeholders resolve against the player receiving it. Anything that doesn't fit in the inventory drops at their feet.

### `/ci exchange`

```
/ci exchange Steve coin 10 give %player_name% diamond 1
```

Counts that player's stacks of the item, and if they have at least `amount`, removes that many and runs the trailing command from the console. If they don't, they're told so and nothing happens. The command is run through PlaceholderAPI against the target, so `%player_name%` and friends work.

### `/ci loot`

Registers the item with [LootPool](https://wyne.gitbook.io/wyne-docs/lootpool) as a pool entry, with an optional amount range and weight. Fails if a pool with that key already exists. Only useful with LootPool installed.

## Permissions

| Permission             | Default  | Grants                                                |
| ---------------------- | -------- | ----------------------------------------------------- |
| `customitems.reload`   | Operator | `/ci reload`.                                         |
| `customitems.give`     | Operator | `/ci give`.                                           |
| `customitems.exchange` | Operator | `/ci exchange`.                                       |
| `customitems.*`        | Operator | All above.                                            |
| `lootpool.create`      | —        | `/ci loot`. Declared by LootPool, not by CustomItems. |

## When CommandAPI is missing

Commands are registered through [CommandAPI](https://commandapi.jorel.dev), a soft dependency. Without it CustomItems logs that commands are not registered and runs with none—items still load and work, but changes need a server restart instead of `/ci reload`.
