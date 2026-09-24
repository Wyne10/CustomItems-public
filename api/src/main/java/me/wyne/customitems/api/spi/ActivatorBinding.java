package me.wyne.customitems.api.spi;

import me.wyne.customitems.api.CustomItemApi;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * One custom item's declaration of an activator: which stack it acts on, what has to be true, and
 * what happens. An {@link ActivatorType} fires every binding registered against it.
 */
public final class ActivatorBinding {

    private final CustomItemApi item;
    private final ItemIdentifier identifier;
    private final List<ActivatorAttribute> attributes;
    private final List<ActivatorCondition> conditions;
    private final List<ActivatorAttribute> onConditionFail;

    public ActivatorBinding(
            @NotNull CustomItemApi item,
            @NotNull ItemIdentifier identifier,
            @NotNull List<ActivatorAttribute> attributes,
            @NotNull List<ActivatorCondition> conditions,
            @NotNull List<ActivatorAttribute> onConditionFail
    ) {
        this.item = item;
        this.identifier = identifier;
        this.attributes = List.copyOf(attributes);
        this.conditions = List.copyOf(conditions);
        this.onConditionFail = List.copyOf(onConditionFail);
    }

    public @NotNull CustomItemApi getItem() {
        return item;
    }

    /**
     * Resolves the stack through this binding's {@link ItemIdentifier} and fires. Does nothing if
     * the identifier finds no stack.
     */
    public void activate(@NotNull Event event, @NotNull Player player) {
        ItemStack stack = identifier.getItem(player, item);
        if (stack == null) return;
        fire(event, player, stack);
    }

    /**
     * Fires against {@code stack} directly, for events that carry the stack themselves rather than
     * leaving it to be found in the player's inventory. Does nothing unless {@code stack} is this
     * binding's item.
     */
    public void activateForStack(@NotNull Event event, @NotNull Player player, @Nullable ItemStack stack) {
        if (stack == null || !item.isCustomItem(stack)) return;
        fire(event, player, stack);
    }

    private void fire(Event event, Player player, ItemStack stack) {
        for (ActivatorCondition condition : conditions) {
            if (!condition.appliesTo(event)) continue;
            if (condition.test(event, player, item, stack)) continue;
            for (ActivatorAttribute attribute : onConditionFail) {
                attribute.apply(event, player, item, stack);
            }
            return;
        }
        for (ActivatorAttribute attribute : attributes) {
            if (attribute.isApplicable(event, player, item, stack)) {
                attribute.apply(event, player, item, stack);
            }
        }
    }
}
