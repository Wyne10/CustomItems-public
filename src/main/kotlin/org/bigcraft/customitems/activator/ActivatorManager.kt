package org.bigcraft.customitems.activator

import com.google.inject.Singleton
import org.bigcraft.customitems.event.EventRegistry

@Singleton
class ActivatorManager {

    val activatorRegistry = EventRegistry()

    init {
        instance = this
    }

    companion object {
        lateinit var instance: ActivatorManager
            private set
    }

}