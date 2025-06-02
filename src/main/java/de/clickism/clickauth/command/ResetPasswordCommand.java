/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.command;

import de.clickism.clickauth.authentication.AuthManager;
import de.clickism.clickauth.authentication.LoginHandler;
import de.clickism.clickauth.data.PasswordRepository;
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

public class ResetPasswordCommand implements TabExecutor {
    public static final String LABEL = "reset_password";
    private static final String PERMISSION_SELF = "clickauth.reset_password.self";
    private static final String PERMISSION_OTHERS = "clickauth.reset_password.others";

    private final PasswordRepository passwordRepository;
    private final LoginHandler loginHandler;
    private final AuthManager authManager;

    public ResetPasswordCommand(PasswordRepository passwordRepository,
                                LoginHandler loginHandler,
                                AuthManager authManager) {
        this.passwordRepository = passwordRepository;
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
                sender.sendMessage("Usage: /reset_password <player>");
                return false;
            }
        }
        return true;
    }

    private void changeSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Usage: /reset_password <player>");
            return;
        }
        // TODO: Is it necessary now?
        if (!authManager.isAuthenticated(player)) {
            player.sendMessage(localize(MUST_BE_LOGGED_IN));
        }
        if (!player.hasPermission(PERMISSION_SELF)) {
            player.sendMessage(localize(NO_PERMISSION));
            return;
        }
        passwordRepository.setPasswordHash(player.getUniqueId(), null);
        loginHandler.askRegister(player);
    }

    private void changeOther(String playerName, CommandSender sender) {
        // todo: check if executor is logged in
        if (sender instanceof Player player && !authManager.isAuthenticated(player)) {
            player.sendMessage(localize(MUST_BE_LOGGED_IN));
            return;
        }
        if (!sender.hasPermission(PERMISSION_OTHERS)) {
            sender.sendMessage(localize(NO_PERMISSION));
            return;
        }
        @SuppressWarnings("deprecation")
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
        passwordRepository.setPasswordHash(player.getUniqueId(), null);
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null) {
            onlinePlayer.sendMessage(localize(PASSWORD_CHANGED));
            loginHandler.askRegister(onlinePlayer);
        }
        sender.sendMessage(localize(PASSWORD_CHANGED_OTHER, playerName));
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
