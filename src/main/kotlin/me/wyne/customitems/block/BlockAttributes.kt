package me.wyne.customitems.block

import com.destroystokyo.paper.event.block.AnvilDamagedEvent
import de.tr7zw.nbtapi.NBT
import me.wyne.wutils.common.Args
import me.wyne.wutils.common.config.ConfigUtils
import me.wyne.wutils.common.inventory.InventoryUtils
import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.common.kotlin.item.dropActuallyNaturally
import me.wyne.customitems.api.CustomItemApi
import me.wyne.customitems.api.spi.BlockAttributeType
import me.wyne.customitems.api.spi.InstanceRegistry
import me.wyne.customitems.item.CustomItemManager
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

internal fun registerBlockAttributeTypes(registry: InstanceRegistry<BlockAttributeType<*>>) {
    registry.register(BreakDropAttribute)
    registry.register(DropDropAttribute)
    registry.register(BreakMagnetAttribute)
    registry.register(BurnDropAttribute)
    registry.register(ExplodeDropAttribute)
    registry.register(FurnaceSpeedAttribute)
    registry.register(UnbreakableAnvilAttribute)
    registry.register(RandomSpawnerAttribute)
    registry.register(DisablePhysicsAttribute)
}

/**
 * A block attribute whose value names what the block drops: `true` for the item itself, a material
 * name, or another custom item's key.
 */
abstract class DropAttributeType(key: String) : BlockAttributeType<String>(key) {

    override fun parse(key: String, config: ConfigurationSection): String = config.getString(key).orEmpty()

    override fun isActive(value: String) = value.isNotBlank()

    protected fun createDrop(item: CustomItemApi, drop: String, player: Player?): ItemStack {
        drop.toBooleanStrictOrNull()?.let { if (it) return item.createItemStack(player) }
        ConfigUtils.getByName(drop, Material::class.java)?.let { return ItemStack(it) }
        CustomItemManager.instance.getItem(drop)?.let { return it.createItemStack(player) }
        return ItemStack(Material.AIR)
    }
}

object BreakDropAttribute : DropAttributeType("break-drop") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBreak(event: BlockBreakEvent) = forBlock(event.block) { item, drop ->
        event.isDropItems = false
        (event.block.state as? Container)?.inventory
            ?.filterNotNull()
            ?.forEach { event.block.world.dropItemNaturally(event.block.location, it) }
        listOf(createDrop(item, drop, event.player)).dropActuallyNaturally(event)
    }
}

object DropDropAttribute : DropAttributeType("drop-drop") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onDropItem(event: BlockDropItemEvent) = forBlock(event.blockState.block) { item, drop ->
        event.items.clear()
        event.items.add(event.block.world.dropItemNaturally(event.block.location, createDrop(item, drop, event.player)))
    }
}

object BreakMagnetAttribute : DropAttributeType("break-magnet") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBreak(event: BlockBreakEvent) = forBlock(event.block) { item, drop ->
        event.isDropItems = false
        (event.block.state as? Container)?.inventory
            ?.filterNotNull()
            ?.forEach { InventoryUtils.addOrDrop(event.player, it) }
        InventoryUtils.addOrDrop(event.player, createDrop(item, drop, event.player))
    }
}

object BurnDropAttribute : DropAttributeType("burn-drop") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBurn(event: BlockBurnEvent) = forBlock(event.block) { item, drop ->
        event.block.world.dropItemNaturally(event.block.location, createDrop(item, drop, null))
    }
}

object ExplodeDropAttribute : DropAttributeType("explode-drop") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onExplode(event: EntityExplodeEvent) = event.blockList().forEach(::dropExploded)

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    fun onBlockExplode(event: BlockExplodeEvent) = event.blockList().forEach(::dropExploded)

    private fun dropExploded(block: org.bukkit.block.Block) = forBlock(block) { item, drop ->
        block.world.dropItemNaturally(block.location, createDrop(item, drop, null))
        block.type = Material.AIR
    }
}

object FurnaceSpeedAttribute : BlockAttributeType<Double>("furnace-speed") {
    override fun parse(key: String, config: ConfigurationSection): Double = config.getDouble(key)

    @EventHandler(ignoreCancelled = true)
    fun onPlace(event: BlockPlaceEvent) = forStack(event.itemInHand) { _, multiplier ->
        (event.block.state as? Furnace)?.let { furnace ->
            furnace.cookSpeedMultiplier = multiplier
            furnace.update()
        }
    }
}

object UnbreakableAnvilAttribute : BlockAttributeType<Boolean>("unbreakable-anvil") {
    override fun parse(key: String, config: ConfigurationSection): Boolean = config.getBoolean(key)

    @EventHandler(ignoreCancelled = true)
    fun onDamage(event: AnvilDamagedEvent) {
        val block = event.inventory.location?.block ?: return
        forBlock(block) { _, _ -> event.isCancelled = true }
    }
}

object RandomSpawnerAttribute : BlockAttributeType<RandomSpawnerAttribute.Spawns>("random-spawner") {

    data class Spawns(val weights: Map<EntityType, Int>, val nbt: Map<EntityType, String>)

    override fun parse(key: String, config: ConfigurationSection): Spawns {
        if (config.isList(key)) return Spawns(config.getStringList(key).toWeights(), emptyMap())
        val section = config.deep(key)
        return Spawns(
            section.getStringList("spawn").toWeights(),
            section.getStringList("nbt")
                .map { it.split(":", " ", limit = 2) }
                .associate { EntityType.valueOf(it[0]) to it[1] }
        )
    }

    override fun isActive(value: Spawns) = value.weights.isNotEmpty()

    @EventHandler(ignoreCancelled = true)
    fun onCreatureSpawn(event: SpawnerSpawnEvent) = forBlock(event.spawner.block) { _, spawns ->
        event.spawner.spawnedType = spawns.weights.weightedRandom() ?: return@forBlock
        spawns.nbt[event.entityType]?.let { nbt ->
            NBT.modify(event.entity) { it.mergeCompound(NBT.parseNBT(nbt)) }
        }
        event.spawner.update()
    }

    private fun List<String>.toWeights(): Map<EntityType, Int> =
        map { Args(it) }.associate { EntityType.valueOf(it[0]) to it[1].toInt() }
}

object DisablePhysicsAttribute : BlockAttributeType<Boolean>("disable-physics") {
    override fun parse(key: String, config: ConfigurationSection): Boolean = config.getBoolean(key)

    @EventHandler(ignoreCancelled = true)
    fun onChange(event: EntityChangeBlockEvent) = forBlock(event.block) { _, disable ->
        event.isCancelled = disable
    }
}

fun <K> Map<K, Int>.weightedRandom(): K? {
    if (isEmpty()) return null

    val totalWeight = values.sum()
    if (totalWeight <= 0) return null

    var roll = Random.nextInt(totalWeight)
    for ((entry, weight) in this) {
        roll -= weight
        if (roll < 0) return entry
    }
    return null
}
