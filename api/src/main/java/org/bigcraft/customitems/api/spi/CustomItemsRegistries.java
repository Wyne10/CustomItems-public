package org.bigcraft.customitems.api.spi;

import org.jetbrains.annotations.NotNull;

/**
 * The extension points of a custom item. Obtain the active instance via
 * {@link org.bigcraft.customitems.api.CustomItemsApi#getRegistries()}.
 * <p>
 * <b>When to register.</b> CustomItems reads its item files once the server has finished loading
 * plugins, so an extension declaring {@code depend: [CustomItems]} can register everything from its
 * own {@code onEnable} and still be in time. Registering after that point is refused with a logged
 * warning, because the items that would have used the key are already built.
 */
public interface CustomItemsRegistries {

    /** Kinds of activator — an event plus the rule for finding the player it concerns. */
    @NotNull InstanceRegistry<ActivatorType> activatorTypes();

    /** Kinds of restriction — things a custom item is not allowed to take part in. */
    @NotNull InstanceRegistry<RestrictionType<?>> restrictions();

    /** Kinds of behaviour a custom item takes on once placed as a block. */
    @NotNull InstanceRegistry<BlockAttributeType<?>> blockAttributes();

    /** Effects an activator runs when it fires. */
    @NotNull FactoryRegistry<ActivatorAttribute> activatorAttributes();

    /** Gates every activator attribute is checked against. */
    @NotNull FactoryRegistry<ActivatorCondition> activatorConditions();

    /** Kinds of cooldown an item can declare. */
    @NotNull FactoryRegistry<Cooldown> cooldowns();

    /** What the player sees when a cooldown starts or blocks an activation. */
    @NotNull FactoryRegistry<CooldownFeedback> cooldownFeedback();

    /** Extra steps in building an item's stack. */
    @NotNull FactoryRegistry<CustomItemStackAttribute> itemAttributes();

    /** Adds a source of placeholders for item names and lore. */
    void registerItemPlaceholders(@NotNull ItemPlaceholderProvider provider);

    /** Strategies for choosing which stack of an item an activator acts on. */
    @NotNull InstanceRegistry<ItemIdentifier> itemIdentifiers();

    /** Whether registrations are still being accepted. */
    boolean isOpen();
}
