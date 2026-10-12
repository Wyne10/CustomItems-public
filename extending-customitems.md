---
description: >-
  Compile against customitems-api, register your own activators, effects,
  conditions, restrictions and cooldowns from onEnable, and use them from item
  YAML like any built-in key.
---

# Extending CustomItems

Everything in the [item reference](item-reference.md) is a key registered into a registry at startup, and the registries are public. A plugin that depends on CustomItems can add keys of its own to any of them, and from then on an item file uses them exactly as it uses the built-in ones.

That is the point of the API: a feature too specific, too heavy or too dependent on another plugin to belong in CustomItems lives in its own jar instead, without anyone having to write a second item system around it.

## Adding the dependency

The API artifact is published to Maven Central:

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    compileOnly("io.github.wyne10:customitems-api:3.0.0")
}
```

Keep it `compileOnly`. The API classes ship inside the CustomItems plugin jar under their real package names, so shading your own copy leaves you with two unrelated `CustomItemApi` interfaces and a `ClassCastException`.

Then declare the dependency in your `plugin.yml`:

```yaml
depend: [CustomItems]
```

`depend` rather than `softdepend`, unless your plugin is useful without CustomItems—the registries don't exist until CustomItems has enabled.

Everything in `me.wyne.customitems.api` and `me.wyne.customitems.api.spi` is plain Java against the Bukkit API. No WUtils type appears in a published signature, because CustomItems relocates WUtils into its own shadow package.

## When to register

CustomItems reads its item files **one tick after the server finishes enabling plugins**. That is late enough for every `depend: [CustomItems]` plugin to have had its own `onEnable`, and it's the window you register in:

```java
@Override
public void onEnable() {
    CustomItemsRegistries registries = CustomItemsApi.getRegistries();
    registries.activatorTypes().register(new ShearActivator());
}
```

After that moment the registries close. A registration arriving later is refused with a logged warning, because the items that would have used the key have already been built. `registries.isOpen()` tells you which side of the line you're on.

A reload doesn't reopen them—and doesn't need to. Registered types survive a reload; only the items bound to them are rebuilt.

## The registries

`CustomItemsApi.getRegistries()` returns all of them:

| Registry                        | Adds                                               | Kind     |
| ------------------------------- | -------------------------------------------------- | -------- |
| `activatorTypes()`              | A moment an item reacts to.                        | instance |
| `activatorAttributes()`         | An effect an activator runs.                       | factory  |
| `activatorConditions()`         | A gate an activator is checked against.            | factory  |
| `restrictions()`                | Something an item may not take part in.            | instance |
| `blockAttributes()`             | Behaviour for a placed custom block.               | instance |
| `cooldowns()`                   | A kind of cooldown.                                | factory  |
| `cooldownFeedback()`            | What a cooldown shows the player.                  | factory  |
| `itemAttributes()`              | An extra step in building the stack.               | factory  |
| `itemIdentifiers()`             | A way of finding which stack an activator acts on. | instance |
| `registerItemPlaceholders(...)` | A source of placeholders for names and lore.       | —        |

The two kinds differ in what a config key gets you:

* An **instance registry** takes one shared object, which carries its own key and serves every item that names it. Use it when the object has no per-item configuration beyond what it parses itself.
* A **factory registry** takes a key and a `ConfigFactory`, and builds a fresh object for each item that uses the key.

### What a `ConfigFactory` receives

```java
public interface ConfigFactory<T> {
    @NotNull T create(@NotNull String key, @NotNull ConfigurationSection config);
}
```

Note the second argument: the **enclosing** section, not the value at the key. That's deliberate—it lets one factory accept `my-effect: 5`, `my-effect: [A, B]` or a `my-effect:` section, and lets a factory read sibling keys when it needs to. Implement `CompositeConfigFactory` to take both a bare scalar and a section body without writing the dispatch yourself.

## Writing an activator

An activator is a Bukkit event plus the rule for finding the player it concerns. Subclass `ActivatorType`, add `@EventHandler` methods, and call `dispatch`:

```java
public final class ShearActivator extends ActivatorType {

    public ShearActivator() {
        super("player-shear");
    }

    @EventHandler(ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent event) {
        dispatch(event, event.getPlayer());
    }
}
```

`dispatch` finds each bound item on the player through its configured `identifier`, tests its conditions, and runs its attributes. Use `dispatchForStack` instead when the event carries the stack itself—a dropped item, a consumed item—and looking it up in the inventory would fail.

Register the instance once:

```java
registries.activatorTypes().register(new ShearActivator());
```

CustomItems registers it with Bukkit a single time, no matter how many items use it, and re-binds items to it on each reload. Don't register it as a listener yourself.

Item files can now say:

```yaml
activators:
  player-shear:
    identifier: HOLD
    commands: ['say sheared']
```

## Writing an effect

An `ActivatorAttribute` is one effect. `isApplicable` is the event-type check; `apply` does the work:

```java
public final class SetOnFireAttribute implements ActivatorAttribute {

    private final int ticks;

    public SetOnFireAttribute(int ticks) {
        this.ticks = ticks;
    }

    @Override
    public void apply(Event event, Player player, CustomItemApi item, ItemStack stack) {
        player.setFireTicks(ticks);
    }
}

registries.activatorAttributes()
        .register("set-on-fire", (key, config) -> new SetOnFireAttribute(config.getInt(key)));
