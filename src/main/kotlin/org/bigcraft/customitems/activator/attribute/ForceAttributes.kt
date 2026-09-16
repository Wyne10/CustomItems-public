package org.bigcraft.customitems.activator.attribute

import me.wyne.wutils.common.Args
import me.wyne.wutils.common.config.ConfigUtils
import me.wyne.wutils.common.location.LocationUtils
import me.wyne.wutils.common.vector.VectorUtils
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.api.spi.CompositeConfigFactory
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class ForceFieldAttribute(
    key: String,
    forceFieldData: me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData
) : ActivatorAttribute<me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData>(key, forceFieldData) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val location = player.location.clone().add(value.offset)
        location.world.getNearbyPlayers(location, value.radius)
            .filterNot { it == player }
            .forEach { it.velocity = it.location.toVector().subtract(location.toVector()).normalize().multiply(value.velocity) }
    }

    object Factory : CompositeConfigFactory<ForceFieldAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): ForceFieldAttribute =
            ForceFieldAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    ConfigUtils.getVectorOrZero(section, "offset"),
                    section.getDouble("radius", 1.0),
                    section.getDouble("velocity", 1.0)
                )
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): ForceFieldAttribute {
            val args = Args(string)
            return ForceFieldAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    VectorUtils.getVectorOrZero(args.get(2)),
                    args.get(0, "1.0").toDouble(),
                    args.get(1, "1.0").toDouble()
                )
            )
        }
    }
}

class ForceAttribute(
    key: String,
    forceFieldData: me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData
) : ActivatorAttribute<me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData>(key, forceFieldData) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val location = player.location.clone().add(value.offset)
        player.velocity = player.location.toVector().subtract(location.toVector()).normalize().multiply(value.velocity)
    }

    object Factory : CompositeConfigFactory<ForceAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): ForceAttribute =
            ForceAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    ConfigUtils.getVectorOrZero(section, "offset"),
                    section.getDouble("radius", 1.0),
                    section.getDouble("velocity", 1.0)
                )
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): ForceAttribute {
            val args = Args(string)
            return ForceAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    VectorUtils.getVectorOrZero(args.get(2)),
                    args.get(0, "1.0").toDouble(),
                    args.get(1, "1.0").toDouble()
                )
            )
        }
    }
}

class RelativeForceAttribute(
    key: String,
    forceFieldData: me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData
) : ActivatorAttribute<me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData>(key, forceFieldData) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val location = LocationUtils.addRelative(player.location, value.offset)
        player.velocity = player.location.toVector().subtract(location.toVector()).normalize().multiply(value.velocity)
    }

    object Factory : CompositeConfigFactory<RelativeForceAttribute> {
        override fun fromSection(key: String, section: ConfigurationSection): RelativeForceAttribute =
            RelativeForceAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    ConfigUtils.getVectorOrZero(section, "offset"),
                    section.getDouble("radius", 1.0),
                    section.getDouble("velocity", 1.0)
                )
            )

        override fun fromString(key: String, string: String, config: ConfigurationSection): RelativeForceAttribute {
            val args = Args(string)
            return RelativeForceAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    VectorUtils.getVectorOrZero(args.get(2)),
                    args.get(0, "1.0").toDouble(),
                    args.get(1, "1.0").toDouble()
                )
            )
        }
    }
}

fun Vector.isEmpty(): Boolean =
    this.x == 0.0 && this.y == 0.0 && this.z == 0.0