package org.bigcraft.customitems.aoe.event

import org.bigcraft.customitems.aoe.AreaOfEffect
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

abstract class AoeEvent(val areaOfEffect: AreaOfEffect) : Event() {

    override fun getHandlers(): HandlerList {
        return HANDLER_LIST
    }

    companion object {
        val HANDLER_LIST = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLER_LIST
        }
    }

}