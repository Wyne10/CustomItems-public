package org.bigcraft.customitems.trap

import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import me.wyne.wutils.common.kotlin.config.deep
import me.wyne.wutils.config.configurables.AttributeConfigurable
import me.wyne.wutils.config.configurables.attribute.AttributeBase
import me.wyne.wutils.config.configurables.attribute.AttributeFactory
import me.wyne.wutils.config.configurables.attribute.ImmutableAttributeContainer
import org.bigcraft.customitems.CustomItems
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.io.File
import java.io.FileInputStream

class Scheme(
    key: String,
    path: String,
    private val selectors: Set<SchemeSelector>
) : AttributeBase<String>(key, path), SchemeSelector {

    private val file = File(CustomItems.instance.dataFolder, path)

    fun load(): Clipboard {
        val clipboardFormat = ClipboardFormats.findByFile(file)
        return clipboardFormat!!.getReader(FileInputStream(file))
            .use { it.read() }
    }

    override fun isApplicable(player: Player): Boolean =
        selectors.all { it.isApplicable(player) }

    object Factory : AttributeFactory<Scheme> {
        override fun create(key: String, config: ConfigurationSection): Scheme {
            val section = config.getConfigurationSection(key)!!
            return Scheme(
                key,
                section.getString("path")!!,
                createSchemeSelectors(key, config)
            )
        }

        private fun createSchemeSelectors(key: String, config: ConfigurationSection) =
            AttributeConfigurable(ImmutableAttributeContainer(SCHEME_SELECTOR_MAP, config.deep(key)))
                .getSet(SchemeSelector::class.java)
    }

}


