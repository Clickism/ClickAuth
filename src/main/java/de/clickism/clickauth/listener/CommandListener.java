/*
 * Copyright 2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import de.clickism.clickauth.authentication.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import static de.clickism.clickauth.message.Messages.*;

public class CommandListener implements RegistrableListener {
    private final AuthManager authManager;

    public CommandListener(AuthManager authManager) {
        this.authManager = authManager;
    }

    @EventHandler
    private void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (!authManager.isAuthenticated(player)) {
            event.setCancelled(true);
            player.sendMessage(localize(MUST_BE_LOGGED_IN));
        }
    }
}
