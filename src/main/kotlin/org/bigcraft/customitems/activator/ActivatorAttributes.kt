package org.bigcraft.customitems.activator

import me.wyne.wutils.common.kotlin.config.getDoubleOperation
import me.wyne.wutils.common.kotlin.config.getEnumSet
import me.wyne.wutils.common.kotlin.config.getIntOperation
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.common.kotlin.config.getPotionTypeEnumSet
import me.wyne.wutils.config.configurables.InteractionListConfigurable
import me.wyne.wutils.config.configurables.gui.attribute.SoundAttribute
import net.kyori.adventure.sound.Sound
import org.bigcraft.customitems.activator.attribute.*
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.api.spi.ActivatorAttribute as ActivatorAttributeApi
import org.bigcraft.customitems.api.spi.FactoryRegistry
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

internal fun registerActivatorAttributes(registry: FactoryRegistry<ActivatorAttributeApi>) {
    registry.register("condition-cancel-event") { key, config -> CancelEventAttribute(key, config.getBoolean(key)) }
    registry.register("cooldown-cancel-event") { key, config -> CooldownCancelEventAttribute(key, config.getBoolean(key)) }
    registry.register("cancel-event") { key, config -> CancelEventAttribute(key, config.getBoolean(key)) }
    registry.register("commands") { key, config -> CommandsAttribute(key, config.getStringList(key)) }
    registry.register("cooldown") { key, config -> CooldownAttribute(key, config.getStringList(key).toSet()) }
    registry.register("steal-level") { key, config -> LevelStealAttribute(key, config.getDouble(key)) }
    registry.register("entity-potion-effects", EntityPotionEffectsAttribute.Factory)
    registry.register("player-potion-effects", PlayerPotionEffectsAttribute.Factory)
    registry.register("entity-remove-effects") { key, config -> EntityRemoveEffectsAttribute(key, config.getPotionTypeEnumSet(key)) }
    registry.register("player-remove-effects") { key, config -> PlayerRemoveEffectsAttribute(key, config.getPotionTypeEnumSet(key)) }
    registry.register("consume-projectile") { key, config -> ConsumeProjectileAttribute(key, config.getBoolean(key)) }
    registry.register("set-projectile-meta") { key, config -> ProjectileMetaAttribute(key, config.getString(key)!!) }
    registry.register("set-damager-health") { key, config -> DamagerHealthAttribute(key, config.getDoubleOperation(key)) }
    registry.register("lock-drop") { key, config -> DropLockAttribute(key, config.getMillis(key)) }
    registry.register("player-explosion", PlayerExplosionAttribute.Factory)
    registry.register("jack-o-lantern", JackOLanternAttribute.Factory)
    registry.register("repair") { key, config -> RepairAttribute(key, config.getEnumSet<EquipmentSlot>(key)) }
    registry.register("force-field", ForceFieldAttribute.Factory)
    registry.register("force", ForceAttribute.Factory)
    registry.register("relative-force", RelativeForceAttribute.Factory)
    registry.register("action-bar") { key, config -> ActionBarAttribute(key, config.getString(key)!!) }
    registry.register("player-message") { key, config -> PlayerMessageAttribute(key, config.getString(key)!!) }
    registry.register("player-interaction") { key, config -> InteractionAttribute(key, InteractionListConfigurable().also { it.fromConfig(config.get(key)) }) }
    registry.register("player-sound") { key, config -> PlayerSoundAttribute(key, SoundAttribute.Factory().create(key, config).value as Sound) }
    registry.register("local-sound") { key, config -> LocalSoundAttribute(key, SoundAttribute.Factory().create(key, config).value as Sound) }
    registry.register("set-player-exp") { key, config -> PlayerExpAttribute(key, config.getIntOperation(key)) }
    registry.register("set-custom-block") { key, config -> SetCustomBlockAttribute(key, config.getString(key)!!) }
    registry.register("call-entity-damage", CallEntityDamageAttribute.Factory)
    registry.register("cause-entity-damage", CauseEntityDamageAttribute.Factory)
    registry.register("create-aoe", CreateAoeAttribute.Factory)
    registry.register("locate-compass", CompassLocatorAttribute.Factory)
    registry.register("set-item-amount") { key, config -> ItemAmountAttribute(key, config.getIntOperation(key)) }
    registry.register("set-item-durability") { key, config -> ItemDurabilityAttribute(key, config.getIntOperation(key)) }
    registry.register("set-item-owner") { key, config -> ItemOwnerAttribute(key, config.getBoolean(key)) }
    registry.register("damage-item") { key, config -> DamageItemAttribute(key, config.getInt(key)) }
    registry.register("update-view") { key, config -> UpdateViewAttribute(key, config.getBoolean(key)) }
}

abstract class ActivatorAttribute<V : Any>(val key: String, val value: V) : ActivatorAttributeApi {

    final override fun isApplicable(event: Event, player: Player, item: CustomItemApi, stack: ItemStack): Boolean =
        isApplicable(event, player, item as CustomItem, stack)

    final override fun apply(event: Event, player: Player, item: CustomItemApi, stack: ItemStack) =
        apply(event, player, item as CustomItem, stack)

    open fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) = true
    abstract fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack)
}
