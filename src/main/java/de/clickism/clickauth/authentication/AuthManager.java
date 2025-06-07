/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.authentication;

import java.util.*;

public class AuthManager {

    private final Set<UUID> authenticatedPlayers = new HashSet<>();
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();

    public boolean isAuthenticated(UUID uuid) {
        return authenticatedPlayers.contains(uuid);
    }

    public void authenticate(UUID uuid) {
        authenticatedPlayers.add(uuid);
        failedAttempts.remove(uuid);
    }

    public void deauthenticate(UUID uuid) {
        authenticatedPlayers.remove(uuid);
    }

    public int getFailedAttempts(UUID uuid) {
        return failedAttempts.getOrDefault(uuid, 0);
    }

    public void incrementFailedAttempts(UUID uuid) {
        int attempts = failedAttempts.getOrDefault(uuid, 0);
        failedAttempts.put(uuid, attempts + 1);
    }

    public void resetFailedAttempts(UUID uuid) {
        failedAttempts.remove(uuid);
    }
}
