/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.listener;

import me.clickism.clickauth.authentication.AuthManager;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class GriefListener implements RegistrableListener {
    // TODO: Implement grief & movement prevention when not authenticated
    private final AuthManager authManager;

    public GriefListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler(ignoreCancelled = true)
    private void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (authManager.isAuthenticated(player)) return;
        Location to = event.getTo();
        if (to == null) return;
        Location from = event.getFrom();
        if (to.getY() < from.getY()) {
            // Allow falling
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    private void onInteract(PlayerInteractEvent event) {
        cancelIfNotAuthenticated(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onDropItem(PlayerDropItemEvent event) {
        cancelIfNotAuthenticated(event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        cancelIfNotAuthenticated(player, event);
    }

    @EventHandler
    private void onDamage(EntityDamageEvent event) {
        // TODO: Config setting to enable damage?
        if (!(event.getEntity() instanceof Player player)) return;
        cancelIfNotAuthenticated(player, event);
    }

    @EventHandler(ignoreCancelled = true)
    private void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof HumanEntity player) {
            cancelIfNotAuthenticated(player, event);
        } else if (event.getEntity() instanceof HumanEntity player) {
            cancelIfNotAuthenticated(player, event);
        }
    }

    private <T extends PlayerEvent & Cancellable> void cancelIfNotAuthenticated(T event) {
        cancelIfNotAuthenticated(event.getPlayer(), event);
    }

    private void cancelIfNotAuthenticated(HumanEntity player, Cancellable event) {
        if (authManager.isAuthenticated(player)) return;
        event.setCancelled(true);
    }
}
