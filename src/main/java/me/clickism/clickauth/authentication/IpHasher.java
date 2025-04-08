/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.authentication;

public class IpHasher implements Hasher {
    @Override
    public String hash(String string) {
        return String.valueOf(string.hashCode());
    }

    @Override
    public boolean check(String string, String hash) {
        return String.valueOf(string.hashCode()).equals(hash);
    }
}
