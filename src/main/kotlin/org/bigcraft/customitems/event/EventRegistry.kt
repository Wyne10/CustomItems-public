package org.bigcraft.customitems.event

import org.bigcraft.customitems.CustomItems
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

interface EventListener<E : Event> {
    val event: Class<out E>
    fun onEvent(event: E)
}

class EventRegistry : Listener, AutoCloseable {

    private val registry: MutableMap<Class<out Event>, MutableSet<EventListener<*>>> = HashMap()

    fun <E : Event> register(listener: EventListener<E>) {
        if (!registry.containsKey(listener.event)) {
            registry[listener.event] = HashSet()
            registerEvent(listener.event)
        }
        registry[listener.event]!!.add(listener)
    }

    override fun close() {
        registry.values.forEach { it.clear() }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <E : Event> registerEvent(event: Class<out E>) {
        Bukkit.getPluginManager().registerEvent(
            event,
            this,
            EventPriority.NORMAL,
            { _, calledEvent ->
                if (calledEvent.javaClass == event)
                    registry[event]!!.forEach { (it as EventListener<E>).onEvent(calledEvent as E) }
            },
            CustomItems.instance,
            true
        )
    }

}
