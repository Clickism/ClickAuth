/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ChatInputListener implements RegistrableListener {

    private final JavaPlugin plugin;
    private final Map<Player, Consumer<String>> callbackMap = new ConcurrentHashMap<>();

    public ChatInputListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void addChatCallback(Player player, Consumer<String> onInput, Runnable onCancel, long timeoutTicks) {
        callbackMap.put(player, onInput);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!onInput.equals(callbackMap.get(player))) return;
            callbackMap.remove(player);
            onCancel.run();
        }, timeoutTicks);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        Consumer<String> callback = callbackMap.get(player);
        if (callback == null) return;
        callbackMap.remove(player);
        String message = event.getMessage();
        Bukkit.getScheduler().runTask(plugin, () -> {
            callback.accept(message);
        });
        event.setMessage("");
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        callbackMap.remove(event.getPlayer());
    }
}
