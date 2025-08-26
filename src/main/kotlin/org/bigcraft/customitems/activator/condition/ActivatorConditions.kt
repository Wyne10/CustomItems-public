package org.bigcraft.customitems.activator.condition

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.config.getIntComparator
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

val ACTIVATOR_CONDITION_MAP = AttributeMap(
    mutableMapOf(
        "is-sneaking" to AttributeFactory { key, config -> SneakingCondition(key, config.getBoolean(key)) },
        "is-sprinting" to AttributeFactory { key, config -> SprintingCondition(key, config.getBoolean(key)) },
        "is-flying" to AttributeFactory { key, config -> FlyingCondition(key, config.getBoolean(key)) },
        "is-blocking" to AttributeFactory { key, config -> BlockingCondition(key, config.getBoolean(key)) },
        "is-gliding" to AttributeFactory { key, config -> GlidingCondition(key, config.getBoolean(key)) },
        "is-swimming" to AttributeFactory { key, config -> SwimmingCondition(key, config.getBoolean(key)) },
        "is-on-fire" to AttributeFactory { key, config -> OnFireCondition(key, config.getBoolean(key)) },
        "is-in-air" to AttributeFactory { key, config -> InAirCondition(key, config.getBoolean(key)) },
        "light-level" to AttributeFactory { key, config -> LightLevelCondition(key, config.getIntComparator(key)) }
    )
)

abstract class ActivatorCondition<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    abstract fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean
}