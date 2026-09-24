---
description: >-
  The five keys in config.yml, the language files, and the files the plugin
  writes underneath plugins/CustomItems.
---

# Configuration

The plugin's own settings are small—almost everything interesting lives in the item files. `plugins/CustomItems/config.yml` is generated on first startup:

```yaml
regenerate: true
lang: 'en.yml'
usePlayerLanguage: true
# LEGACY/ENHANCED_LEGACY/MINI_MESSAGE/ITEM_LEGACY/ITEM_ENHANCED_LEGACY/ITEM_MINI_MESSAGE
serializer: ITEM_MINI_MESSAGE
# OFF/FATAL/ERROR/WARN/INFO/DEBUG/TRACE/ALL
logLevel: INFO
```

| Key                 | What it does                                                                                                                                      |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------- |
| `regenerate`        | Whether to merge new keys into `config.yml` on each startup. See [Regeneration](configuration.md#regeneration).                                   |
| `lang`              | The language file used for players whose own language isn't known, and for the console.                                                           |
| `usePlayerLanguage` | Whether to render each player's messages and item text in their own client language, falling back to `lang`.                                      |
| `serializer`        | How markup in names, lore and messages is parsed. See [Serializers](configuration.md#serializers).                                                |
| `logLevel`          | Threshold for the plugin's own logging. `DEBUG` lists every item key as it loads, which is the quickest way to find the file a bad key came from. |

## Serializers

`serializer` picks the markup dialect for every string the plugin renders—item names, lore and chat messages alike:

| Value                  | Markup                                                                                   |
| ---------------------- | ---------------------------------------------------------------------------------------- |
| `LEGACY`               | `&`-codes                                                                                |
| `ENHANCED_LEGACY`      | `&`-codes plus [EnhancedLegacyText](https://github.com/Vankka/EnhancedLegacyText) markup |
| `MINI_MESSAGE`         | [MiniMessage](https://docs.advntr.dev/minimessage/format.html)                           |
| `ITEM_LEGACY`          | As `LEGACY`, without the italics Minecraft adds to renamed items                         |
| `ITEM_ENHANCED_LEGACY` | As `ENHANCED_LEGACY`, without the italics                                                |
| `ITEM_MINI_MESSAGE`    | As `MINI_MESSAGE`, without the italics                                                   |

Prefer one of the three `ITEM_*` values. Minecraft renders any item with a custom name in italics, and the `ITEM_*` variants wrap the result in a non-italic root so names and lore come out the way you wrote them. The plain variants are there for configs written before the distinction existed.

The choice is server-wide: a name written for MiniMessage shows its tags verbatim under `LEGACY`.

## Languages

`plugins/CustomItems/lang/` holds one file per language—`en.yml` and `ru.yml` ship with the plugin. Each is a flat map of message keys to markup:

```yaml
format-composable-entry: "<gray> - %ci_<key>_name-plain%"
info-activation: "<gray>Effect invoked: %ci_<key>_name%"
error-item-not-found: "<red>Item '<key>' doesn't exist!"
success-plugin-reload: "<green>Plugin reloaded!"
```

Two kinds of substitution appear in these strings. `<key>` and friends are placeholders the plugin fills in—each message documents its own. `%ci_..._name%` is a PlaceholderAPI placeholder, resolved only if PlaceholderAPI is installed.

Add a language by dropping a file next to them; the file name is what `lang` and the player's client language are matched against. Message keys are also what an item's `action-bar` and `player-message` effects name, so a new key here is immediately usable from an item.

{% hint style="info" %}
CustomItems registers the `ci` PlaceholderAPI expansion, so `%ci_<key>_name%` and `%ci_<key>_name-plain%` resolve any item's display name from anywhere on the server.
{% endhint %}

## Regeneration

With `regenerate: true`—the default—each startup merges the version's own defaults into `config.yml`: keys a new version added appear, with their comments, and your values are kept. The defaults it merges from are written to `defaults/config.yml`, and a copy of your file as it was goes to `backups/` first. Set it to `false` and `config.yml` is left exactly as you wrote it, at the cost of having to add new keys by hand after an update.

Regeneration only touches `config.yml`. Item files are a different story: the plugin unpacks the ones it ships **only when `plugins/CustomItems/item/` does not exist yet**—on the very first startup. After that the directory is yours. Nothing you write there is ever overwritten, and nothing you delete comes back.

{% hint style="info" %}
That also means the shipped items are a starting set, not a managed one. To see a later version's samples, move your `item/` directory aside and let the plugin unpack a fresh one.
{% endhint %}

## Files the plugin writes

| Path                                      | Contents                                                                                               |
| ----------------------------------------- | ------------------------------------------------------------------------------------------------------ |
| `plugins/CustomItems/config.yml`          | The settings above.                                                                                    |
| `plugins/CustomItems/item/`               | Item files, in any arrangement of subdirectories. Unpacked once, then left alone. See Writing an item. |
| `plugins/CustomItems/lang/`               | Language files.                                                                                        |
| `plugins/CustomItems/data/blocks.json`    | Every placed custom block, so it is still itself after a restart. Don't edit it.                       |
| `plugins/CustomItems/defaults/config.yml` | The generated defaults regeneration merges from. Don't edit it.                                        |
| `plugins/CustomItems/backups/`            | Copies of `config.yml` from before each regeneration.                                                  |

## Applying changes

Edit anything under `plugins/CustomItems/`, then run `/ci reload` or restart the server. A reload re-reads the config, the language files and every item file, and rebinds items to their activators—so an item added, changed or deleted while the server runs takes effect without a restart.

{% hint style="warning" %}
A reload rebuilds every item, which resets the plugin's cooldowns. Stacks already in players' inventories are untouched: they keep their key, and pick up the new definition the next time they're used.
{% endhint %}
