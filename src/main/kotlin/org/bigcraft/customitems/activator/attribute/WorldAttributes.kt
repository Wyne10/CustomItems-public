package org.bigcraft.customitems.activator.attribute

import me.wyne.wutils.animation.data.AnimationParticle
import me.wyne.wutils.animation.runnable.ParticleArray
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.common.range.LocationRange
import me.wyne.wutils.common.range.iterator.VectorRangeEdgeIterator
import me.wyne.wutils.config.configurables.animation.attribute.AnimationParticleAttribute
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.aoe.AoeManager
import org.bigcraft.customitems.aoe.AreaOfEffect
import org.bigcraft.customitems.api.spi.CompositeConfigFactory
import org.bigcraft.customitems.api.spi.ConfigFactory
import org.bigcraft.customitems.block.CustomBlockManager
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.block.BlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class SetCustomBlockAttribute(
    key: String,
    blockKey: String
) : ActivatorAttribute<String>(key, blockKey) {

    private val customBlock by lazy {
        CustomItemManager.instance.getItem(value)!!
    }

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        customBlock.isBlock && (event is BlockEvent || event is PlayerInteractEvent)

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        if (event is BlockEvent) {
            val block = event.block
            if (block.type != customBlock.material)
                block.type = customBlock.material
            CustomBlockManager.instance.placeBlock(block, customBlock)
        } else if (event is PlayerInteractEvent) {
            val block = event.clickedBlock ?: return
            if (block.type != customBlock.material)
                block.type = customBlock.material
            CustomBlockManager.instance.placeBlock(block, customBlock)
        }
    }
}

open class CallEntityDamageAttribute(
    key: String,
    cause: EntityDamageEvent.DamageCause,
    damage: Double
) : ActivatorAttribute<Pair<EntityDamageEvent.DamageCause, Double>>(key, cause to damage) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is EntityEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as EntityEvent
        EntityDamageByEntityEvent(player, event.entity, value.first, value.second).callEvent()
    }

    object Factory : CompositeConfigFactory<CallEntityDamageAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): CallEntityDamageAttribute =
            CallEntityDamageAttribute(
                key,
                runCatching { EntityDamageEvent.DamageCause.valueOf(section.getString("cause")!!) }.getOrDefault(EntityDamageEvent.DamageCause.ENTITY_ATTACK),
                section.getDouble("damage")
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): CallEntityDamageAttribute {
            val args = Args(string)
            return CallEntityDamageAttribute(
                key,
                runCatching { EntityDamageEvent.DamageCause.valueOf(args.get(0, "ENTITY_ATTACK")) }.getOrDefault(EntityDamageEvent.DamageCause.ENTITY_ATTACK),
                args.get(1, "0.0").toDouble()
            )
        }
    }
}

class CreateAoeAttribute(
    key: String,
    aoeKey: String,
    private val radius: Double,
    private val durationMillis: Long,
    private val particle: AnimationParticle?
) : ActivatorAttribute<String>(key, aoeKey) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val bounds = LocationRange(player.location, radius)
        val renderer = if (particle != null) ParticleArray(player.world,
            VectorRangeEdgeIterator(bounds, 1.0).asSequence().toSet(), particle) else null
        AoeManager.instance.startAoe(
            AreaOfEffect(
                value,
                player,
                durationMillis,
                bounds,
                renderer
            )
        )
    }

    object Factory : ConfigFactory<CreateAoeAttribute> {
        override fun create(key: String, config: ConfigurationSection): CreateAoeAttribute {
            val section = config.deep(key)
            return CreateAoeAttribute(
                key,
                section.getString("key")!!,
                section.getDouble("radius"),
                section.getMillis("duration"),
                AnimationParticleAttribute.Factory().fromSection(
                    "particle",
                    requireNotNull(section.getConfigurationSection("particle")) { "'create-aoe' requires a 'particle' section" }
                ).value
            )
        }
    }
}
