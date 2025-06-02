/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package de.clickism.clickauth.data;

import de.clickism.clickauth.ClickAuth;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class PasswordRepository {
    private final Database database;

    protected PasswordRepository(Database database) {
        this.database = database;
    }

    public static Optional<PasswordRepository> create(Database database) {
        PasswordRepository repository = new PasswordRepository(database);
        if (!repository.createTable()) {
            ClickAuth.LOGGER.severe("Failed to create players table");
            return Optional.empty();
        }
        return Optional.of(repository);
    }

    public boolean hasPassword(UUID uuid) {
        return getPasswordHash(uuid).isPresent();
    }

    public Optional<String> getPasswordHash(UUID uuid) {
        @Language("SQL")
        String sql = "SELECT password_hash FROM players WHERE uuid = ?";
        return database.query(sql, resultSet -> {
            try {
                return resultSet.getString("password_hash");
            } catch (SQLException e) {
                ClickAuth.LOGGER.severe("Failed to get password hash: " + e.getMessage());
                return null;
            }
        }, uuid.toString());
    }

    public boolean setPasswordHash(UUID uuid, String passwordHash) {
        @Language("SQL")
        String sql = "INSERT OR REPLACE INTO players (uuid, password_hash) VALUES (?, ?)";
        return database.execute(sql, uuid.toString(), passwordHash);
    }

    public Optional<String> getLastIp(UUID uuid) {
        @Language("SQL")
        String sql = "SELECT last_ip FROM players WHERE uuid = ?";
        return database.query(sql, resultSet -> {
            try {
                return resultSet.getString("last_ip");
            } catch (SQLException e) {
                ClickAuth.LOGGER.severe("Failed to get last IP: " + e.getMessage());
                return null;
            }
        }, uuid.toString());
    }

    public boolean setLastIp(UUID uuid, @Nullable String lastIp) {
        @Language("SQL")
        String sql = "UPDATE players SET last_ip = ? WHERE uuid = ?";
        return database.execute(sql, lastIp, uuid.toString());
    }

    public boolean createTable() {
        @Language("SQL")
        String sql = """
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    password_hash TEXT,
                    last_ip TEXT
                )
                """;
        return database.execute(sql);
    }
}
