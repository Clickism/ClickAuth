/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.listener;

import me.clickism.clickauth.authentication.AuthManager;
import me.clickism.clickauth.authentication.PasswordManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.UUID;

public class JoinListener implements RegistrableListener {

    private static final long LOGIN_TIMEOUT = 20 * 20;
    private static final long MAX_LOGIN_ATTEMPTS = 3;

    private final PasswordManager passwordManager;
    private final AuthManager authManager;
    private final ChatInputListener chatInputListener;

    public JoinListener(PasswordManager passwordManager,
                        AuthManager authManager,
                        ChatInputListener chatInputListener) {
        this.passwordManager = passwordManager;
        this.authManager = authManager;
        this.chatInputListener = chatInputListener;
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();
        if (checkLastSession(player)) {
            return;
        }
        if (passwordManager.hasPassword(playerUuid)) {
            askLogin(player);
            return;
        }
        askRegister(player);
    }

    private void askLogin(Player player) {
        player.sendMessage("Enter password:");
        chatInputListener.addChatCallback(player,
                password -> {
                    if (passwordManager.checkPassword(player.getUniqueId(), password)) {
                        // Log in player
                        player.sendMessage("Welcome back.");
                        authManager.authenticate(player);
                    } else {
                        player.sendMessage("Incorrect password, please try again.");
                        authManager.incrementFailedAttempts(player);
                        if (authManager.getFailedAttempts(player) >= MAX_LOGIN_ATTEMPTS) {
                            player.kickPlayer("Too many failed attempts.");
                            return;
                        }
                        askLogin(player);
                    }
                },
                () -> player.kickPlayer("Login timed out."),
                LOGIN_TIMEOUT);
    }

    private void askRegister(Player player) {
        player.sendMessage("Enter new password:");
        handlePasswordSetup(player, null);
    }

    private void handlePasswordSetup(Player player, @Nullable String enteredPassword) {
        chatInputListener.addChatCallback(player,
                password -> {
                    if (enteredPassword == null) {
                        player.sendMessage("Confirm password:");
                        handlePasswordSetup(player, password);
                        return;
                    }
                    if (!enteredPassword.equals(password)) {
                        player.sendMessage("Passwords do not match. Please try again.");
                        askRegister(player);
                        return;
                    }
                    if (!passwordManager.setPassword(player.getUniqueId(), password)) {
                        player.sendMessage("Failed to set password. Please try again.");
                        askRegister(player);
                        return;
                    }
                    authManager.authenticate(player);
                    player.sendMessage("Password set. You can now log in.");
                },
                () -> player.kickPlayer("Registration timed out."),
                LOGIN_TIMEOUT);
    }

    private boolean checkLastSession(Player player) {
        InetSocketAddress socketAddress = player.getAddress();
        if (socketAddress != null) {
            String ip = socketAddress.getHostString();
            return passwordManager.checkLastSession(player.getUniqueId(), ip);
        }
        return false;
    }
}
