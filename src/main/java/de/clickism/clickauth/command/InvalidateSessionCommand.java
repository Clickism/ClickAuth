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

import static de.clickism.clickauth.message.Messages.*;

public class InvalidateSessionCommand implements TabExecutor {
    public static final String LABEL = "invalidate_session";
    private static final String PERMISSION_SELF = "clickauth.invalidate_session.self";
    private static final String PERMISSION_OTHERS = "clickauth.invalidate_session.others";
    private static final String USAGE = "Usage: /invalidate_session [player]";

    private final PasswordManager passwordManager;
    private final LoginHandler loginHandler;
    private final AuthManager authManager;

    public InvalidateSessionCommand(PasswordManager passwordManager,
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
            case 0 -> invalidateSelf(sender);
            case 1 -> invalidateOther(args[0], sender);
            default -> {
                sender.sendMessage(USAGE);
                return false;
            }
        }
        return true;
    }

    private void invalidateSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(USAGE);
            return;
        }
        // TODO: Is it necessary now?
//        if (!authManager.isAuthenticated(player)) {
//            player.sendMessage(localize(MUST_BE_LOGGED_IN));
//        }
        if (!player.hasPermission(PERMISSION_SELF)) {
            AUTH_FAIL.send(player, localize(NO_PERMISSION));
            return;
        }
        passwordManager.invalidateSession(player.getUniqueId());
        authManager.deauthenticate(player.getUniqueId());
        AUTH_WARN.send(player, localize(INVALIDATED_SESSION));
        loginHandler.handleLogin(player);
    }

    private void invalidateOther(String playerName, CommandSender sender) {
//        if (sender instanceof Player player && !authManager.isAuthenticated(player)) {
//            player.sendMessage(localize(MUST_BE_LOGGED_IN));
//            return;
//        }
        if (!sender.hasPermission(PERMISSION_OTHERS)) {
            AUTH_FAIL.send(sender, localize(NO_PERMISSION));
            return;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        passwordManager.invalidateSession(player.getUniqueId());
        authManager.deauthenticate(player.getUniqueId());
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            AUTH_WARN.send(onlinePlayer, localize(INVALIDATED_SESSION));
            loginHandler.handleLogin(onlinePlayer);
        }
        AUTH_SUCCESS.send(sender, localize(INVALIDATED_SESSION_OTHER, playerName));
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
