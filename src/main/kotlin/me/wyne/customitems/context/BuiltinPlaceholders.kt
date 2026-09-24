package me.wyne.customitems.context

import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.i18n.I18n
import me.wyne.wutils.i18n.kotlin.miniMessage
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.customitems.api.CustomItemApi
import me.wyne.customitems.api.spi.CooldownContext
import me.wyne.customitems.api.spi.ItemPlaceholderProvider
import me.wyne.customitems.api.spi.ItemPlaceholderSink
import me.wyne.customitems.composition.ComposedKeys
import me.wyne.customitems.i18n
import me.wyne.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

internal fun registerBuiltinPlaceholders(register: (ItemPlaceholderProvider) -> Unit) {
    register(StackPlaceholders)
    register(CooldownPlaceholders)
    register(ComposedListPlaceholder)
}

/** `<durability>`, `<max-durability>` and `<amount>`, read off the stack itself. */
object StackPlaceholders : ItemPlaceholderProvider {
    override fun contribute(sink: ItemPlaceholderSink, item: CustomItemApi, stack: ItemStack, player: Player?) {
        sink.text("durability", stack.durability.toString())
        sink.text("max-durability", stack.type.maxDurability.toString())
        sink.text("amount", stack.amount.toString())
    }
}

/** `<cooldown-<key>>` for every cooldown the item declares, formatted in seconds. */
object CooldownPlaceholders : ItemPlaceholderProvider {
    override fun contribute(sink: ItemPlaceholderSink, item: CustomItemApi, stack: ItemStack, player: Player?) {
        if (player == null || item !is CustomItem) return
        val context = CooldownContext(player, item, stack)
        item.cooldowns.keys.forEach { key ->
            val remaining = item.cooldowns[key]?.getRemainingMillis(context) ?: 0L
            sink.text("cooldown-$key", Period(System.currentTimeMillis() + remaining).getRemainingStringFormat("s"))
        }
    }
}

/** `<composed-list>`, one lore line per item composed into the stack. */
object ComposedListPlaceholder : ItemPlaceholderProvider {
    override fun contribute(sink: ItemPlaceholderSink, item: CustomItemApi, stack: ItemStack, player: Player?) {
        sink.lines(
            "composed-list",
            ComposedKeys.of(stack).keys.map { composed ->
                i18n.accessor(player, "format-composable-entry")
                    .getPlaceholderComponent(I18n.toPlayer(player), "key" replace composed)
                    .miniMessage()
            }
        )
    }
}

/** Durability as "hits left", matching how the `<durability>` placeholder has always read. */
val ItemStack.durability: Int
    get() = type.maxDurability - ((itemMeta as? Damageable)?.damage ?: 0)
