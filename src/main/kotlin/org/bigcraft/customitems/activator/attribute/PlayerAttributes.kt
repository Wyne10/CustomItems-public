package org.bigcraft.customitems.activator.attribute

import me.wyne.wutils.animation.runnable.LocalSound
import me.wyne.wutils.common.kotlin.player.currentExp
import me.wyne.wutils.common.operation.IntOperation
import me.wyne.wutils.config.configurables.InteractionListConfigurable
import me.wyne.wutils.i18n.kotlin.placeholderComponent
import me.wyne.wutils.i18n.kotlin.replace
import net.kyori.adventure.sound.Sound
import org.bigcraft.customitems.activator.ActivatorAttribute
import org.bigcraft.customitems.i18n
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack

class LevelStealAttribute(
    key: String,
    steal: Double
) : ActivatorAttribute<Double>(key, steal) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack) =
        event is PlayerDeathEvent

    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        event as PlayerDeathEvent
        val killedPlayer = event.entity
        val toSteal = killedPlayer.level * value
        player.level += toSteal.toInt()
    }
}

class ActionBarAttribute(
    key: String,
    message: String
) : ActivatorAttribute<String>(key, message) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.placeholderComponent(value, "key" replace customItem.key).sendActionBar(player)
    }
}

class PlayerMessageAttribute(
    key: String,
    message: String
) : ActivatorAttribute<String>(key, message) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        player.placeholderComponent(value, "key" replace customItem.key).sendMessage(player)
    }
}

class PlayerSoundAttribute(
    key: String,
    sound: Sound
) : ActivatorAttribute<Sound>(key, sound) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        i18n.audiences.player(player).playSound(value)
    }
}

class LocalSoundAttribute(
    key: String,
    sound: Sound
) : ActivatorAttribute<Sound>(key, sound) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        LocalSound(player.location, value).run()
    }
}

class InteractionAttribute(
    key: String,
    interaction: InteractionListConfigurable
) : ActivatorAttribute<InteractionListConfigurable>(key, interaction) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        value.send(player, player, "key" replace customItem.key)
    }
}

class PlayerExpAttribute(
    key: String,
    operation: IntOperation
) : ActivatorAttribute<IntOperation>(key, operation) {
    override fun apply(event: Event, player: Player, customItem: CustomItem, item: ItemStack) {
        val currentExp = player.currentExp
        player.exp = 0f
        player.level = 0
        player.giveExp(value.evaluate(currentExp))
    }
}