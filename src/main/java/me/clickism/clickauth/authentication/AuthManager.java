/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.authentication;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;

public class AuthManager {

    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();

    public boolean isAuthenticated(HumanEntity player) {
        return authenticatedPlayers.contains(player.getUniqueId());
    }

    public void authenticate(HumanEntity player) {
        authenticatedPlayers.add(player.getUniqueId());
    }

    public int getFailedAttempts(HumanEntity player) {
        return failedAttempts.getOrDefault(player.getUniqueId(), 0);
    }

    public void incrementFailedAttempts(HumanEntity player) {
        int attempts = failedAttempts.getOrDefault(player.getUniqueId(), 0);
        failedAttempts.put(player.getUniqueId(), attempts + 1);
    }
}
