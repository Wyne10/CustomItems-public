package org.bigcraft.customitems.api.spi;

import org.bigcraft.customitems.api.CustomItemApi;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * A kind of restriction — something a custom item is not allowed to take part in.
 * <p>
 * One instance serves every item that declares it: subclass this, add {@code @EventHandler} methods
 * that cancel through {@link #cancelIfBound} or {@link #forEachBinding}, and register the instance
 * with {@link CustomItemsRegistries#restrictions()}.
 * <p>
 * Restrictions run at {@link org.bukkit.event.EventPriority#LOWEST} by convention, so a cancel is
 * visible to everything downstream.
 *
 * <pre>{@code
 * public final class CancelDrop extends RestrictionType<Boolean> {
 *     public CancelDrop() { super("cancel-drop"); }
 *
 *     @Override protected Boolean parse(String key, ConfigurationSection config) {
 *         return config.getBoolean(key);
 *     }
 *
 *     @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
 *     public void onDrop(PlayerDropItemEvent event) {
 *         cancelIfBound(event, event.getItemDrop().getItemStack());
 *     }
 * }
 * }</pre>
 *
 * @param <V> the parsed configuration value, per item
 */
public abstract class RestrictionType<V> implements KeyedExtension, Listener {

    private final String key;
    private volatile List<Binding<V>> bindings = List.of();

    protected RestrictionType(@NotNull String key) {
        this.key = key;
    }

    @Override
    public final @NotNull String getKey() {
        return key;
    }

    /**
     * Reads this restriction's value out of an item's {@code restrictions} section. Receives the
     * enclosing section, so the value lives at {@code key}.
     */
    protected abstract @NotNull V parse(@NotNull String key, @NotNull ConfigurationSection config);

    /**
     * Whether a parsed value means "restrict". By default a {@code false} boolean and an empty
     * collection both mean the restriction was declared but left off, and no binding is made.
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

    /**
     * Cancels {@code event} if {@code stack} belongs to any bound item, stopping at the first match.
     * The common case, for restrictions whose value is a plain on/off.
     */
    protected final void cancelIfBound(@NotNull Cancellable event, @Nullable ItemStack stack) {
        if (stack == null) return;
        List<Binding<V>> current = bindings;
        for (Binding<V> vBinding : current) {
            if (vBinding.item.isCustomItem(stack)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    /**
     * Runs {@code action} for every bound item, for restrictions whose value decides whether the
     * event is in scope at all.
     */
    protected final void forEachBinding(@NotNull BiConsumer<CustomItemApi, V> action) {
        List<Binding<V>> current = bindings;
        for (Binding<V> binding : current) {
            action.accept(binding.item, binding.value);
        }
    }

    /** Cancels {@code event} if {@code stack} is {@code item}. */
    protected final void cancel(@NotNull Cancellable event, @NotNull CustomItemApi item, @Nullable ItemStack stack) {
        if (item.isCustomItem(stack)) event.setCancelled(true);
    }

    private record Binding<V>(CustomItemApi item, V value) {}
}
