package org.bigcraft.customitems.attribute

import me.wyne.wutils.common.Args
import me.wyne.wutils.common.ConfigUtils
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.VectorUtils
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.ItemConfigurable
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.CompositeAttributeFactory
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import me.wyne.wutils.config.configurables.item.attribute.EnchantmentAttribute
import me.wyne.wutils.config.configurables.item.attribute.MaterialAttribute
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.item.CustomItem
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class PlayerExplosionAttribute(
    key: String,
    offset: Vector,
    power: Float,
    private val setFire: Boolean,
    private val breakBlocks: Boolean,
) : ActivatorAttribute<Pair<Vector, Float>>(key, offset to power) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.location.world.createExplosion(player.eyeLocation.getRelative(value.first), value.second, setFire, breakBlocks)
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): Attribute<*> =
            PlayerExplosionAttribute(
                key,
                ConfigUtils.getVectorOrZero(section, "offset"),
                section.getDouble("power").toFloat(),
                section.getBoolean("set-fire"),
                section.getBoolean("break-blocks")
            )

        override fun fromString(key: String, string: String): Attribute<*> {
            val args = Args(string, " ")
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

// TODO Maybe get rid of registerable listener
class JackOLanternAttribute(
    key: String,
    radius: Double,
    durationMillis: Long
) : ActivatorAttribute<Pair<Double, Long>>(key, radius to durationMillis), RegisterableListener {

    init {
        CustomItemManager.instance.activatorRegistry.register(this)
    }

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val players = player.location.getNearbyPlayers(value.first)
            .filterNot { it == player }
            .filter { it.inventory.getItem(EquipmentSlot.HEAD) == null }
        players.forEach { it.inventory.setItem(EquipmentSlot.HEAD, JACK_O_LANTERN_ITEM) }
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            players.forEach { it.inventory.setItem(EquipmentSlot.HEAD, null) }
        }, Ticks.ofMillis(value.second))
    }

    // Fail safe
    @EventHandler
    private fun onDisconnect(event: PlayerQuitEvent) {
        if (event.player.inventory.getItem(EquipmentSlot.HEAD) != JACK_O_LANTERN_ITEM) return
        event.player.inventory.setItem(EquipmentSlot.HEAD, null)
    }

    companion object {
        val JACK_O_LANTERN_ITEM: ItemStack = ItemConfigurable(
            ImmutableAttributeContainer.builder()
                .with(MaterialAttribute(Material.JACK_O_LANTERN))
                .with(PdcTagAttribute("ci-key", PdcTagAttribute.PdcTag(PersistentDataType.STRING as PersistentDataType<Any, Any>, "jack-o-lantern")))
                .with(EnchantmentAttribute(EnchantmentAttribute.EnchantmentData(Enchantment.BINDING_CURSE, 1)))
                .buildImmutable()
        ).build()
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): Attribute<*> =
            JackOLanternAttribute(
                key,
                section.getDouble("radius"),
                section.getMillis("duration")
            )

        override fun fromString(key: String, string: String): Attribute<*> {
            val args = Args(string, " ")
            return JackOLanternAttribute(
                key,
                args.get(0, "0.0").toDouble(),
                getMillis(args.get(1, "0s"))
            )
        }
    }
}

// TODO Test with custom mending
class RepairAttribute(
    key: String,
    slots: Set<EquipmentSlot>
) : ActivatorAttribute<Set<EquipmentSlot>>(key, slots) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.mapNotNull { player.inventory.getItem(it) }
            .filter { it.containsEnchantment(Enchantment.MENDING) }
            .forEach { it.editMeta { meta -> (meta as? Damageable)?.damage = 0 } }
    }
}

class ForceFieldAttribute(
    key: String,
    forceFieldData: me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData
) : ActivatorAttribute<me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData>(key, forceFieldData) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val location = player.location.clone().add(value.offset)
        location.world.getNearbyPlayers(location, value.radius)
            .filterNot { it == player }
            .forEach { it.velocity = it.location.toVector().subtract(location.toVector()).multiply(value.velocity) }
    }

    object Factory : CompositeAttributeFactory {
        override fun fromSection(key: String, section: ConfigurationSection): Attribute<*> =
            ForceFieldAttribute(
                key,
                me.wyne.wutils.config.configurables.animation.attribute.ForceFieldAttribute.ForceFieldData(
                    ConfigUtils.getVectorOrZero(section, "offset"),
                    section.getDouble("radius", 1.0),
                    section.getDouble("velocity", 1.0)
                )
            )

        override fun fromString(key: String, string: String): Attribute<*> {
            val args = Args(string, " ")
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