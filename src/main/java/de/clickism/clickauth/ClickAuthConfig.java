/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth;

import de.clickism.clickauth.message.Messages;
import de.clickism.configured.Config;
import de.clickism.configured.ConfigOption;

public class ClickAuthConfig {
    public static final Config CONFIG =
            Config.of("plugins/ClickAuth/config.yml")
                    .version(1)
                    .header("""
                            ---------------------------------------------------------
                            ClickAuth Config
                            NOTE: RELOAD/RESTART SERVER FOR CHANGES TO TAKE EFFECT
                            ---------------------------------------------------------
                            """);

    public static final ConfigOption<String> LANGUAGE =
            CONFIG.optionOf("language", "en_US")
                    .description("""
                            Language for messages.
                            Available languages: en_US, de_DE
                            """)
                    .appendDefaultValue()
                    .onLoad(lang -> Messages.LOCALIZATION
                            .language(lang)
                            .load());

    public static final ConfigOption<Integer> MAX_LOGIN_ATTEMPTS =
            CONFIG.optionOf("max_login_attempts", 3)
                    .description("""
                            Maximum number of login attempts before the player is kicked.
                            Use 0 to disable this feature.
                            """)
                    .appendDefaultValue();

    public static final ConfigOption<Integer> LOGIN_TIMEOUT =
            CONFIG.optionOf("login_timeout", 20 * 20)
                    .description("""
                            Time in TICKS before a player is kicked for not logging in.
                            Use 0 to disable this feature.
                            """)
                    .appendDefaultValue();

    public static final ConfigOption<Boolean> REMEMBER_SESSIONS =
            CONFIG.optionOf("remember_sessions", true)
                    .description("""
                            Whether to keep track of the last session of a player.
                            Player will be automatically logged in if they have the
                            same IP address from their last successful login.
                            """)
                    .appendDefaultValue();

    public static final ConfigOption<Boolean> VALIDATE_PASSWORDS =
            CONFIG.optionOf("validate_passwords", true)
                    .description("""
                            Whether to validate passwords against invalid characters.
                            If enabled, passwords must not contain spaces or non-standard
                            characters and must be at least 8 characters long.
                            If disabled, any character is allowed.
                            """)
                    .appendDefaultValue();
}
