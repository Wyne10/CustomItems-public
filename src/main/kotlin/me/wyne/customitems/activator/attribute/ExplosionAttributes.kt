package me.wyne.customitems.activator.attribute

import me.wyne.wutils.common.location.LocationUtils
import me.wyne.wutils.common.scheduler.Schedulers
import me.wyne.wutils.animation.data.AnimationParticle
import me.wyne.wutils.animation.runnable.ParticleMap
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.config.ConfigUtils
import me.wyne.wutils.common.duration.Durations.getMillis
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.common.range.iterator.CircleEdgeIterator
import me.wyne.wutils.common.vector.VectorUtils
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.attribute.EnchantmentAttribute
import me.wyne.wutils.config.configurables.item.attribute.MaterialAttribute
import me.wyne.customitems.activator.ActivatorAttribute
import me.wyne.customitems.api.spi.CompositeConfigFactory
import me.wyne.customitems.item.CustomItem
import me.wyne.customitems.spi.Registries
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class PlayerExplosionAttribute(
    key: String,
    offset: Vector,
    power: Float,
    private val setFire: Boolean,
    private val breakBlocks: Boolean,
) : ActivatorAttribute<Pair<Vector, Float>>(key, offset to power) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.location.world.createExplosion(LocationUtils.addRelative(player.eyeLocation, value.first), value.second, setFire, breakBlocks)
    }

    object Factory : CompositeConfigFactory<PlayerExplosionAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): PlayerExplosionAttribute =
            PlayerExplosionAttribute(
                key,
                ConfigUtils.getVectorOrZero(section, "offset"),
                section.getDouble("power").toFloat(),
                section.getBoolean("set-fire"),
                section.getBoolean("break-blocks")
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): PlayerExplosionAttribute {
            val args = Args(string)
            return PlayerExplosionAttribute(
                key,
                VectorUtils.getVectorOrZero(args.get(0)),
                args.get(1, "1.0").toFloat(),
                args.get(2, "false").toBoolean(),
                args.get(3, "false").toBoolean(),
            )
        }
    }
}