/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth;

import me.clickism.configured.Config;
import me.clickism.configured.ConfigOption;

public class ClickAuthConfig {
    public static final Config CONFIG = Config.ofYaml("plugins/ClickAuth/config.yml")
            .version(1)
            .header("""
                    ---------------------------------------------------------
                    ClickAuth Config
                    NOTE: RELOAD/RESTART SERVER FOR CHANGES TO TAKE EFFECT
                    ---------------------------------------------------------
                    """);

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
}
