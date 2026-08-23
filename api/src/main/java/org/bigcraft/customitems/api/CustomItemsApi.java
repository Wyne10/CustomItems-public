package org.bigcraft.customitems.api;

import org.bigcraft.customitems.api.spi.CustomItemsRegistries;

/**
 * Static access point for the CustomItems plugin's {@link CustomItemProvider},
 * {@link CustomBlockRegistry} and {@link CustomItemsRegistries}.
 * <p>
 * Both are set once by the plugin during startup and are not synchronized; consumers
 * should treat the getters as unusable (possibly {@code null}) until CustomItems has
 * finished enabling.
 */
public final class CustomItemsApi {
    private static CustomItemProvider provider;
    private static CustomBlockRegistry blockRegistry;
    private static CustomItemsRegistries registries;

    /**
     * @return the active {@link CustomItemProvider}, or {@code null} if CustomItems has not enabled yet
     */
    public static CustomItemProvider getProvider() {
        return provider;
    }

    /**
     * @return the active {@link CustomBlockRegistry}, or {@code null} if CustomItems has not enabled yet
     */
    public static CustomBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    /**
     * @return the extension points of a custom item, or {@code null} if CustomItems has not enabled yet
     */
    public static CustomItemsRegistries getRegistries() {
        return registries;
    }

    public static void setProvider(CustomItemProvider provider) {
        CustomItemsApi.provider = provider;
    }

    public static void setBlockRegistry(CustomBlockRegistry blockRegistry) {
        CustomItemsApi.blockRegistry = blockRegistry;
    }

    public static void setRegistries(CustomItemsRegistries registries) {
        CustomItemsApi.registries = registries;
    }
}
