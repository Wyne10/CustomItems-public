package me.wyne.customitems.aoe

import com.google.inject.Singleton

@Singleton
class AoeManager {

    private val activeAoe = mutableSetOf<AreaOfEffect>()

    init {
        instance = this
    }

    fun startAoe(aoe: AreaOfEffect) {
        addAoe(aoe)
        aoe.start(this)
    }

    fun addAoe(aoe: AreaOfEffect) =
        activeAoe.add(aoe)

    fun removeAoe(aoe: AreaOfEffect) =
        activeAoe.remove(aoe)

    fun getAoe(key: String): Set<AreaOfEffect> =
        activeAoe.filter { it.key == key }.toSet()

    companion object {
        lateinit var instance: AoeManager
            private set
    }

}