package org.bigcraft.customitems.activator

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.common.kotlin.config.deep
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.StructureType
import org.bukkit.block.Biome
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.CompassMeta

class CompassLocatorAttribute(
    key: String,
    locators: Set<CompassLocator>
) : ActivatorAttribute<Set<CompassLocator>>(key, locators) {

    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        item.type == Material.COMPASS && item.itemMeta is CompassMeta

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val candidates = value.flatMap { locator ->
            locator.availableValues(player).map { locator to it }
        }
        if (value.isEmpty()) {
            val meta = item.itemMeta as CompassMeta
            if (meta.lodestone != null) {
                val target = meta.lodestone!!
                player.placeholderComponent("info-compass-locate",
                    "x" replace target.blockX,
                    "y" replace target.blockY,
                    "z" replace target.blockZ,
                    "world" replace target.world.name
                ).sendMessage(player)
            }
            return
        }
        var located = false
        for ((locator, valueName) in candidates.shuffled()) {
            val target = locator.locate(player, valueName) ?: continue
            located = true
            val meta = item.itemMeta as CompassMeta
            meta.isLodestoneTracked = false
            meta.lodestone = target
            item.itemMeta = meta
            player.placeholderComponent("success-compass-locate",
                "x" replace target.blockX,
                "y" replace target.blockY,
                "z" replace target.blockZ,
                "world" replace target.world.name
            ).sendMessage(player)
            break
        }
        if (!located) {
            customItem.cooldowns.forEach { it.reset(player, item) }
            player.placeholderComponent("error-compass-locate").sendMessage(player)
        }
    }

    object Factory : AttributeFactory<CompassLocatorAttribute> {
        override fun create(key: String, config: ConfigurationSection): CompassLocatorAttribute {
            val section = config.deep(key)
            val radius = if (section.contains("radius")) section.getInt("radius") else DEFAULT_RADIUS
            val locators = buildSet {
                section.getStringList("biomes").toPatterns()?.let { add(BiomeCompassLocator("biomes", it, radius)) }
                section.getStringList("structures").toPatterns()?.let { add(StructureCompassLocator("structures", it, radius)) }
                section.getStringList("regions").toPatterns()?.let { add(RegionCompassLocator("regions", it)) }
            }
            return CompassLocatorAttribute(key, locators)
        }

        private fun List<String>.toPatterns(): List<Regex>? =
            if (isEmpty()) null else map { it.toRegex() }
    }

    companion object {
        /** Default search radius in blocks (structures search `radius / 16` chunks). */
        const val DEFAULT_RADIUS = 6400
    }
}

interface CompassLocator {
    fun availableValues(player: Player): List<String>
    fun locate(player: Player, value: String): Location?
}

abstract class RegexCompassLocator<T>(
    key: String,
    patterns: List<Regex>
) : AttributeBase<List<Regex>>(key, patterns), CompassLocator {

    protected abstract fun candidates(player: Player): Collection<T>
    protected abstract fun nameOf(candidate: T): String
    protected abstract fun resolve(player: Player, candidate: T): Location?

    override fun availableValues(player: Player): List<String> =
        candidates(player).map(::nameOf).filter { name -> value.any { it.matches(name) } }

    override fun locate(player: Player, value: String): Location? =
        candidates(player).firstOrNull { nameOf(it) == value }?.let { resolve(player, it) }
}

class BiomeCompassLocator(
    key: String,
    patterns: List<Regex>,
    private val radius: Int
) : RegexCompassLocator<Biome>(key, patterns) {
    override fun candidates(player: Player): Collection<Biome> = Biome.entries
    override fun nameOf(candidate: Biome): String = candidate.key.toString()
    override fun resolve(player: Player, candidate: Biome): Location? =
        player.world.locateNearestBiome(player.location, candidate, radius, 32)
}

class StructureCompassLocator(
    key: String,
    patterns: List<Regex>,
    radius: Int
) : RegexCompassLocator<StructureType>(key, patterns) {
    private val chunkRadius = (radius / 16).coerceAtLeast(1)
    override fun candidates(player: Player): Collection<StructureType> = StructureType.getStructureTypes().values
    override fun nameOf(candidate: StructureType): String = candidate.name
    override fun resolve(player: Player, candidate: StructureType): Location? =
        player.world.locateNearestStructure(player.location, candidate, chunkRadius, true)
}

class RegionCompassLocator(
    key: String,
    patterns: List<Regex>
) : RegexCompassLocator<ProtectedRegion>(key, patterns) {
    override fun candidates(player: Player): Collection<ProtectedRegion> =
        WorldGuard.getInstance().platform.regionContainer.get(BukkitAdapter.adapt(player.world))
            ?.regions?.values ?: emptyList()

    override fun nameOf(candidate: ProtectedRegion): String = candidate.id

    override fun resolve(player: Player, candidate: ProtectedRegion): Location {
        val min = candidate.minimumPoint
        val max = candidate.maximumPoint
        return Location(
            player.world,
            (min.x + max.x) / 2.0,
            (min.y + max.y) / 2.0,
            (min.z + max.z) / 2.0
        )
    }
}
