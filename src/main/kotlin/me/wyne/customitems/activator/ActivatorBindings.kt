package me.wyne.customitems.activator

import me.wyne.customitems.CustomItems
import me.wyne.customitems.api.spi.ActivatorBinding
import me.wyne.customitems.item.CustomItem
import me.wyne.customitems.spi.Registries
import org.bukkit.configuration.ConfigurationSection

private const val CONDITION_CANCEL_EVENT = "condition-cancel-event"
private const val ATTRIBUTE_TYPE = "attributeType"
private const val IDENTIFIER = "identifier"

/**
 * Binds [item] to every activator type its `activators` section names. A section may name a type
 * under its own key, or under any name carrying `attributeType: <type>`, which is how one item
 * declares the same activator more than once.
 */
internal fun bindActivators(item: CustomItem, section: ConfigurationSection) {
    for (configKey in section.getKeys(false)) {
        val body = section.getConfigurationSection(configKey) ?: CustomItems.EMPTY_CONFIGURATION
        val typeKey = body.getString(ATTRIBUTE_TYPE) ?: configKey
        val type = Registries.activatorTypeInstances.get(typeKey)
        if (type == null) {
            CustomItems.logger.warn("Unknown activator '{}' on item '{}'", typeKey, item.key)
            continue
        }
        type.bind(createBinding(item, body))
    }
}

private fun createBinding(item: CustomItem, body: ConfigurationSection): ActivatorBinding {
    val attributes = Registries.activatorAttributeFactories.createAllMap(body)
    return ActivatorBinding(
        item,
        resolveIdentifier(item, body),
        attributes.filterKeys { it != CONDITION_CANCEL_EVENT }.values.toList(),
        Registries.activatorConditionFactories.createAll(body).toList(),
        listOfNotNull(attributes[CONDITION_CANCEL_EVENT]),
    )
}

private fun resolveIdentifier(item: CustomItem, body: ConfigurationSection) =
    body.getString(IDENTIFIER)?.let { name ->
        Registries.itemIdentifierInstances.get(name) ?: run {
            CustomItems.logger.warn("Unknown item identifier '{}' on item '{}'", name, item.key)
            null
        }
    } ?: PlayerItemIdentifier
