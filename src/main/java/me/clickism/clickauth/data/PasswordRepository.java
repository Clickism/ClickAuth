/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.data;

import me.clickism.clickauth.ClickAuth;
import org.intellij.lang.annotations.Language;

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
            ClickAuth.LOGGER.severe("Failed to create Players table");
            return Optional.empty();
        }
        return Optional.of(repository);
    }

    public boolean hasPassword(UUID uuid) {
        return getPasswordHash(uuid).isPresent();
    }

    public Optional<String> getPasswordHash(UUID uuid) {
        @Language("SQL")
        String sql = "SELECT PasswordHash FROM Players WHERE Uuid = ?";
        return database.query(sql, resultSet -> {
            try {
                return resultSet.getString("PasswordHash");
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

    public boolean createTable() {
        @Language("SQL")
        String sql = """
                CREATE TABLE IF NOT EXISTS players (
                    uuid TEXT PRIMARY KEY,
                    password_hash TEXT NOT NULL
                )
                """;
        return database.execute(sql);
    }
}
