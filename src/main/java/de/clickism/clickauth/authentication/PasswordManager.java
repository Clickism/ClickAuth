/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.authentication;

import de.clickism.clickauth.data.PasswordRepository;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class PasswordManager {
    private final PasswordRepository passwordRepository;
    private final Hasher passwordHasher;
    private final Hasher ipHasher;

    public PasswordManager(PasswordRepository passwordRepository,
                           Hasher passwordHasher,
                           Hasher ipHasher) {
        this.passwordRepository = passwordRepository;
        this.passwordHasher = passwordHasher;
        this.ipHasher = ipHasher;
    }

    public boolean checkLastSession(UUID uuid, String ip) {
        return passwordRepository.getLastIp(uuid)
                .map(lastIp -> ipHasher.check(ip, lastIp))
                .orElse(false);
    }

    public boolean setLastSession(UUID uuid, @NotNull String ip) {
        return passwordRepository.setLastIp(uuid, ipHasher.hash(ip));
    }

    public boolean invalidateSession(UUID uuid) {
        return passwordRepository.setLastIp(uuid, null);
    }

    public boolean checkPassword(UUID uuid, String password) {
        return passwordRepository.getPasswordHash(uuid)
                .map(hash -> passwordHasher.check(password, hash))
                .orElse(false);
    }

    public boolean setPassword(UUID uuid, @NotNull String password) {
        return passwordRepository.setPasswordHash(uuid, passwordHasher.hash(password));
    }

    public boolean resetPassword(UUID uuid) {
        return passwordRepository.setPasswordHash(uuid, null);
    }

    public boolean hasPassword(UUID uuid) {
        return passwordRepository.hasPassword(uuid);
    }
}
