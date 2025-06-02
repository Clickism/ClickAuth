/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.authentication;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class BCryptHasher implements Hasher {
    @Override
    public String hash(String string) {
        return BCrypt.withDefaults().hashToString(12, string.toCharArray());
    }

    @Override
    public boolean check(String string, String hash) {
        return BCrypt.verifyer().verify(string.toCharArray(), hash).verified;
    }
}
