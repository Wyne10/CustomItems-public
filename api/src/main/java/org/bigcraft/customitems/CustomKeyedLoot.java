package org.bigcraft.customitems;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bigcraft.customitems.api.CustomItemsApi;
import me.wyne.lootpool.api.Loot;
import me.wyne.lootpool.api.LootPool;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single-entry {@link LootPool} keyed to a CustomItems item, analogous to LootPool's own
 * {@code KeyedLoot} but resolving its item dynamically through {@link CustomItemsApi} instead
 * of always yielding the stored {@code loot}.
 * <p>
 * {@code key} is looked up against {@link CustomItemsApi#getProvider()} on every access via
 * {@link #getCustomItem()}; when a matching custom item is registered, its freshly created item
 * stack replaces {@code loot}'s item (keeping {@code loot}'s weight and amount range) so that
 * later edits to the custom item definition are reflected without updating this record. If no
 * custom item is registered under {@code key}, {@code loot} is used as-is.
 */
public record CustomKeyedLoot(@NotNull String key, @NotNull Loot loot) implements ConfigurationSerializable, LootPool {

    public CustomKeyedLoot(@NotNull CustomKeyedLoot customKeyedLoot) {
        this(customKeyedLoot.key(), customKeyedLoot.loot());
    }

    public CustomKeyedLoot(@NotNull Map<String, Object> args) {
        this(deserialize(args));
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("key", key);
        data.put("loot", loot);
        return data;
    }

    @NotNull
    public static CustomKeyedLoot deserialize(@NotNull Map<String, Object> args) {
        return new CustomKeyedLoot((String) args.get("key"), (Loot) args.get("loot"));
    }

    /**
     * @return the custom item registered under {@link #key()}, or {@code null} if none is registered
     */
    public @Nullable CustomItemApi getCustomItem() {
        return CustomItemsApi.getProvider().getItem(key);
    }

    /**
     * @return {@link #loot()} with its item replaced by {@link #getCustomItem()}'s item stack when
     * a matching custom item is registered, otherwise {@link #loot()} unchanged
     */
    public @NotNull Loot getLoot() {
        var item = getCustomItem();
        if (item == null)
            return loot;
        return new Loot(item.createItemStack(null), loot.weight(), loot.minAmount(), loot.maxAmount());
    }

    @Override
    public @NotNull List<@NotNull Loot> getLootList() {
        return List.of(getLoot());
    }

    @Override
    public @NotNull Loot getRandom() {
        return getLoot();
    }

    @Override
    public @NotNull List<@NotNull ItemStack> populate(int slots) {
        return LootPool.populate(getLootList(), slots);
    }

    @Override
    public @NotNull List<@NotNull ItemStack> populate(@NotNull Inventory inventory, int slots) {
        return LootPool.populate(getLootList(), inventory, slots);
    }

    @Override
    public @NotNull List<@NotNull ItemStack> populateRandomly(@NotNull Inventory inventory, int slots) {
        return LootPool.populateRandomly(getLootList(), inventory, slots);
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public @NotNull NamespacedKey getKey() {
        return NamespacedKey.fromString("lootpool:" + key);
    }

}
