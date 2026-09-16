package org.bigcraft.customitems.composition

import org.bigcraft.customitems.item.CustomItem
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * The custom items composed into one stack, stored as a comma-joined list in the stack's persistent
 * data. Exists so the delimiter is parsed in one place rather than by substring-matching at every
 * call site.
 */
@JvmInline
value class ComposedKeys private constructor(val keys: List<String>) {

    operator fun contains(key: String) = key in keys

    val isEmpty: Boolean
        get() = keys.isEmpty()

    operator fun plus(key: String) = ComposedKeys(keys + key)

    override fun toString() = keys.joinToString(DELIMITER)

    companion object {
        private const val DELIMITER = ","

        val EMPTY = ComposedKeys(emptyList())

        fun parse(raw: String?): ComposedKeys =
            if (raw.isNullOrEmpty()) EMPTY else ComposedKeys(raw.split(DELIMITER))

        fun of(stack: ItemStack): ComposedKeys {
            if (!stack.hasItemMeta()) return EMPTY
            return parse(
                stack.itemMeta.persistentDataContainer.get(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING)
            )
        }

        fun write(stack: ItemStack, keys: ComposedKeys) {
            stack.editMeta { meta ->
                meta.persistentDataContainer.set(CustomItem.COMPOSE_TAG_KEY, PersistentDataType.STRING, keys.toString())
            }
        }
    }
}
