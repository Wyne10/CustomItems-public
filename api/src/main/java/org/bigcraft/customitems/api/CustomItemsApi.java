package org.bigcraft.customitems.api;

public final class CustomItemsApi {
    private static CustomItemProvider provider;

    public static CustomItemProvider getProvider() {
        return provider;
    }

    public static void setProvider(CustomItemProvider provider) {
        CustomItemsApi.provider = provider;
    }
}
