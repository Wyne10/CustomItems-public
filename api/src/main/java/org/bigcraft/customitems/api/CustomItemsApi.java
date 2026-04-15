package org.bigcraft.customitems.api;

public final class CustomItemsApi {
    private static CustomItemProvider provider;
    private static CustomBlockRegistry blockRegistry;

    public static CustomItemProvider getProvider() {
        return provider;
    }

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
