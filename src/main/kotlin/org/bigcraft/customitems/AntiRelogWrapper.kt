package org.bigcraft.talisman

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import ru.leymooo.antirelog.Antirelog

object AntiRelogWrapper {
    val IS_ANTIRELOG_PRESENT: Boolean
    val Antirelog: Any?

    init {
        var isAntiRelogPresent = false
        try {
            Class.forName("ru.leymooo.antirelog.Antirelog")
            isAntiRelogPresent = true
        } catch (e: ClassNotFoundException) {
            isAntiRelogPresent = false
        }
        IS_ANTIRELOG_PRESENT = isAntiRelogPresent
        Antirelog = if (IS_ANTIRELOG_PRESENT)
            Bukkit.getPluginManager().getPlugin("AntiRelog") as? Antirelog
        else
            null
    }

    fun isInPvp(player: Player): Boolean {
        if (!IS_ANTIRELOG_PRESENT) return false
        return (Antirelog as? Antirelog)?.pvpManager?.isInPvP(player) == true ||
                (Antirelog as? Antirelog)?.pvpManager?.isInSilentPvP(player) == true
    }
}
