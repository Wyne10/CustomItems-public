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

    /** Effects an activator runs when it fires. */
    @NotNull FactoryRegistry<ActivatorAttribute> activatorAttributes();

    /** Gates every activator attribute is checked against. */
    @NotNull FactoryRegistry<ActivatorCondition> activatorConditions();

    /** Extra steps in building an item's stack. */
    @NotNull FactoryRegistry<CustomItemStackAttribute> itemAttributes();

    /** Strategies for choosing which stack of an item an activator acts on. */
    @NotNull InstanceRegistry<ItemIdentifier> itemIdentifiers();

    /** Whether registrations are still being accepted. */
    boolean isOpen();
}
