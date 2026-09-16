package org.bigcraft.customitems.activator.attribute

import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.config.configurables.item.attribute.PotionEffectsAttribute
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.api.spi.ConfigFactory
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class EntityPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent && event.entity is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val target = event.entity as LivingEntity
        value.forEach { potionEffect ->
            potionEffect.apply(target)
        }
    }

    object Factory : ConfigFactory<EntityPotionEffectsAttribute> {
        override fun create(key: String, config: ConfigurationSection): EntityPotionEffectsAttribute {
            return EntityPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class EntityRemoveEffectsAttribute(
    key: String,
    effects: Set<PotionEffectType>
) : ActivatorAttribute<Set<PotionEffectType>>(key, effects) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent && event.entity is LivingEntity

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        val target = event.entity as LivingEntity
        value.forEach { potionEffect ->
            target.removePotionEffect(potionEffect)
        }
    }
}

class PlayerPotionEffectsAttribute(
    key: String,
    effects: Set<PotionEffect>
) : ActivatorAttribute<Set<PotionEffect>>(key, effects) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        Schedulers.sync().runLater({
            value.forEach { potionEffect ->
                potionEffect.apply(player)
            }
        }, 1L)
    }

    object Factory : ConfigFactory<PlayerPotionEffectsAttribute> {
        override fun create(key: String, config: ConfigurationSection): PlayerPotionEffectsAttribute {
            return PlayerPotionEffectsAttribute(
                key,
                PotionEffectsAttribute(key, config).value.map { it.value }.toSet()
            )
        }
    }
}

class PlayerRemoveEffectsAttribute(
    key: String,
    effects: Set<PotionEffectType>
) : ActivatorAttribute<Set<PotionEffectType>>(key, effects) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.forEach { potionEffect ->
            player.removePotionEffect(potionEffect)
        }
    }
}
