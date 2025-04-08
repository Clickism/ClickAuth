/*
 * Copyright 2020-2025 Clickism
 * Released under the GNU General Public License 3.0.
 * See LICENSE.md for details.
 */

package me.clickism.clickauth.data;

import me.clickism.clickauth.ClickAuth;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class Database {
    private static final String CONNECTION_PREFIX = "jdbc:sqlite:";

    private final Connection connection;

    protected Database(Connection connection) {
        this.connection = connection;
    }

    public static Optional<Database> connect(File file) {
        if (!createParentDirectoriesIfAbsent(file)) {
            ClickAuth.LOGGER.severe("Failed to create missing database file: " + file.getAbsolutePath());
            return Optional.empty();
        }
        String url = CONNECTION_PREFIX + file.getAbsolutePath();
        try {
            Connection connection = DriverManager.getConnection(url);
            ClickAuth.LOGGER.info("Connected to database: " + file.getName());
            return Optional.of(new Database(connection));
        } catch (SQLException e) {
            ClickAuth.LOGGER.severe("Can't connect to database: " + e.getMessage());
            return Optional.empty();
        }
    }

    private static boolean createParentDirectoriesIfAbsent(File file) {
        if (file.exists()) return true;
        File parentFile = file.getParentFile();
        if (parentFile.exists()) return true;
        return parentFile.mkdirs();
    }

    public boolean execute(String sql) {
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
            return true;
        } catch (SQLException e) {
            ClickAuth.LOGGER.severe("SQL execution failed: " + e.getMessage());
            return false;
        }
    }

    public boolean execute(String sql, Object... params) {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            statement.execute();
            return true;
        } catch (SQLException e) {
            ClickAuth.LOGGER.severe("SQL execution failed: " + e.getMessage());
            return false;
        }
    }

    public <T> Optional<T> query(String sql, ResultMapper<T> mapper) {
        try (var statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(sql);
            return Optional.ofNullable(mapper.map(resultSet));
        } catch (SQLException e) {
            ClickAuth.LOGGER.severe("SQL query failed:" + e.getMessage());
            return Optional.empty();
        }
    }

    public <T> Optional<T> query(String sql, ResultMapper<T> mapper, Object... params) {
        try (var statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            ResultSet resultSet = statement.executeQuery();
            return Optional.ofNullable(mapper.map(resultSet));
        } catch (SQLException e) {
            ClickAuth.LOGGER.severe("SQL query failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    @FunctionalInterface
    public interface ResultMapper<T> {
        T map(ResultSet resultSet) throws SQLException;
    }
}

