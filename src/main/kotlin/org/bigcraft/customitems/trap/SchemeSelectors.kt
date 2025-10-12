package org.bigcraft.customitems.trap

import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeMap
import org.bigcraft.customitems.config.getEnumSet
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player

val SCHEME_SELECTOR_MAP = AttributeMap(
    mapOf(
        "biomes" to AttributeFactory { key, config -> BiomeSchemeSelector(key, config.getEnumSet<Biome>(key)) },
        "worlds" to AttributeFactory { key, config -> WorldSchemeSelector(key, config.getStringList(key).mapNotNull { Bukkit.getWorld(it) }.toSet()) },
        "blocks" to AttributeFactory { key, config -> BlockSchemeSelector(key, config.getEnumSet<Material>(key)) }
    )
)

interface SchemeSelector {
    fun isApplicable(player: Player): Boolean
}

class BiomeSchemeSelector(key: String, biomes: Set<Biome>) : AttributeBase<Set<Biome>>(key, biomes), SchemeSelector {
    override fun isApplicable(player: Player) = player.location.block.biome in value
}

class WorldSchemeSelector(key: String, worlds: Set<World>) : AttributeBase<Set<World>>(key, worlds), SchemeSelector {
    override fun isApplicable(player: Player) = player.location.world in value
}

class BlockSchemeSelector(key: String, blocks: Set<Material>) : AttributeBase<Set<Material>>(key, blocks), SchemeSelector {
    override fun isApplicable(player: Player) = player.location.block.getRelative(BlockFace.DOWN).type in value
}