/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.authentication;

import de.clickism.clickauth.ClickAuth;
import de.clickism.clickauth.listener.ChatInputListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.regex.Pattern;

import static de.clickism.clickauth.ClickAuthConfig.*;
import static de.clickism.clickauth.message.Messages.*;

public class LoginHandler {

    private final PasswordManager passwordManager;
    private final AuthManager authManager;
    private final ChatInputListener chatInputListener;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile("[A-Za-z0-9#?!@$%^&*\\-]{8,}");

    public LoginHandler(PasswordManager passwordManager, AuthManager authManager, ChatInputListener chatInputListener) {
        this.passwordManager = passwordManager;
        this.authManager = authManager;
        this.chatInputListener = chatInputListener;
    }

    public void handleLogin(Player player) {
        if (checkLastSession(player)) {
            authManager.authenticate(player);
            sendScheduledMessage(player, localize(WELCOME_BACK, player.getName()));
            return;
        }
        if (passwordManager.hasPassword(player.getUniqueId())) {
            askLogin(player);
            return;
        }
        askRegister(player);
    }

    public void askLogin(Player player) {
        sendScheduledMessage(player, "Enter password:");
        chatInputListener.addChatCallback(player,
                password -> {
                    if (passwordManager.checkPassword(player.getUniqueId(), password)) {
                        // Log in player
                        authenticateAndSaveSession(player);
                        AUTH.send(player, localize(WELCOME_BACK, player.getName()));
                    } else {
                        AUTH_FAIL.send(player, localize(INCORRECT_PASSWORD));
                        authManager.incrementFailedAttempts(player);
                        if (authManager.getFailedAttempts(player) >= CONFIG.get(MAX_LOGIN_ATTEMPTS)) {
                            player.kickPlayer(localize(TOO_MANY_ATTEMPTS));
                            return;
                        }
                        askLogin(player);
                    }
                },
                () -> player.kickPlayer(localize(LOGIN_TIMED_OUT)),
                CONFIG.get(LOGIN_TIMEOUT));
    }

    public void askRegister(Player player) {
        AUTH.send(player, localize(ENTER_NEW_PASSWORD));
        handlePasswordSetup(player, null);
    }

    public void handlePasswordSetup(Player player, @Nullable String enteredPassword) {
        chatInputListener.addChatCallback(player,
                password -> {
                    if (enteredPassword == null) {
                        if (CONFIG.get(VALIDATE_PASSWORDS) && !isValidPassword(password)) {
                            AUTH_FAIL.send(player, localize(INVALID_PASSWORD));
                            askRegister(player);
                            return;
                        }
                        AUTH.send(player, localize(CONFIRM_PASSWORD));
                        handlePasswordSetup(player, password);
                        return;
                    }
                    if (!enteredPassword.equals(password)) {
                        AUTH_FAIL.send(player, localize(PASSWORD_MISMATCH));
                        askRegister(player);
                        return;
                    }
                    if (!passwordManager.setPassword(player.getUniqueId(), password)) {
                        AUTH_FAIL.send(player, localize(FAILED_TO_SET_PASSWORD));
                        askRegister(player);
                        return;
                    }
                    authenticateAndSaveSession(player);
                    AUTH.send(player, localize(PASSWORD_SET_SUCCESSFULLY));
                },
                () -> player.kickPlayer(localize(REGISTRATION_TIMED_OUT)),
                CONFIG.get(LOGIN_TIMEOUT));
    }

    public void authenticateAndSaveSession(Player player) {
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

    private boolean isValidPassword(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private void sendScheduledMessage(Player player, String message) {
        Bukkit.getScheduler().runTask(ClickAuth.INSTANCE, () -> {
            if (player.isOnline()) {
                player.sendMessage(message);
            }
        });
    }
}
