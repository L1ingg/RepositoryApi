package org.ling;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public abstract class SqlRepository<U, T> implements Repository<U, T> {

    private final KeyConverter<U> keyConverter;
    private final Class<T> clazz;
    private final String tableName;
    private final Connection connection;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Logger logger;

    public SqlRepository(Logger logger, Class<T> clazz, String tableName, Connection connection, KeyConverter<U> keyConverter) {
        this.logger = logger;
        this.clazz = clazz;
        this.tableName = tableName;
        this.connection = connection;
        this.keyConverter = keyConverter;
        createTable();
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " (id TEXT PRIMARY KEY, json TEXT NOT NULL)";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Table " + tableName + " successfully created");
        } catch (SQLException e) {
            logger.severe("Error creating table: " + e.getMessage());
        }
    }

    private U parseKey(String s) {
        return keyConverter.fromString(s);
    }

    private String keyToString(U u) {
        return keyConverter.toString(u);
    }

    @Override
    public Map<U, T> getAll() {
        Map<U, T> items = new HashMap<>();
        String sql = "SELECT id, json FROM " + tableName;

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                U key = parseKey(rs.getString("id"));
                T value = mapper.readValue(rs.getString("json"), clazz);
                items.put(key, value);
            }

        } catch (SQLException | JsonProcessingException e) {
            logger.severe("Error getting all objects: " + e.getMessage());
        }

        return items;
    }

    @Override
    public Optional<T> get(U u) {
        String sql = "SELECT json FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, keyToString(u));
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    T value = mapper.readValue(rs.getString("json"), clazz);
                    return Optional.of(value);
                }
            }
        } catch (SQLException | JsonProcessingException e) {
            logger.severe("Error getting object: " + e.getMessage());
        }

        return Optional.empty();
    }

    @Override
    public void add(U u, T t) {
        String sql = "INSERT INTO " + tableName + " (id, json) VALUES(?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, keyToString(u));
            stmt.setString(2, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(t));
            stmt.executeUpdate();
        } catch (SQLException | JsonProcessingException e) {
            logger.severe("Error adding object: " + e.getMessage());
        }
    }

    @Override
    public boolean hasKey(U u) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE id = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, keyToString(u));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.severe("Error checking key: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean hasValue(T t) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE json = ? LIMIT 1";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, mapper.writeValueAsString(t));
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException | JsonProcessingException e) {
            logger.severe("Error checking value: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void remove(U u) {
        String sql = "DELETE FROM " + tableName + " WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, keyToString(u));
            stmt.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error deleting object: " + e.getMessage());
        }
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public KeyConverter<U> getKeyConverter() {
        return keyConverter;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getTableName() {
        return tableName;
    }

    public Logger getLogger() {
        return logger;
    }

    public Connection getConnection() {
        return connection;
    }
}
