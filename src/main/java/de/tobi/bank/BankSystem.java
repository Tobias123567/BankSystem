package de.tobi.bank;

import de.tobi.api.money.util.Currency;
import de.tobi.bank.commands.BankCommand;
import de.tobi.bank.util.Account;
import de.tobi.databases.DatabaseApi;
import de.tobi.databases.database.ConnectionPool;
import de.tobi.databases.util.ConnectionInformation;
import org.bukkit.configuration.Configuration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class BankSystem extends JavaPlugin {

    private static BankSystem instance;

    public static BankSystem getInstance() {
        return instance;
    }

    private ConnectionPool connectionPool;

    @Override
    public void onEnable() {
        instance = this;
        this.saveDefaultConfig();
        Configuration configuration = this.getConfig();

        this.connectionPool = DatabaseApi.openConnectionPool(new ConnectionInformation(configuration.getString("ip"), configuration.getInt("port"), configuration.getString("database"), configuration.getString("username"), configuration.getString("password")));
        this.connectionPool.executeUpdate("CREATE TABLE IF NOT EXISTS bank(id INT NOT NULL PRIMARY KEY AUTO_INCREMENT, name VARCHAR(30) NOT NULL, uuid UUID NOT NULL, currency TINYINT, amount INT NOT NULL DEFAULT 0)");
        this.connectionPool.executeUpdate("CREATE TABLE IF NOT EXISTS bank_access(id INT NOT NULL, uuid UUID NOT NULL, PRIMARY KEY(id, uuid), FOREIGN KEY (id) REFERENCES bank(id) ON DELETE CASCADE)");

        this.getCommand("bank").setExecutor(new BankCommand());
    }

    @NotNull
    public CompletableFuture<@NotNull List<@NotNull Account>> getAccounts(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Account> accounts = new ArrayList<>();
            this.connectionPool.executeQuery("SELECT id, name, currency, uuid, amount FROM bank WHERE uuid = ?", (connection, resultSet) -> {
                try {
                    while (resultSet.next()) {
                        accounts.add(new Account(resultSet.getInt("id"), resultSet.getString("name"), UUID.fromString(resultSet.getString("uuid")), Currency.values()[resultSet.getInt("currency")], resultSet.getInt("amount")));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, uuid);
            return accounts;
        });
    }

    @NotNull
    public CompletableFuture<@NotNull List<@NotNull Account>> getAccountsAccessable(@NotNull UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<Account> accounts = new ArrayList<>();
            this.connectionPool.executeQuery("SELECT ba.id, b.id, b.name, b.uuid, b.currency, b.amount FROM bank_access ba INNER JOIN bank b ON ba.id = b.id WHERE ba.UUID = ?", (connection, resultSet) -> {
                try {
                    while (resultSet.next()) {
                        accounts.add(new Account(resultSet.getInt("id"), resultSet.getString("name"), UUID.fromString(resultSet.getString("uuid")), Currency.values()[resultSet.getInt("currency")], resultSet.getInt("amount")));
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }, uuid);
            return accounts;
        });
    }

    @NotNull
    public CompletableFuture<@Nullable Integer> getAmount(int id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.connectionPool.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT amount FROM bank WHERE id = ?")) {
                    preparedStatement.setInt(1, id);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        if (resultSet.next()) {
                            return resultSet.getInt("amount");
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @NotNull
    public CompletableFuture<@NotNull List<@NotNull UUID>> getAccess(int id) {
        return CompletableFuture.supplyAsync(() -> {
            List<UUID> uuids = new ArrayList<>();
            try (Connection connection = this.connectionPool.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT uuid FROM bank_access WHERE id = ?")) {
                    preparedStatement.setInt(1, id);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            uuids.add(UUID.fromString(resultSet.getString("uuid")));
                        }
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return uuids;
        });
    }

    @NotNull
    public CompletableFuture<Account> createAccount(@NotNull UUID uuid, @NotNull String name, @NotNull Currency currency) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = this.connectionPool.getConnection()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO bank(uuid, name, currency) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS)) {
                    preparedStatement.setString(1, uuid.toString());
                    preparedStatement.setString(2, name);
                    preparedStatement.setInt(3, currency.ordinal());
                    preparedStatement.executeUpdate();
                    try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                        return new Account(resultSet.getInt(1), name, uuid, currency, 0);
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @NotNull
    public CompletableFuture<Void> deleteAccount(int id) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("DELETE FROM bank WHERE id = ?", id));
    }

    @NotNull
    public CompletableFuture<Void> setAmount(int id, int amount) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("UPDATE bank SET amount = ? WHERE id = ?", amount, id));
    }

    @NotNull
    public CompletableFuture<Void> addAmount(int id, int amount) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("UPDATE bank SET amount = amount + ? WHERE id = ?", amount, id));
    }

    @NotNull
    public CompletableFuture<Void> removeAmount(int id, int amount) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("UPDATE bank SET amount = amount - ? WHERE id = ?", amount, id));
    }

    @NotNull
    public CompletableFuture<Void> setName(int id, @NotNull String name) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("UPDATE bank SET name = ? WHERE id = ?", name, id));
    }

    @NotNull
    public CompletableFuture<Void> grantAccess(int id, @NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("INSERT IGNORE INTO bank_access (id, uuid) VALUES (?, ?)", id, uuid));
    }

    @NotNull
    public CompletableFuture<Void> revokeAccess(int id, @NotNull UUID uuid) {
        return CompletableFuture.runAsync(() -> this.connectionPool.executeUpdate("DELETE FROM bank_access  WHERE id = ? AND uuid = ?", id, uuid));
    }
}
