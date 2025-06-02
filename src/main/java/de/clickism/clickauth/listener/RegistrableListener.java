/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public interface RegistrableListener extends Listener {
    @SuppressWarnings("unchecked")
    default <T extends RegistrableListener> T registerListener(JavaPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        return (T) this;
    }
}
