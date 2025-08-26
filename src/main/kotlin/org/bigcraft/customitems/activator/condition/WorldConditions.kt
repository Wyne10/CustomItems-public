package org.bigcraft.customitems.activator.condition

import org.bigcraft.customitems.config.IntComparator
import org.bigcraft.customitems.item.CustomItem
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class WeatherCondition(key: String, weatherTypes: Set<Weather>) : ActivatorCondition<Set<Weather>>(key, weatherTypes) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        player.location.world.getWeather() in value
}

class DayTimeCondition(key: String, comparator: IntComparator) : ActivatorCondition<IntComparator>(key, comparator) {
    override fun isApplicable(event: Event, player: Player, customItem: CustomItem, item: ItemStack): Boolean =
        value.compare(player.location.world.time.toInt())
}

fun World.getWeather(): Weather {
    if (hasStorm())
        return Weather.STORM
    else if (!isClearWeather)
        return Weather.RAIN
    return Weather.CLEAR
}

enum class Weather {
    CLEAR,
    RAIN,
    STORM
}
