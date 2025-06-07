/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.command;

import de.clickism.clickauth.authentication.AuthManager;
import de.clickism.clickauth.authentication.LoginHandler;
import de.clickism.clickauth.authentication.PasswordManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static de.clickism.clickauth.message.Messages.*;

public class ResetPasswordCommand implements TabExecutor {
    public static final String LABEL = "reset_password";
    public static final String USAGE = "Usage: /reset_password <player>";
    private static final String PERMISSION_SELF = "clickauth.reset_password.self";
    private static final String PERMISSION_OTHERS = "clickauth.reset_password.others";
    private final PasswordManager passwordManager;
    private final LoginHandler loginHandler;
    private final AuthManager authManager;

    public ResetPasswordCommand(PasswordManager passwordManager,
                                LoginHandler loginHandler,
                                AuthManager authManager) {
        this.passwordManager = passwordManager;
        this.loginHandler = loginHandler;
        this.authManager = authManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (!label.equals(LABEL)) return false;
        switch (args.length) {
            case 0 -> changeSelf(sender);
            case 1 -> changeOther(args[0], sender);
            default -> {
                AUTH_FAIL.send(sender, USAGE);
                return false;
            }
        }
        return true;
    }

    private void changeSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            AUTH_FAIL.send(sender, USAGE);
            return;
        }
        UUID uuid = player.getUniqueId();
        if (!player.hasPermission(PERMISSION_SELF)) {
            AUTH_FAIL.send(player, localize(NO_PERMISSION));
            return;
        }
        passwordManager.resetPassword(uuid);
        passwordManager.invalidateSession(uuid);
        authManager.deauthenticate(uuid);
        AUTH_WARN.send(player, localize(PASSWORD_CHANGED));
        loginHandler.handleLogin(player);
    }

    private void changeOther(String playerName, CommandSender sender) {
        if (!sender.hasPermission(PERMISSION_OTHERS)) {
            AUTH_FAIL.send(sender, localize(NO_PERMISSION));
            return;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        UUID uuid = player.getUniqueId();
        passwordManager.resetPassword(uuid);
        passwordManager.invalidateSession(uuid);
        authManager.deauthenticate(uuid);
        AUTH_CONFIRM.send(sender, localize(PASSWORD_CHANGED_OTHER, playerName));
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            AUTH_WARN.send(onlinePlayer, localize(PASSWORD_CHANGED));
            loginHandler.handleLogin(onlinePlayer);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String label, @NotNull String[] args) {
        if (!label.equals(LABEL)) return null;
        if (args.length != 1) return null;
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(Objects::nonNull)
                .map(OfflinePlayer::getName)
                .toList();
    }
}
