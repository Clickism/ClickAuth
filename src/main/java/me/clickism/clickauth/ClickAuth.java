/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth;

import me.clickism.clickauth.data.Database;
import me.clickism.clickauth.data.PasswordRepository;
import me.clickism.clickauth.authentication.BCryptPasswordHasher;
import me.clickism.clickauth.authentication.PasswordManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.UUID;
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
        PasswordManager passwordManager = new PasswordManager(passwordRepository, new BCryptPasswordHasher());
        UUID[] uuids = new UUID[10];
        for (int i = 0; i < 10; i++) {
            uuids[i] = UUID.randomUUID();
            passwordManager.setPassword(uuids[i], "password" + i);
        }
        for (int i = 0; i < 10; i++) {
            LOGGER.info("Result: " + passwordManager.checkPassword(uuids[i], "password" + i));
        }
    }
}
