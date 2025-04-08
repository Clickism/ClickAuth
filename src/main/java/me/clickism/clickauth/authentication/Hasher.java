/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.authentication;

public interface Hasher {
    String hash(String string);

    boolean check(String string, String hash);
}
