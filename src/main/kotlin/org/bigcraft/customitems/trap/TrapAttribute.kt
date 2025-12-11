package org.bigcraft.customitems.trap

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.Region
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.world.World
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.kotlin.config.getMillis
import me.wyne.wutils.common.range.LocationRange
import me.wyne.wutils.common.scheduler.EventPromisedTask
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeUtils
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.aoe.AoeManager
import org.bigcraft.customitems.aoe.AreaOfEffect
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class TrapAttribute(
    key: String,
    schemes: Set<Scheme>,
    private val durationMillis: Long
) : ActivatorAttribute<Set<Scheme>>(key, schemes) {

    private val durationTicks = Ticks.ofMillis(durationMillis)

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val scheme = value.first { it.isApplicable(player) }
        val clipboard = scheme.load()
        val location = player.location
        val editLocation = BukkitAdapter.adapt(location)
        val editWorld = BukkitAdapter.adapt(location.world)
        val snapshot = snapshotRegion(clipboard, editLocation)
        if (!CreateTrapEvent(player, scheme, clipboard, snapshot, location, durationTicks).callEvent())
            return
        createTrapAoe(scheme, player, snapshot.region, editWorld)
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            pasteClipboard(clipboard, editLocation.toVector().toBlockPoint(), editWorld)
        }, 1)
        EventPromisedTask(
            CustomItems.instance,
            {
                pasteClipboard(snapshot, snapshot.origin, editWorld)
            },
            { event ->
                if (event == null || event.plugin == CustomItems.instance)
                    pasteClipboard(snapshot, snapshot.origin, editWorld)
            },
            PluginDisableEvent::class.java
        ).runTaskLater(durationTicks)
    }

    private fun snapshotRegion(clipboard: Clipboard, location: Location): Clipboard {
        val region = clipboard.region
        region.shift(location.toVector().toBlockPoint().subtract(clipboard.origin))
        val snapshot = BlockArrayClipboard(region)
        val forwardExtentCopy = ForwardExtentCopy(location.extent, region, snapshot, region.minimumPoint)
        forwardExtentCopy.isCopyingEntities = false
        forwardExtentCopy.isCopyingBiomes = false
        Operations.complete(forwardExtentCopy)
        return snapshot
    }

    private fun pasteClipboard(clipboard: Clipboard, point: BlockVector3, world: World) {
        WorldEdit.getInstance().newEditSession(world)
            .use { session ->
                val operation = ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(point)
                    .copyEntities(false)
                    .copyBiomes(false)
                    .ignoreAirBlocks(false)
                    .build()
                Operations.complete(operation)
            }
    }

    private fun createTrapAoe(scheme: Scheme, player: Player, region: Region, world: World) {
        val min = Vector(region.minimumPoint.x, region.minimumPoint.y, region.minimumPoint.z)
        val max = Vector(region.maximumPoint.x + 1, region.maximumPoint.y + 1, region.maximumPoint.z + 1)
        val bounds = LocationRange(BukkitAdapter.adapt(world), min, max)
        AoeManager.instance.startAoe(
            AreaOfEffect(
                scheme.key,
                player,
                durationMillis,
                bounds,
                null
            )
        )
        AoeManager.instance.startAoe(
            AreaOfEffect(
                "${scheme.key}-pre",
                player,
                durationMillis - 1500,
                bounds,
                null
            )
        )
    }

    object Factory : AttributeFactory<TrapAttribute> {
        override fun create(key: String, config: ConfigurationSection): TrapAttribute {
            val section = config.getConfigurationSection(key)!!
            val schemes = AttributeUtils.createAll<Scheme>(section.getConfigurationSection("schemes"), Scheme.Factory)
            return TrapAttribute(
                key,
                schemes,
                section.getMillis("duration"),
            )
        }
    }

}