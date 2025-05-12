/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth;

import me.clickism.clickauth.authentication.AuthManager;
import me.clickism.clickauth.authentication.BCryptHasher;
import me.clickism.clickauth.authentication.IpHasher;
import me.clickism.clickauth.authentication.PasswordManager;
import me.clickism.clickauth.data.Database;
import me.clickism.clickauth.data.PasswordRepository;
import me.clickism.clickauth.listener.ChatInputListener;
import me.clickism.clickauth.listener.GriefListener;
import me.clickism.clickauth.listener.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

import static me.clickism.clickauth.ClickAuthConfig.*;

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
        new JoinListener(passwordManager, authManager, chatInputListener)
                .registerListener(this);
        new GriefListener(authManager)
                .registerListener(this);
    }

    @Override
    public void onDisable() {
        if (database != null) {
            database.disconnect();
        }
    }
}
