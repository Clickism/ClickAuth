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
import me.clickism.clickauth.listener.JoinListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public final class ClickAuth extends JavaPlugin {

    public static Logger LOGGER;

    @Override
    public void onLoad() {
        LOGGER = getLogger();
    }

    @Override
    public void onEnable() {
        File databaseFile = new File(getDataFolder(), "database.db");
        PasswordRepository passwordRepository = Database.connect(databaseFile)
                .flatMap(PasswordRepository::create)
                .orElse(null);
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
    }
}
