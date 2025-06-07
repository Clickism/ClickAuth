/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.message;

import de.clickism.clickauth.ClickAuth;
import de.clickism.configured.localization.Localization;
import de.clickism.configured.localization.LocalizationKey;
import de.clickism.configured.localization.Parameters;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public enum Messages implements LocalizationKey {
    @Parameters("version")
    UPDATE,
    ENTER_PASSWORD,
    @Parameters("player")
    WELCOME_BACK,
    INCORRECT_PASSWORD,
    INVALID_PASSWORD,
    TOO_MANY_ATTEMPTS,
    LOGIN_TIMED_OUT,
    ENTER_NEW_PASSWORD,
    CONFIRM_PASSWORD,
    PASSWORD_MISMATCH,
    FAILED_TO_SET_PASSWORD,
    PASSWORD_SET_SUCCESSFULLY,
    REGISTRATION_TIMED_OUT,
    NO_PERMISSION,
    PASSWORD_CHANGED,
    @Parameters("player")
    PASSWORD_CHANGED_OTHER,
    MUST_BE_LOGGED_IN,
    INVALIDATED_SESSION,
    @Parameters("player")
    INVALIDATED_SESSION_OTHER;

    public static final MessageType AUTH = MessageType.silent("&6[🔑] &e", "&8< &b%s &8>");
    public static final MessageType AUTH_FAIL = new MessageType("&6[🔑] &c", "&8< &b%s &8>") {
        @Override
        public void playSound(Player player) {
            MessageType.FAIL.playSound(player);
        }
    };
    public static final MessageType AUTH_WARN = new MessageType("&6[🔑] &e", "&8< &b%s &8>") {
        @Override
        public void playSound(Player player) {
            MessageType.WARN.playSound(player);
        }
    };
    public static final MessageType AUTH_CONFIRM = new MessageType("&6[🔑] &a", "&8< &a%s &8>") {
        @Override
        public void playSound(Player player) {
            MessageType.CONFIRM.playSound(player);
        }
    };
    public static final MessageType AUTH_PORTAL = new MessageType("&6[🔑] &a", "&8< &b%s &8>") {
        @Override
        public void playSound(Player player) {
            player.playSound(player, Sound.BLOCK_PORTAL_TRAVEL, .2f, 1f);
        }
    };

    public static final Localization LOCALIZATION =
            Localization.of(lang -> "plugins/ClickAuth/lang/" + lang + ".json")
                    .resourceProvider(ClickAuth.class, lang -> "/lang/" + lang + ".json")
                    .version(1)
                    .fallbackLanguage("en_US");

    public static String localize(LocalizationKey key, Object... params) {
        return LOCALIZATION.get(key, params);
    }
}
