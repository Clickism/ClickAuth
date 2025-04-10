/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.authentication;

import me.clickism.clickauth.data.PasswordRepository;

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

        return false; // TODO: Implement
    }

    public void setLastSession(UUID uuid, String ip) {
        // TODO: Implement
    }

    public boolean checkPassword(UUID uuid, String password) {
        return passwordRepository.getPasswordHash(uuid)
                .map(hash -> passwordHasher.check(password, hash))
                .orElse(false);
    }

    public void setPassword(UUID uuid, String password) {
        passwordRepository.setPasswordHash(uuid, passwordHasher.hash(password));
    }

    public boolean hasPassword(UUID uuid) {
        return passwordRepository.hasPassword(uuid);
    }
}
