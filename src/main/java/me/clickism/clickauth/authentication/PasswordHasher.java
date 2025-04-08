/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.authentication;

public interface PasswordHasher {
    String hash(String password);

    boolean check(String password, String hash);
}
