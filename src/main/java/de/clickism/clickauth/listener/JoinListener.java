/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import de.clickism.clickauth.authentication.LoginHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements RegistrableListener {

    private final LoginHandler loginHandler;

    public JoinListener(LoginHandler loginHandler) {
        this.loginHandler = loginHandler;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        loginHandler.handleLogin(event.getPlayer());
    }
}
