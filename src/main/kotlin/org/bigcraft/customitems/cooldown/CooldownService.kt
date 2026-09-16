package org.bigcraft.customitems.cooldown

/**
 * Cooldowns one item puts on another through `other-cooldown`.
 *
 * They are created once, at load, and looked up by the target item's key — the target's own
 * [CooldownGroup] never has to be mutated, and repeated activations cannot pile up duplicates.
 */
object CooldownService {

    private val imposed = LinkedHashMap<String, MutableList<ConfiguredCooldown>>()

    fun impose(targetItemKey: String, cooldown: ConfiguredCooldown) {
        imposed.getOrPut(targetItemKey) { mutableListOf() }.add(cooldown)
    }

    fun imposedOn(itemKey: String): List<ConfiguredCooldown> = imposed[itemKey].orEmpty()

    fun clear() = imposed.clear()

}
