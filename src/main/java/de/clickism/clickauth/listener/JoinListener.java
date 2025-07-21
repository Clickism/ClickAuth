/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import de.clickism.clickauth.ClickAuth;
import de.clickism.clickauth.authentication.AuthManager;
import de.clickism.clickauth.authentication.LoginHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinListener implements RegistrableListener {

    private final LoginHandler loginHandler;
    private final AuthManager authManager;

    public JoinListener(LoginHandler loginHandler, AuthManager authManager) {
        this.loginHandler = loginHandler;
        this.authManager = authManager;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        authManager.deauthenticate(event.getPlayer().getUniqueId()); // Deauthenticate player first
        Bukkit.getScheduler().runTask(ClickAuth.INSTANCE, () -> loginHandler.handleLogin(event.getPlayer()));
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        authManager.deauthenticate(event.getPlayer().getUniqueId());
    }
}
