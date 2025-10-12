package org.bigcraft.customitems.trap

import com.sk89q.worldedit.extent.clipboard.Clipboard
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.HandlerList
import org.bukkit.event.player.PlayerEvent

class CreateTrapEvent(
    player: Player,
    val scheme: Scheme,
    val clipboard: Clipboard,
    val snapshot: Clipboard,
    val location: Location,
    val durationTicks: Long
) : PlayerEvent(player), Cancellable {

    private var cancelled = false

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    override fun isCancelled(): Boolean =
        cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

    companion object {
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }

}