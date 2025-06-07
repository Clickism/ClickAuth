/*
 * Copyright 2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import de.clickism.clickauth.ClickAuth;
import de.clickism.clickauth.message.MessageType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import static de.clickism.clickauth.message.Messages.*;

public class UpdateWarningListener implements RegistrableListener {

    private final Supplier<String> newerVersionSupplier;
    private final Set<UUID> notifiedPlayers = new HashSet<>();

    public UpdateWarningListener(Supplier<String> newerVersionSupplier) {
        this.newerVersionSupplier = newerVersionSupplier;
    }

    @EventHandler(ignoreCancelled = true)
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.isOp()) return;
        String newerVersion = newerVersionSupplier.get();
        if (newerVersion == null) return;
        if (notifiedPlayers.contains(player.getUniqueId())) return;
        notifiedPlayers.add(player.getUniqueId());
        Bukkit.getScheduler().runTaskLater(ClickAuth.INSTANCE,
                () -> notifyPlayer(player, newerVersion), 10L);
    }

    private void notifyPlayer(Player player, String newerVersion) {
        MessageType.WARN.send(player, localize(UPDATE, newerVersion));
    }
}
