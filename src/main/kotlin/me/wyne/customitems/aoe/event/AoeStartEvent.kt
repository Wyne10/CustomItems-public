package me.wyne.customitems.aoe.event

import me.wyne.customitems.aoe.AreaOfEffect
import org.bukkit.event.Cancellable

class AoeStartEvent(areaOfEffect: AreaOfEffect) : AoeEvent(areaOfEffect), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean =
        cancelled

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}