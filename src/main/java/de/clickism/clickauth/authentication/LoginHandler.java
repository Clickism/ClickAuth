/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.authentication;

import de.clickism.clickauth.listener.ChatInputListener;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static de.clickism.clickauth.ClickAuthConfig.*;
import static de.clickism.clickauth.message.Messages.*;

public class LoginHandler {

    private static Pattern passwordPattern = null;

    private final PasswordManager passwordManager;
    private final AuthManager authManager;
    private final ChatInputListener chatInputListener;

    public LoginHandler(PasswordManager passwordManager, AuthManager authManager, ChatInputListener chatInputListener) {
        this.passwordManager = passwordManager;
        this.authManager = authManager;
        this.chatInputListener = chatInputListener;
    }

    public static void setPasswordPattern(Pattern passwordPattern) {
        LoginHandler.passwordPattern = passwordPattern;
    }

    public void handleLogin(Player player) {
        if (checkLastSession(player)) {
            authManager.authenticate(player.getUniqueId());
            AUTH_CONFIRM.send(player, localize(WELCOME_BACK, player.getName()));
            return;
        }
        if (passwordManager.hasPassword(player.getUniqueId())) {
            askLogin(player);
            return;
        }
        askRegister(player);
    }

    private void askLogin(Player player) {
        AUTH.send(player, localize(ENTER_PASSWORD));
        chatInputListener.addChatCallback(player,
                password -> {
                    UUID uuid = player.getUniqueId();
                    if (passwordManager.checkPassword(uuid, password)) {
                        // Log in player
                        authenticateAndSaveSession(player);
                        AUTH_PORTAL.send(player, localize(WELCOME_BACK, player.getName()));
                    } else {
                        AUTH_FAIL.send(player, localize(INCORRECT_PASSWORD));
                        authManager.incrementFailedAttempts(uuid);
                        if (authManager.getFailedAttempts(uuid) >= CONFIG.get(MAX_LOGIN_ATTEMPTS)) {
                            authManager.resetFailedAttempts(uuid);
                            player.kickPlayer("§c" + localize(TOO_MANY_ATTEMPTS));
                            return;
                        }
                        askLogin(player);
                    }
                },
                () -> player.kickPlayer(localize(LOGIN_TIMED_OUT)),
                CONFIG.get(LOGIN_TIMEOUT));
    }

    private void askRegister(Player player) {
        AUTH.send(player, localize(ENTER_NEW_PASSWORD));
        handlePasswordSetup(player, null);
    }

    private void handlePasswordSetup(Player player, @Nullable String enteredPassword) {
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
                    AUTH_CONFIRM.send(player, localize(PASSWORD_SET_SUCCESSFULLY));
                },
                () -> player.kickPlayer(localize(REGISTRATION_TIMED_OUT)),
                CONFIG.get(LOGIN_TIMEOUT));
    }

    private void authenticateAndSaveSession(Player player) {
        UUID uuid = player.getUniqueId();
        authManager.authenticate(uuid);
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
        return passwordPattern.matcher(password).matches();
    }
}
