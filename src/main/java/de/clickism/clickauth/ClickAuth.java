/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth;

import de.clickism.clickauth.authentication.*;
import de.clickism.clickauth.command.InvalidateSessionCommand;
import de.clickism.clickauth.command.ResetPasswordCommand;
import de.clickism.clickauth.data.Database;
import de.clickism.clickauth.data.PasswordRepository;
import de.clickism.clickauth.listener.ChatInputListener;
import de.clickism.clickauth.listener.CommandListener;
import de.clickism.clickauth.listener.GriefListener;
import de.clickism.clickauth.listener.JoinListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

import static de.clickism.clickauth.ClickAuthConfig.CONFIG;

public final class ClickAuth extends JavaPlugin {

    public static ClickAuth INSTANCE;
    public static Logger LOGGER;

    private @Nullable Database database;

    @Override
    public void onLoad() {
        INSTANCE = this;
        LOGGER = getLogger();
    }

    @Override
    public void onEnable() {
        CONFIG.load();
        File databaseFile = new File(getDataFolder(), "database.db");
        this.database = Database.connect(databaseFile).orElse(null);
        PasswordRepository passwordRepository = null;
        if (database != null) {
            passwordRepository = PasswordRepository.create(database).orElse(null);
        }
        if (passwordRepository == null) {
            LOGGER.severe("Failed to initialize database.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        PasswordManager passwordManager = new PasswordManager(passwordRepository,
                new BCryptHasher(), new IpHasher());
        AuthManager authManager = new AuthManager();
        ChatInputListener chatInputListener = new ChatInputListener(this)
                .registerListener(this);
        LoginHandler loginHandler = new LoginHandler(passwordManager, authManager, chatInputListener);
        new JoinListener(loginHandler)
                .registerListener(this);
        new GriefListener(authManager)
                .registerListener(this);
        new CommandListener(authManager)
                .registerListener(this);
        registerCommand(ResetPasswordCommand.LABEL, new ResetPasswordCommand(
                passwordManager, loginHandler, authManager));
        registerCommand(InvalidateSessionCommand.LABEL, new InvalidateSessionCommand(
                passwordManager, loginHandler, authManager));
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.disconnect();
        }
    }

    private void registerCommand(String name, TabExecutor executor) {
        PluginCommand command = getCommand(name);
        if (command == null) {
            LOGGER.warning("Command '" + name + "' not found in plugin.yml");
            return;
        }
        command.setExecutor(executor);
        command.setTabCompleter(executor);
    }
}
