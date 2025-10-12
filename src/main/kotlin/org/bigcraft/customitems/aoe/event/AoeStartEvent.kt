package org.bigcraft.customitems.aoe.event

import org.bigcraft.customitems.aoe.AreaOfEffect
import org.bukkit.event.Cancellable

class AoeStartEvent(areaOfEffect: AreaOfEffect) : AoeEvent(areaOfEffect), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean =
        cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}