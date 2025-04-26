package org.bigcraft.customitems.attribute

import com.destroystokyo.paper.profile.ProfileProperty
import me.wyne.wutils.i18n.I18n
import org.bigcraft.customitems.extension.getIntNullable
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.Repairable
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import kotlin.math.min

val MetaAttributes = AttributeMap(
    mutableMapOf(
        "name" to AttributeFactory { key, config -> DisplayNameAttribute(config.getString(key)!!) },
        "lore" to AttributeFactory { key, config -> LoreAttribute(config.getStringList(key)) },
        "flags" to AttributeFactory { key, config -> FlagsAttribute(config.getStringList(key).mapNotNull { ItemFlag.valueOf(it) }.toTypedArray()) },
        "skull" to AttributeFactory { key, config -> SkullAttribute(Bukkit.getPlayerUniqueId(config.getString(key)!!)?.let { Bukkit.getOfflinePlayer(it) }) },
        "skull64" to AttributeFactory { key, config -> Skull64Attribute(config.getString(key)!!) },
        "skullPlayer" to AttributeFactory { _, _ -> PlayerSkullAttribute },
        "unbreakable" to AttributeFactory { key, config -> UnbreakableAttribute(config.getBoolean(key, false)) },
        "glow" to AttributeFactory { key, config -> GlowAttribute(config.getBoolean(key, false)) },
        "durability" to AttributeFactory { key, config -> DurabilityAttribute(config.getInt(key, 1)) },
        "damage" to AttributeFactory { key, config -> DamageAttribute(config.getInt(key, 0)) },
        "model" to AttributeFactory { key, config -> CustomModelDataAttribute(config.getIntNullable(key, null)) },
        "repairCost" to AttributeFactory { key, config -> RepairableAttribute(config.getInt(key, 1)) }
    )
)

abstract class MetaAttribute<V>(key: String, value: V) : AttributeBase<V>(key, value) {
    override fun apply(item: ItemStack): ItemStack = item.also { it.editMeta(::apply) }
    abstract fun apply(meta: ItemMeta)
}

@Suppress("DEPRECATION")
class DisplayNameAttribute(
    name: String
) : MetaAttribute<String>("name", name), PlayerAwareAttribute<String> {

    override fun apply(meta: ItemMeta) =
        meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(null, value).bungee())

    override fun apply(item: ItemStack, player: Player): ItemStack =
        item.also { it.editMeta { meta -> meta.setDisplayNameComponent(I18n.global.getPlaceholderComponent(player.locale(), player, value).bungee()) } }

}

@Suppress("DEPRECATION")
class LoreAttribute(
    lore: List<String>
) : MetaAttribute<List<String>>("lore", lore), PlayerAwareAttribute<List<String>> {

    override fun apply(meta: ItemMeta) =
        meta.setLoreComponents(value.map { I18n.global.getPlaceholderComponent(null, it).bungee() })

    override fun apply(item: ItemStack, player: Player): ItemStack =
        item.also { it.editMeta { meta -> meta.setLoreComponents(value.map { I18n.global.getPlaceholderComponent(null, it).bungee() } ) } }

}

class FlagsAttribute(
    flags: Array<ItemFlag>
) : MetaAttribute<Array<ItemFlag>>("flags", flags) {

    override fun apply(meta: ItemMeta) =
        meta.addItemFlags(*value)

}

class SkullAttribute(
    owner: OfflinePlayer?
) : MetaAttribute<OfflinePlayer?>("skull", owner) {

    override fun apply(meta: ItemMeta) {
        if (meta !is SkullMeta) return
        meta.owningPlayer = value
    }

}

class Skull64Attribute(
    skull64: String
) : MetaAttribute<String>("skull64", skull64) {

    override fun apply(meta: ItemMeta) {
        if (meta !is SkullMeta) return
        val profile = Bukkit.createProfile(UUID.randomUUID())
        profile.setProperty(ProfileProperty("textures", value))
        meta.playerProfile = profile
    }

}

object PlayerSkullAttribute : Attribute<Unit> by EmptyAttribute("skullPlayer"), PlayerAwareAttribute<Unit> {

    override fun apply(item: ItemStack, player: Player): ItemStack {
        if (item.itemMeta !is SkullMeta) return item
        item.editMeta { meta -> (meta as SkullMeta).owningPlayer = player }
        return item
    }

}

class UnbreakableAttribute(
    unbreakable: Boolean
) : MetaAttribute<Boolean>("unbreakable", unbreakable) {

    override fun apply(meta: ItemMeta) {
        meta.isUnbreakable = value
    }

}

class GlowAttribute(
    glow: Boolean
) : MetaAttribute<Boolean>("glow", glow) {

    override fun apply(meta: ItemMeta) {
        if (meta.hasEnchants()) return
        if (!value) return
        meta.addEnchant(Enchantment.LURE, 1, true)
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

}

class DurabilityAttribute(
    durability: Int
) : MetaAttribute<Int>("durability", durability) {

    private var item: ItemStack? = null

    override fun apply(item: ItemStack): ItemStack {
        this.item = item
        return super.apply(item)
    }

    override fun apply(meta: ItemMeta) {
        if (meta !is Damageable) return
        val maxDurability = item?.type?.maxDurability?.toInt() ?: 0
        meta.damage = maxDurability - min(maxDurability, value)
    }

}

class DamageAttribute(
    damage: Int
) : MetaAttribute<Int>("damage", damage) {

    override fun apply(meta: ItemMeta) {
        if (meta !is Damageable) return
        meta.damage = value
    }

}

class CustomModelDataAttribute(
    model: Int?
) : MetaAttribute<Int?>("model", model) {

    override fun apply(meta: ItemMeta) =
        meta.setCustomModelData(value)

}

class RepairableAttribute(
    cost: Int
) : MetaAttribute<Int>("repairCost", cost) {

    override fun apply(meta: ItemMeta) {
        if (meta !is Repairable) return
        meta.repairCost = value
    }

}
