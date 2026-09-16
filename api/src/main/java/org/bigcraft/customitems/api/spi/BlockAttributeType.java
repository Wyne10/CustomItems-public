package org.bigcraft.customitems.api.spi;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A kind of behaviour a custom item takes on once placed as a block.
 * <p>
 * One instance serves every item that declares it: subclass this, add {@code @EventHandler} methods
 * that resolve the affected item through {@link #forBlock} or {@link #forStack}, and register the
 * instance with {@link CustomItemsRegistries#blockAttributes()}.
 * <p>
 * A block belongs to at most one custom item, so the {@code for*} helpers stop at the first match.
 *
 * @param <V> the parsed configuration value, per item
 */
public abstract class BlockAttributeType<V> implements KeyedExtension, Listener {

    private final String key;
    private volatile List<Binding<V>> bindings = List.of();

    protected BlockAttributeType(@NotNull String key) {
        this.key = key;
    }

    @Override
    public final @NotNull String getKey() {
        return key;
    }

    /**
     * Reads this attribute's value out of an item's {@code block} section. Receives the enclosing
     * section, so the value lives at {@code key}.
     */
    protected abstract @NotNull V parse(@NotNull String key, @NotNull ConfigurationSection config);

    /**
     * Whether a parsed value means "apply". By default a {@code false} boolean and an empty
     * collection both mean the attribute was declared but left off, and no binding is made.
     */
    protected boolean isActive(@NotNull V value) {
        if (value instanceof Boolean bool) return bool;
        if (value instanceof Collection<?> collection) return !collection.isEmpty();
        return true;
    }

    public final void bind(@NotNull CustomItemApi item, @NotNull ConfigurationSection config) {
        V value = parse(key, config);
        if (!isActive(value)) return;
        List<Binding<V>> updated = new ArrayList<>(bindings);
        updated.add(new Binding<>(item, value));
        bindings = List.copyOf(updated);
    }

    public final void clearBindings() {
        bindings = List.of();
    }

    /** Runs {@code action} for the item {@code block} was placed as, if it is a bound one. */
    protected final void forBlock(@NotNull Block block, @NotNull BiConsumer<CustomItemApi, V> action) {
        List<Binding<V>> current = bindings;
        for (Binding<V> binding : current) {
            if (binding.item.isCustomItem(block)) {
                action.accept(binding.item, binding.value);
                return;
            }
        }
    }

    /** Runs {@code action} for the item {@code stack} is, if it is a bound one. */
    protected final void forStack(@Nullable ItemStack stack, @NotNull BiConsumer<CustomItemApi, V> action) {
        if (stack == null) return;
        List<Binding<V>> current = bindings;
        for (Binding<V> binding : current) {
            if (binding.item.isCustomItem(stack)) {
                action.accept(binding.item, binding.value);
                return;
            }
        }
    }

    private record Binding<V>(CustomItemApi item, V value) {}
}
