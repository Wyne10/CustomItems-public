package org.bigcraft.customitems.activator

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent
import org.bigcraft.customitems.api.spi.ActivatorType
import org.bigcraft.customitems.api.spi.InstanceRegistry
import org.bigcraft.customitems.aoe.event.AoeFinishEvent
import org.bigcraft.customitems.trap.CreateTrapEvent
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.EquipmentSlot

internal fun registerActivatorTypes(registry: InstanceRegistry<ActivatorType>) {
    registry.register(RightClickActivator)
    registry.register(PlayerKillActivator)
    registry.register(PlayerHitActivator)
    registry.register(EntityHitPlayerActivator)
    registry.register(ProjectileHitPlayerActivator)
    registry.register(PlayerHitEntityActivator)
    registry.register(LaunchProjectileActivator)
    registry.register(PlayerDamageActivator)
    registry.register(PlayerPotionEffectActivator)
    registry.register(PlayerConsumeItemActivator)
    registry.register(BlockBreakActivator)
    registry.register(BlockPlaceActivator)
    registry.register(PlayerResurrectActivator)
    registry.register(PlayerMoveActivator)
    registry.register(PlayerCommandActivator)
    registry.register(CreateTrapActivator)
    registry.register(AoeFinishActivator)
    registry.register(PlayerDropItemActivator)
    registry.register(PlayerDeathActivator)
}

object RightClickActivator : ActivatorType("right-click") {
    @EventHandler(priority = EventPriority.HIGH)
    fun onRightClick(event: PlayerInteractEvent) {
        if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) return
        if (event.hand != EquipmentSlot.HAND) return
        dispatch(event, event.player)
    }
}

object PlayerKillActivator : ActivatorType("player-kill-player") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDeath(event: PlayerDeathEvent) {
        dispatch(event, event.entity.killer ?: return)
    }
}

object PlayerHitActivator : ActivatorType("player-hit-player") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity !is Player) return
        dispatch(event, damager)
    }
}

object EntityHitPlayerActivator : ActivatorType("entity-hit-player") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        if (event.damager is Player) return
        dispatch(event, damaged)
    }
}

object ProjectileHitPlayerActivator : ActivatorType("projectile-hit-player") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damaged = event.entity as? Player ?: return
        if (event.damager !is Projectile) return
        dispatch(event, damaged)
    }
}

object PlayerHitEntityActivator : ActivatorType("player-hit-entity") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageByEntityEvent) {
        val damager = event.damager as? Player ?: return
        if (event.entity is Player) return
        dispatch(event, damager)
    }
}

object LaunchProjectileActivator : ActivatorType("player-launch-projectile") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onLaunch(event: PlayerLaunchProjectileEvent) {
        dispatch(event, event.player)
    }
}

object PlayerDamageActivator : ActivatorType("player-take-damage") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDamage(event: EntityDamageEvent) {
        dispatch(event, event.entity as? Player ?: return)
    }
}

object PlayerPotionEffectActivator : ActivatorType("player-potion-effect") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPotion(event: EntityPotionEffectEvent) {
        dispatch(event, event.entity as? Player ?: return)
    }
}

object PlayerConsumeItemActivator : ActivatorType("player-consume-item") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onConsume(event: PlayerItemConsumeEvent) {
        dispatch(event, event.player)
    }
}

object BlockBreakActivator : ActivatorType("player-block-break") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onBreak(event: BlockBreakEvent) {
        dispatch(event, event.player)
    }
}

object BlockPlaceActivator : ActivatorType("player-block-place") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onPlace(event: BlockPlaceEvent) {
        dispatch(event, event.player)
    }
}

object PlayerResurrectActivator : ActivatorType("player-resurrect") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onResurrect(event: EntityResurrectEvent) {
        dispatch(event, event.entity as? Player ?: return)
    }
}

object PlayerMoveActivator : ActivatorType("player-move") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        dispatch(event, event.player)
    }
}

object PlayerCommandActivator : ActivatorType("player-command") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        dispatch(event, event.player)
    }
}

object CreateTrapActivator : ActivatorType("player-create-trap") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onTrapCreation(event: CreateTrapEvent) {
        dispatch(event, event.player)
    }
}

object AoeFinishActivator : ActivatorType("aoe-finish") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onAoeFinish(event: AoeFinishEvent) {
        event.areaOfEffect.getWithin().forEach { dispatch(event, it) }
    }
}

object PlayerDropItemActivator : ActivatorType("player-drop-item") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onItemDrop(event: PlayerDropItemEvent) {
        dispatchForStack(event, event.player, event.itemDrop.itemStack)
    }
}

object PlayerDeathActivator : ActivatorType("player-death") {
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    fun onDeath(event: PlayerDeathEvent) {
        dispatch(event, event.entity)
    }
}
