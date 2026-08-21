package org.bigcraft.customitems.api;

/**
 * Static access point for the CustomItems plugin's {@link CustomItemProvider} and
 * {@link CustomBlockRegistry}.
 * <p>
 * Both are set once by the plugin during startup and are not synchronized; consumers
 * should treat the getters as unusable (possibly {@code null}) until CustomItems has
 * finished enabling.
 */
public final class CustomItemsApi {
    private static CustomItemProvider provider;
    private static CustomBlockRegistry blockRegistry;

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

    public static void setProvider(CustomItemProvider provider) {
        CustomItemsApi.provider = provider;
    }

    public static void setBlockRegistry(CustomBlockRegistry blockRegistry) {
        CustomItemsApi.blockRegistry = blockRegistry;
    }
}
