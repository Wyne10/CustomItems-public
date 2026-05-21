package org.bigcraft.customitems.block

import com.destroystokyo.paper.event.block.AnvilDamagedEvent
import de.tr7zw.nbtapi.NBT
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.config.ConfigUtils
import me.wyne.wutils.common.inventory.InventoryUtils
import me.wyne.wutils.common.kotlin.item.dropActuallyNaturally
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.item.CustomItemManager
import org.bigcraft.customitems.item.RegisterableCustomItemAttribute
import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.block.Furnace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockExplodeEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.SpawnerSpawnEvent
import org.bukkit.inventory.ItemStack
import kotlin.collections.iterator
import kotlin.random.Random

val BLOCK_ATTRIBUTE_MAP = AttributeMap(
    mapOf(
        "break-drop" to AttributeFactory { key, config -> BreakDropAttribute(key, config.getString(key)!!) },
        "drop-drop" to AttributeFactory { key, config -> DropDropAttribute(key, config.getString(key)!!) },
        "break-magnet" to AttributeFactory { key, config -> BreakMagnetAttribute(key, config.getString(key)!!) },
        "burn-drop" to AttributeFactory { key, config -> BurnDropAttribute(key, config.getString(key)!!) },
        "explode-drop" to AttributeFactory { key, config -> ExplodeDropAttribute(key, config.getString(key)!!) },
        "furnace-speed" to AttributeFactory { key, config -> FurnaceSpeedAttribute(key, config.getDouble(key)) },
        "unbreakable-anvil" to AttributeFactory { key, config -> UnbreakableAnvilAttribute(key, config.getBoolean(key)) },
        "random-spawner" to RandomSpawnerAttribute.Factory,
        "disable-physics" to AttributeFactory { key, config -> DisablePhysicsAttribute(key, config.getBoolean(key)) }
    )
)

abstract class BlockAttribute<V>(
    key: String,
    value: V
) : RegisterableCustomItemAttribute<V>(key, value, CustomBlockManager.instance.blockAttributesRegistry)

abstract class DropAttribute(
    key: String,
    drop: String
) : BlockAttribute<String>(key, drop) {
    fun createDrop(player: Player?): ItemStack {
        value.toBooleanStrictOrNull()?.let {
            if (it)
                return item.createItemStack(player)
        }
        ConfigUtils.getByName(value, Material::class.java)?.let {
            return ItemStack(it)
        }
        CustomItemManager.instance.getItem(value)?.let {
            return it.createItemStack(player)
        }
        return ItemStack(Material.AIR)
    }
}

class BreakDropAttribute(
    key: String,
    drop: String,
) : DropAttribute(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBreak(event: BlockBreakEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isDropItems = false
        val state = event.block.state
        if (state is Container)
            state.inventory
                .filterNotNull()
                .forEach { event.block.world.dropItemNaturally(event.block.location, it) }
        listOf(createDrop(event.player)).dropActuallyNaturally(event)
    }

}

class DropDropAttribute(
    key: String,
    drop: String
) : DropAttribute(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onDropItem(event: BlockDropItemEvent) {
        if (!item.isCustomItem(event.blockState.block)) return
        event.items.clear()
        event.items.add(event.block.world.dropItemNaturally(event.block.location, createDrop(event.player)))
    }

}

class BreakMagnetAttribute(
    key: String,
    drop: String,
) : DropAttribute(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBreak(event: BlockBreakEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isDropItems = false
        val state = event.block.state
        if (state is Container)
            state.inventory
                .filterNotNull()
                .forEach { InventoryUtils.addOrDrop(event.player, it) }
        InventoryUtils.addOrDrop(event.player, createDrop(event.player))
    }

}

class BurnDropAttribute(
    key: String,
    drop: String
) : DropAttribute(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBurn(event: BlockBurnEvent) {
        if (!item.isCustomItem(event.block)) return
        event.block.world.dropItemNaturally(event.block.location, createDrop(null))
    }

}

