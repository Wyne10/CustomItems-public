package org.bigcraft.customitems.context

import me.wyne.wutils.config.configurables.item.ItemAttributeContext
import me.wyne.wutils.i18n.kotlin.replace
import me.wyne.wutils.i18n.kotlin.replaceComponent
import me.wyne.wutils.i18n.language.replacement.ComponentReplacement
import me.wyne.wutils.i18n.language.replacement.TextReplacement
import net.kyori.adventure.text.Component
import me.wyne.wutils.i18n.kotlin.bungee
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bigcraft.customitems.api.CustomItemApi
import org.bigcraft.customitems.api.spi.ItemPlaceholderSink
import org.bigcraft.customitems.spi.Registries
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

/**
 * Everything a stack's name and lore can be written against, gathered once per render.
 *
 * [attributes] is what WUtils substitutes while building the name and lore; [lineExpansions] is
 * applied afterwards, because a run of lines cannot be expressed as a replacement within one line.
 */
class ItemContext(
    val attributes: ItemAttributeContext,
    val lineExpansions: Map<String, List<Component>>,
) {
    companion object {
        val EMPTY = ItemContext(ItemAttributeContext.EMPTY, emptyMap())

        /** Runs every registered provider against [stack]. */
        fun of(item: CustomItemApi, stack: ItemStack, player: Player?): ItemContext {
            val sink = CollectingSink()
            Registries.itemPlaceholderProviders.forEach { it.contribute(sink, item, stack, player) }
            return ItemContext(
                ItemAttributeContext(player, sink.text.toTypedArray(), sink.components.toTypedArray()),
                sink.lines
            )
        }
    }
}

private class CollectingSink : ItemPlaceholderSink {

    val text = mutableListOf<TextReplacement>()
    val components = mutableListOf<ComponentReplacement>()
    val lines = mutableMapOf<String, List<Component>>()

    override fun text(key: String, value: String) {
        text += key replace value
    }

    override fun component(key: String, miniMessage: String) {
        components += key replaceComponent MiniMessage.miniMessage().deserialize(miniMessage)
    }

    override fun lines(key: String, miniMessage: List<String>) {
        lines[key] = miniMessage.map { MiniMessage.miniMessage().deserialize(it) }
        // The marker itself is dropped from the line it sits on; the expansion follows that line.
        text += key replace ""
    }
}

/**
 * Inserts each expansion's lines after the lore line its placeholder was written on, or drops that
 * line entirely when the expansion is empty.
 *
 * Positions are found in the lore [template] rather than in the rendered lore, because the
 * placeholder has already been substituted away by the time this runs. Expansions are applied from
 * the bottom up so that earlier indices stay valid.
 */
fun expandLoreLines(stack: ItemStack, template: List<String>, expansions: Map<String, List<Component>>) {
    val targets = expansions.entries
        .mapNotNull { (key, lines) ->
            template.indexOfFirst { "<$key>" in it }.takeIf { it >= 0 }?.let { it to lines }
        }
        .sortedByDescending { it.first }
    if (targets.isEmpty()) return

    stack.editMeta { meta ->
        val lore = meta.loreComponents?.toMutableList() ?: return@editMeta
        for ((index, lines) in targets) {
            if (index >= lore.size) continue
            if (lines.isEmpty()) lore.removeAt(index) else lore.addAll(index + 1, lines.map { it.bungee })
        }
        meta.loreComponents = lore
    }
}
