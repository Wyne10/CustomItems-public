package org.bigcraft.customitems.attribute

import me.wyne.wutils.common.cooldown.CooldownMap
import me.wyne.wutils.common.cooldown.Period
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import java.util.UUID

val COOLDOWN_MAP = AttributeMap(
    mapOf(
        "player-cooldown" to AttributeFactory { key, config -> PlayerCooldown(key, config.getMillis(key)) },
        "global-cooldown" to AttributeFactory { key, config -> GlobalCooldown(key, config.getMillis(key)) }
    )
)

abstract class Cooldown<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    abstract fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean
    abstract fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack)
}

class PlayerCooldown(key: String, durationMillis: Long) : Cooldown<Long>(key, durationMillis) {

    private val cooldownMap = CooldownMap<UUID>()

    override fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        cooldownMap.isCooldowned(player.uniqueId)

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        cooldownMap.put(player.uniqueId, value)
    }

}

class GlobalCooldown(key: String, durationMillis: Long) : Cooldown<Long>(key, durationMillis) {

    private val cooldown = Period()

    override fun isCooldowned(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        !cooldown.isExpired

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        cooldown.put(value)
    }

}