class ExplodeDropAttribute(
    key: String,
    drop: String
) : DropAttribute(key, drop) {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onExplode(event: EntityExplodeEvent) {
        event.blockList().forEach { block ->
            if (!item.isCustomItem(block)) return@forEach
            block.world.dropItemNaturally(block.location, createDrop(null))
            block.type = Material.AIR
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private fun onBlockExplode(event: BlockExplodeEvent) {
        event.blockList().forEach { block ->
            if (!item.isCustomItem(block)) return@forEach
            block.world.dropItemNaturally(block.location, createDrop(null))
            block.type = Material.AIR
        }
    }

}

class FurnaceSpeedAttribute(
    key: String,
    multiplier: Double
) : BlockAttribute<Double>(key, multiplier) {

    @EventHandler(ignoreCancelled = true)
    private fun onPlace(event: BlockPlaceEvent) {
        if (!item.isCustomItem(event.itemInHand)) return
        if (event.block.state !is Furnace) return
        (event.block.state as Furnace).let { furnace ->
            furnace.cookSpeedMultiplier = value
            furnace.update()
        }
    }

}

class UnbreakableAnvilAttribute(
    key: String,
    unbreakable: Boolean,
) : BlockAttribute<Boolean>(key, unbreakable) {

    @EventHandler(ignoreCancelled = true)
    private fun onDamage(event: AnvilDamagedEvent) {
        if (!item.isCustomItem((event.inventory.location!!.block))) return
        event.isCancelled = true
    }

}

class RandomSpawnerAttribute(
    key: String,
    types: Map<EntityType, Int>,
    private val nbt: Map<EntityType, String>
) : BlockAttribute<Map<EntityType, Int>>(key, types) {

    @EventHandler(ignoreCancelled = true)
    private fun onCreatureSpawn(event: SpawnerSpawnEvent) {
        if (!item.isCustomItem((event.spawner.block))) return
        event.spawner.spawnedType = value.weightedRandom() ?: return
        if (nbt.containsKey(event.entityType)) {
            NBT.modify(event.entity) { it.mergeCompound(NBT.parseNBT(nbt[event.entityType]!!)) }
        }
        event.spawner.update()
    }

    object Factory : AttributeFactory<RandomSpawnerAttribute> {
        override fun create(key: String, config: ConfigurationSection): RandomSpawnerAttribute {
            if (config.isList(key)) {
                val types = config.getStringList(key)
                    .map { Args(it) }
                    .associate { EntityType.valueOf(it[0]) to it[1].toInt() }
                return RandomSpawnerAttribute(key, types, emptyMap())
            } else {
                val section = config.getConfigurationSection(key) ?: return RandomSpawnerAttribute(key, emptyMap(), emptyMap())
                val types = section.getStringList("spawn")
                    .map { Args(it) }
                    .associate { EntityType.valueOf(it[0]) to it[1].toInt() }
                val nbt = section.getStringList("nbt")
                    .map { it.split(":", " ", limit = 2) }
                    .associate { EntityType.valueOf(it[0]) to it[1] }
                return RandomSpawnerAttribute(key, types, nbt)
            }
        }
    }

}

class DisablePhysicsAttribute(
    key: String,
    disable: Boolean
) : BlockAttribute<Boolean>(key, disable) {
    @EventHandler(ignoreCancelled = true)
    private fun onChange(event: EntityChangeBlockEvent) {
        if (!item.isCustomItem(event.block)) return
        event.isCancelled = value
    }
}

fun <K> Map<K, Int>.weightedRandom(): K? {
    if (isEmpty()) return null

    val totalWeight = values.sum()
    if (totalWeight <= 0) return null

    var randomWeight = Random.nextInt(totalWeight)
    for ((key, weight) in this) {
        if (weight <= 0) continue
        if (randomWeight < weight) return key
        randomWeight -= weight
    }

    return null
}

val <K> Map<K, Int>.percentage: Map<K, Double>
    get() = map { (key, weight) -> key to weight / values.sum().toDouble() }.toMap()