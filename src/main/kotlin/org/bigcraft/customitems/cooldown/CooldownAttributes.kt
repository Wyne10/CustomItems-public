package org.bigcraft.customitems.cooldown

import me.wyne.wutils.common.Ticks
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

val COOLDOWN_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "message" to AttributeFactory { key, config -> MessageAttribute(key, config.getString(key)!!) },
        "visual" to AttributeFactory { key, config -> VisualAttribute(key, config.getBoolean(key)) }
    )
)

abstract class CooldownAttribute<V : Any>(key: String, value: V) : AttributeBase<V>(key, value) {
    open fun onTest(player: Player, customItem: CustomItem, item: ItemStack, cooldown: Cooldown) = Unit
    open fun onApply(player: Player, customItem: CustomItem, item: ItemStack, cooldown: Cooldown) = Unit
}

class MessageAttribute(key: String, message: String) : CooldownAttribute<String>(key, message) {
    override fun onTest(player: Player, customItem: CustomItem, item: ItemStack, cooldown: Cooldown) {
        player.placeholderComponent(value, "remaining" replace cooldown.getRemainingFormat(player, item, "s")).sendMessage(player)
    }
}

class VisualAttribute(key: String, isActive: Boolean) : CooldownAttribute<Boolean>(key, isActive) {
    override fun onApply(player: Player, customItem: CustomItem, item: ItemStack, cooldown: Cooldown) {
        if (!value) return
        player.setCooldown(customItem.material, Ticks.ofMillis(cooldown.value).toInt())
    }
}