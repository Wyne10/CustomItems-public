package org.bigcraft.customitems.trap

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.session.ClipboardHolder
import com.sk89q.worldedit.util.Location
import com.sk89q.worldedit.world.World
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.event.RegisterableListener
import me.wyne.wutils.config.configurables.attribute.Attribute
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.AttributeUtils
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.config.getMillis
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class TrapAttribute(
    key: String,
    schemes: Set<Scheme>,
    durationMillis: Long
) : ActivatorAttribute<Set<Scheme>>(key, schemes), RegisterableListener {

    private val durationTicks = Ticks.ofMillis(durationMillis)

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val scheme = value.first { it.isApplicable(player) }
        val clipboard = scheme.load()
        val location = player.location
        val editLocation = BukkitAdapter.adapt(location)
        val editWorld = BukkitAdapter.adapt(location.world)
        val snapshot = snapshotRegion(clipboard, editLocation)
        if (!CreateTrapEvent(player, scheme, clipboard, snapshot, durationTicks).callEvent())
            return
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            pasteClipboard(clipboard, editLocation.toVector().toBlockPoint(), editWorld)
        }, 1)
        Bukkit.getScheduler().runTaskLater(CustomItems.instance, Runnable {
            pasteClipboard(snapshot, snapshot.origin, editWorld)
        }, durationTicks)
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

    object Factory : AttributeFactory {
        override fun create(key: String, config: ConfigurationSection): Attribute<*> {
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