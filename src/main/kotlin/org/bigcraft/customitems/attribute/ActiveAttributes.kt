package org.bigcraft.customitems.attribute

import me.wyne.wutils.common.Args
import me.wyne.wutils.common.ConfigUtils
import me.wyne.wutils.common.VectorUtils
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.CompositeAttributeFactory
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class PlayerExplosionAttribute(
    key: String,
    offset: Vector,
    power: Float
) : ActivatorAttribute<Pair<Vector, Float>>(key, offset to power) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.location.world.createExplosion(player.eyeLocation.getRelative(value.first), value.second)
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): Attribute<*> =
            PlayerExplosionAttribute(
                key,
                ConfigUtils.getVectorOrZero(section, "offset"),
                section.getDouble("power").toFloat()
            )

        override fun fromString(key: String, string: String): Attribute<*> {
            val args = Args(string, " ")
            return PlayerExplosionAttribute(
                key,
                VectorUtils.getVectorOrZero(args.get(0)),
                args.get(1, "1.0").toFloat()
            )
        }
    }
}

fun Location.getRelative(localOffset: Vector): Location {
    if (localOffset.isEmpty())
        return this
    val forward = direction
    val right = forward.clone().crossProduct(UP)
    val up = right.clone().crossProduct(forward)
    val worldOffset = right.multiply(localOffset.x)
        .add(up.multiply(localOffset.y))
        .add(forward.multiply(localOffset.z))
    return this.clone().add(worldOffset)
}

fun Vector.isEmpty(): Boolean =
    this.x == 0.0 && this.y == 0.0 && this.z == 0.0

val UP = BlockFace.UP.direction