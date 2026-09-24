package me.wyne.customitems.api;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A single custom item definition, as registered with {@link CustomItemProvider}.
 * Created item stacks are tagged with this item's key so they can later be recognized
 * by {@link #isCustomItem} or resolved back via {@link CustomItemProvider#getItem(ItemStack)}.
 */
public interface CustomItemApi {

    /**
     * @return the key this item is registered under
     */
    @NotNull String getKey();

    /**
     * @return the material this item is built on
     */
    @NotNull Material getMaterial();

    /**
     * Builds a new item stack from this item's configured attributes, tagged with {@link #getKey()}.
     *
     * @param player context used to resolve player-dependent attributes (e.g. placeholders); may be {@code null} if no player context is available
     */
    @NotNull ItemStack createItemStack(@Nullable Player player);

    /**
     * Re-applies this item's configured attributes onto an existing item stack in place, tagging
     * it with {@link #getKey()}.
     *
     * @param item the item stack to modify
     * @param player context used to resolve player-dependent attributes (e.g. placeholders); may be {@code null} if no player context is available
     */
    void applyItemStack(@NotNull ItemStack item, @Nullable Player player);

    /**
     * @return whether {@code item} is (or contains, if composed with other custom items) this custom item; {@code false} if {@code item} is {@code null}
     */
    boolean isCustomItem(@Nullable ItemStack item);

    /**
     * @return whether {@code block} is a placed custom block matching this item
     */
    boolean isCustomItem(@NotNull Block block);

    /**
     * @return the first stack in {@code inventory} matching this custom item, or {@code null} if none is found
     */
    @Nullable ItemStack getItem(@NotNull Inventory inventory);

    /**
     * Like {@link #getItem(Inventory)}, but checks the held item and off-hand/armor slots before
     * the rest of the inventory.
     *
     * @return the first matching stack, or {@code null} if none is found
     */
    @Nullable ItemStack getItem(@NotNull PlayerInventory inventory);

    /**
     * @return every stack in {@code inventory} matching this custom item, in inventory slot order
     */
    @NotNull List<@NotNull ItemStack> getItems(@NotNull Inventory inventory);
}
