package me.wyne.customitems.activator

import me.wyne.customitems.api.CustomItemApi
import me.wyne.customitems.api.spi.InstanceRegistry
import me.wyne.customitems.api.spi.ItemIdentifier
import me.wyne.storm.api.StormApi
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

internal fun registerItemIdentifiers(registry: InstanceRegistry<ItemIdentifier>) {
    registry.register(EmptyItemIdentifier)
    registry.register(PlayerItemIdentifier)
    registry.register(HoldItemIdentifier)
    registry.register(MainHandItemIdentifier)
    registry.register(OffHandItemIdentifier)
    registry.register(PlayerOrEffectItemIdentifier)
}

object EmptyItemIdentifier : ItemIdentifier {
    override fun getKey() = "EMPTY"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack = ItemStack(Material.AIR)
}

object PlayerItemIdentifier : ItemIdentifier {
    override fun getKey() = "PLAYER"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack? =
        item.getItem(player.inventory)
}

object HoldItemIdentifier : ItemIdentifier {
    override fun getKey() = "HOLD"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack? =
        player.inventory.getItem(EquipmentSlot.HAND).takeIf(item::isCustomItem)
            ?: player.inventory.getItem(EquipmentSlot.OFF_HAND).takeIf(item::isCustomItem)
}

object MainHandItemIdentifier : ItemIdentifier {
    override fun getKey() = "MAIN"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack? =
        player.inventory.getItem(EquipmentSlot.HAND).takeIf(item::isCustomItem)
}

object OffHandItemIdentifier : ItemIdentifier {
    override fun getKey() = "OFF"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack? =
        player.inventory.getItem(EquipmentSlot.OFF_HAND).takeIf(item::isCustomItem)
}

object PlayerOrEffectItemIdentifier : ItemIdentifier {
    override fun getKey() = "PLAYER_OR_EFFECT"
    override fun getItem(player: Player, item: CustomItemApi): ItemStack? {
        val effect = StormApi.getEffectRegistry().getEffectInstance(item.key)
            ?: return PlayerItemIdentifier.getItem(player, item)
        if (StormApi.getEffectManager().isAffected(player, effect.key))
            return EmptyItemIdentifier.getItem(player, item)
        return PlayerItemIdentifier.getItem(player, item)
    }
}
