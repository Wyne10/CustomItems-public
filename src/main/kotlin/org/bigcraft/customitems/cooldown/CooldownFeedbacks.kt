package org.bigcraft.customitems.cooldown

import me.wyne.wutils.common.Ticks
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.common.cooldown.Period
import org.bigcraft.customitems.api.spi.Cooldown
import org.bigcraft.customitems.api.spi.CooldownContext
import org.bigcraft.customitems.api.spi.CooldownFeedback
import org.bigcraft.customitems.api.spi.FactoryRegistry

internal fun registerCooldownFeedback(registry: FactoryRegistry<CooldownFeedback>) {
    registry.register("message") { key, config -> MessageFeedback(config.getString(key)!!) }
    registry.register("visual") { key, config -> VisualFeedback(config.getBoolean(key)) }
}

/** Tells the player how long is left, using the `<remaining>` placeholder. */
class MessageFeedback(private val message: String) : CooldownFeedback {
    override fun onBlocked(context: CooldownContext, cooldown: Cooldown) {
        val remaining = Period(System.currentTimeMillis() + cooldown.getRemainingMillis(context))
            .getRemainingStringFormat("s")
        context.player.placeholderComponent(message, "remaining" replace remaining).sendMessage(context.player)
    }
}

/** Greys the item out in the hotbar for as long as the cooldown runs. */
class VisualFeedback(private val enabled: Boolean) : CooldownFeedback {
    override fun onStarted(context: CooldownContext, cooldown: Cooldown) {
        if (!enabled) return
        context.player.setCooldown(context.item.material, Ticks.ofMillis(cooldown.getRemainingMillis(context)).toInt())
    }
}