```

```yaml
activators:
  right-click:
    identifier: HOLD
    set-on-fire: 60
```

An attribute that returns `false` from `isApplicable` is skipped without failing the activation—which is how an effect that only makes sense on one event type coexists with an item bound to several.

{% hint style="warning" %}
Attributes run in **registration order**, not the order they appear in a file. Yours registers after every built-in, so it runs last—after `update-view`. If your effect changes something the lore shows, refresh the view yourself.
{% endhint %}

## Writing a condition

A condition answers two questions, and keeping them apart matters:

```java
public final class RidingCondition implements ActivatorCondition {

    private final boolean expected;

    public RidingCondition(boolean expected) {
        this.expected = expected;
    }

    @Override
    public boolean test(Event event, Player player, CustomItemApi item, ItemStack stack) {
        return (player.getVehicle() != null) == expected;
    }
}

registries.activatorConditions()
        .register("is-riding", (key, config) -> new RidingCondition(config.getBoolean(key)));
```

`test` is the verdict. `appliesTo(Event)` is whether there is a verdict to give at all—override it when your condition only makes sense for some of the events an item might be bound to:

```java
@Override
public boolean appliesTo(Event event) {
    return event instanceof PlayerMoveEvent;
}
```

A condition that doesn't apply is skipped, not failed. That separation is what makes `inverted()` safe: it flips the verdict without flipping whether there was one. Register a `not-` twin for free:

```java
ConfigFactory<ActivatorCondition> factory =
        (key, config) -> new RidingCondition(config.getBoolean(key));

registries.activatorConditions().register("is-riding", factory);
registries.activatorConditions()
        .register("not-is-riding", (key, config) -> factory.create(key, config).inverted());
```

## Writing a restriction

A restriction parses a value per item and cancels events. `parse` reads the value, `isActive` decides whether it counts as "on", and the `cancelIfBound` / `forEachBinding` helpers do the matching:

```java
public final class CancelShearRestriction extends RestrictionType<Boolean> {

    public CancelShearRestriction() {
        super("cancel-shear");
    }

    @Override
    protected Boolean parse(String key, ConfigurationSection config) {
        return config.getBoolean(key);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onShear(PlayerShearEntityEvent event) {
        cancelIfBound(event, event.getPlayer().getInventory().getItemInMainHand());
    }
}
```

The default `isActive` treats a `false` boolean and an empty collection as "declared but off", so `cancel-shear: false` creates no binding. Cancel at `LOWEST` priority, as the built-ins do, so the cancel is visible downstream.

`BlockAttributeType<V>` has the same shape, for behaviour a placed custom block takes on.

## Writing a cooldown

```java
public final class WorldCooldown implements Cooldown {

    private final Map<UUID, Long> until = new HashMap<>();
    private final long durationMillis;

    @Override public String getKey() { return "world-cooldown"; }

    @Override
    public boolean isActive(CooldownContext context) {
        Long expiry = until.get(context.getPlayer().getWorld().getUID());
        return expiry != null && expiry > System.currentTimeMillis();
    }

    @Override
    public void start(CooldownContext context) {
        until.put(context.getPlayer().getWorld().getUID(), System.currentTimeMillis() + durationMillis);
    }

    // reset, getRemainingMillis ...
}
```

`isActive` must be a pure query—CustomItems calls it to decide whether to turn an activation away, and a side effect there would fire on every check. Anything the player should see belongs in a `CooldownFeedback`, which receives `onStarted` and `onBlocked` at exactly the right moments.

## Contributing placeholders

A placeholder provider adds values to every item's name and lore:

```java
registries.registerItemPlaceholders((sink, item, stack, player) -> {
    if (player == null) return;
    sink.component("owner", player.getName());
});
```

The sink takes three kinds, and the difference is when the value is resolved:

| Method                 | Applied                 | Use for                                                   |
| ---------------------- | ----------------------- | --------------------------------------------------------- |
| `text(key, value)`     | Before markup is parsed | Trusted values, including ones that inject markup.        |
| `component(key, mini)` | After markup is parsed  | Anything player-derived. Markup in the value stays inert. |
| `lines(key, miniList)` | Expands the lore        | A run of lines whose length isn't known in advance.       |

`lines` is the only one that can change how many lore lines an item has. The marker's own line is kept and the expansion follows it; an empty expansion removes the line. `<composed-list>` is built this way.

## Consuming items without extending them

Reading custom items needs no registration and no timing care—just `depend`:

```java
CustomItemProvider provider = CustomItemsApi.getProvider();

CustomItemApi grenade = provider.getItem("grenade");
if (grenade != null) {
    player.getInventory().addItem(grenade.createItemStack(player));
}

// Which custom item, if any, is this stack?
CustomItemApi held = provider.getItem(player.getInventory().getItemInMainHand());
```

`CustomItemsApi.getBlockRegistry()` does the same for placed custom blocks—`isCustomBlock`, `getBlock`, `placeBlock` and `breakBlock`.

Every getter on `CustomItemsApi` returns `null` until CustomItems has enabled, so a `softdepend` consumer should check.

## Checking your work

* `registries.keys()` on any registry lists what is registered, including your additions.
* An item naming a key nobody registered logs `Unknown ... on item '<key>'` and loads without it—so a silently missing effect usually means the key never made it into the registry, or arrived too late.
* Set [`logLevel: DEBUG`](configuration.md) to watch items load one key at a time.
