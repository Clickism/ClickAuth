/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.listener;

import me.clickism.clickauth.ClickAuth;
import me.clickism.clickauth.authentication.AuthManager;
import me.clickism.clickauth.authentication.PasswordManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

import static me.clickism.clickauth.ClickAuthConfig.*;

public class JoinListener implements RegistrableListener {

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
            authManager.authenticate(player);
            sendScheduledMessage(player, "Welcome back.");
            return;
        }
        if (passwordManager.hasPassword(playerUuid)) {
            askLogin(player);
            return;
        }
        askRegister(player);
    }

    private void askLogin(Player player) {
        sendScheduledMessage(player, "Enter password:");
        chatInputListener.addChatCallback(player,
                password -> {
                    if (passwordManager.checkPassword(player.getUniqueId(), password)) {
                        // Log in player
                        authenticateAndSaveSession(player);
                        player.sendMessage("Welcome back.");
                    } else {
                        player.sendMessage("Incorrect password, please try again.");
                        authManager.incrementFailedAttempts(player);
                        if (authManager.getFailedAttempts(player) >= CONFIG.get(MAX_LOGIN_ATTEMPTS)) {
                            player.kickPlayer("Too many failed attempts.");
                            return;
                        }
                        askLogin(player);
                    }
                },
                () -> player.kickPlayer("Login timed out."),
                CONFIG.get(LOGIN_TIMEOUT));
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
                    authenticateAndSaveSession(player);
                    player.sendMessage("Password set. You can now log in.");
                },
                () -> player.kickPlayer("Registration timed out."),
                CONFIG.get(LOGIN_TIMEOUT));
    }

    private void authenticateAndSaveSession(Player player) {
        authManager.authenticate(player);
        if (!CONFIG.get(REMEMBER_SESSIONS)) return;
        String ip = getIpAddress(player).orElse(null);
        if (ip == null) return;
        passwordManager.setLastSession(player.getUniqueId(), ip);
    }

    private boolean checkLastSession(Player player) {
        if (!CONFIG.get(REMEMBER_SESSIONS)) return false;
        return getIpAddress(player)
                .map(ip -> passwordManager.checkLastSession(player.getUniqueId(), ip))
                .orElse(false);
    }

    private Optional<String> getIpAddress(Player player) {
        return Optional.ofNullable(player.getAddress())
                .map(InetSocketAddress::getHostString);
    }

    private void sendScheduledMessage(Player player, String message) {
        Bukkit.getScheduler().runTask(ClickAuth.INSTANCE, () -> {
            if (player.isOnline()) {
                player.sendMessage(message);
            }
        });
    }
}
