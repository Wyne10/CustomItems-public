package me.wyne.customitems

object LootPoolSupport {
    // LootPool is optional, and CustomKeyedLoot implements its interface: touching that class without
    // LootPool installed throws NoClassDefFoundError, so every use goes through this check first.
    val IS_LOOTPOOL_PRESENT: Boolean = try {
        Class.forName("me.wyne.lootpool.api.LootPool")
        true
    } catch (_: ClassNotFoundException) {
        false
    }
}
