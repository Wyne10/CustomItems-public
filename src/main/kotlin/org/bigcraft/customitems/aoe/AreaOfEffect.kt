package org.bigcraft.customitems.aoe

import me.wyne.wutils.animation.Animation
import me.wyne.wutils.animation.AnimationRunnable
import me.wyne.wutils.animation.BlockingAnimationStep
import me.wyne.wutils.animation.ParallelAnimationStep
import me.wyne.wutils.animation.runnable.ParticleArray
import me.wyne.wutils.common.Ticks
import me.wyne.wutils.common.range.LocationRange
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.aoe.event.AoeFinishEvent
import org.bigcraft.customitems.aoe.event.AoeStartEvent
import org.bukkit.Location
import org.bukkit.entity.Player

class AreaOfEffect(
    val key: String,
    val owner: Player,
    durationMillis: Long,
    val bounds: LocationRange,
    private val renderer: ParticleArray?
) {

    private val durationTicks = Ticks.ofMillis(durationMillis)

    fun start(manager: AoeManager) {
        val aoe = Animation(CustomItems.instance)
        if (renderer != null)
            aoe.addStep(ParallelAnimationStep(renderer, 0, 5, durationTicks))
        aoe.addStep(BlockingAnimationStep(AnimationRunnable.runnable {
            manager.removeAoe(this)
            AoeFinishEvent(this).callEvent()
        }, durationTicks))
        if (AoeStartEvent(this).callEvent())
            aoe.run()
    }

    fun getWithin(): Set<Player> =
        Location(bounds.world, bounds.center.x, bounds.center.y, bounds.center.z)
            .getNearbyPlayers(bounds.span.x, bounds.span.y, bounds.span.z)
            .toSet()

    fun getAffected(): Set<Player> =
        Location(bounds.world, bounds.center.x, bounds.center.y, bounds.center.z)
            .getNearbyPlayers(bounds.span.x, bounds.span.y, bounds.span.z)
            .filterNot { it == owner }
            .toSet()

}