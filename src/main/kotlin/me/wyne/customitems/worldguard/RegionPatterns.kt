package me.wyne.customitems.worldguard

import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldguard.WorldGuard
import com.sk89q.worldguard.protection.ApplicableRegionSet
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Location

/**
 * Region names to match against, as regular expressions.
 *
 * A plain name still works — it simply matches itself — but a name containing regex metacharacters
 * no longer matches literally. That is the one config break in this area for 3.0.0.
 */
@JvmInline
value class RegionPatterns(private val patterns: List<Regex>) {

    fun matches(regionId: String) = patterns.any { it.matches(regionId) }

    fun matchesAny(regions: ApplicableRegionSet?) = regions?.any { matches(it.id) } ?: false

    fun matchesTop(regions: ApplicableRegionSet?) = regions?.top()?.let { matches(it.id) } ?: false

    companion object {
        fun of(names: Collection<String>) = RegionPatterns(names.map { it.toRegex() })
    }
}

/** The regions covering [location], or `null` when WorldGuard has nothing for that world. */
fun regionsAt(location: Location): ApplicableRegionSet? =
    WorldGuard.getInstance().platform.regionContainer.createQuery()
        .getApplicableRegions(BukkitAdapter.adapt(location))

/** The highest-priority region of the set, which is the one whose rules win. */
fun ApplicableRegionSet.top(): ProtectedRegion? = maxByOrNull { it.priority }
