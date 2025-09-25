package org.bigcraft.customitems.placeholder

import com.google.inject.Inject
import com.google.inject.Singleton
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import me.wyne.wutils.common.Args
import org.bigcraft.customitems.CustomItems
import org.bigcraft.customitems.item.CustomItemManager
import org.bukkit.OfflinePlayer

@Singleton
class ItemPlaceholders @Inject constructor(
    private val plugin: CustomItems,
    private val itemManager: CustomItemManager
) : PlaceholderExpansion() {

    init {
        register()
    }

    override fun getIdentifier(): String = "ci"

    override fun getAuthor(): String = "Wyne"

    override fun getVersion(): String = plugin.description.version

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val args = Args(params, "_")

        if (args.size() < 2) {
            CustomItems.log.error("Not enough arguments for custom items placeholder. Required: 2")
            return null
        }

        val itemKey = args.get(0)
        val item = itemManager.getItem(itemKey) ?: return null

        return when (args.get(1)) {
            "name" -> item.getName(player).toString()
            "name-mm" -> item.getName(player).miniMessage()
            "name-legacy" -> item.getName(player).legacy()
            "name-plain" -> item.getName(player).plainText()
            else -> null
        }
    }

}