package org.bigcraft.customitems.api.spi;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A kind of activator — a Bukkit event, plus the rule for finding the player it concerns.
 * <p>
 * One instance serves every custom item that declares it: subclass this, add {@code @EventHandler}
 * methods that call {@link #dispatch}, and register the instance with
 * {@link CustomItemsRegistries#activatorTypes()}. CustomItems registers it with Bukkit once, and
 * re-binds items to it on every reload.
 *
 * <pre>{@code
 * public final class ShearActivator extends ActivatorType {
 *     public ShearActivator() { super("player-shear"); }
 *
 *     @EventHandler(ignoreCancelled = true)
 *     public void onShear(PlayerShearEntityEvent event) {
 *         dispatch(event, event.getPlayer());
 *     }
 * }
 * }</pre>
 */
public abstract class ActivatorType implements KeyedExtension, Listener {

    private final String key;
    private volatile List<ActivatorBinding> bindings = List.of();

    protected ActivatorType(@NotNull String key) {
        this.key = key;
    }

    @Override
    public final @NotNull String getKey() {
        return key;
    }

    public final void bind(@NotNull ActivatorBinding binding) {
        List<ActivatorBinding> updated = new java.util.ArrayList<>(bindings);
        updated.add(binding);
        bindings = List.copyOf(updated);
    }

    public final void clearBindings() {
        bindings = List.of();
    }

    /**
     * Fires every bound item whose {@link ItemIdentifier} finds a stack on {@code player}.
     */
    protected final void dispatch(@NotNull Event event, @NotNull Player player) {
        List<ActivatorBinding> current = bindings;
        for (ActivatorBinding activatorBinding : current) {
            activatorBinding.activate(event, player);
        }
    }

    /**
     * Fires every bound item that {@code stack} belongs to, bypassing the {@link ItemIdentifier}.
     * Use when the event carries the stack itself — a dropped item, a consumed item — and looking
     * it up in the player's inventory would fail.
     */
    protected final void dispatchForStack(@NotNull Event event, @NotNull Player player, @Nullable ItemStack stack) {
        List<ActivatorBinding> current = bindings;
        for (ActivatorBinding activatorBinding : current) {
            activatorBinding.activateForStack(event, player, stack);
        }
    }
}
