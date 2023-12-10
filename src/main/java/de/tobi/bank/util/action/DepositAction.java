package de.tobi.bank.util.action;

import de.tobi.api.inventories.InventoryManagementApi;
import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.templates.JavaAndBedrockInputAction;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.api.money.MoneyApi;
import de.tobi.bank.BankSystem;
import de.tobi.bank.util.Account;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.geysermc.cumulus.form.CustomForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DepositAction implements JavaAndBedrockInputAction {

    private final Account account;
    private boolean executed;

    public DepositAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @Nullable String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit.title");
    }

    @Override
    public @NotNull String getInputTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit.title");
    }

    @Override
    public @Nullable String getInputPlaceholder(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit.placeholder");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull String input) {
        if (this.executed) {
            return null;
        }
        int amount;
        try {
            amount = Integer.parseInt(input);
        } catch (NumberFormatException ignore) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "no_number").replace("%number", input));
            return this;
        }

        if (amount <= 0) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.min_1"));
            return this;
        }

        if (amount > MoneyApi.getCurrency(player, this.account.currency())) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit.not_enough_money"));
            return this;
        }

        MoneyApi.removeCurrency(player, this.account.currency(), amount);
        BankSystem.getInstance().addAmount(this.account.id(), amount);
        player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit.success").replace("%amount", String.valueOf(amount)));

        this.executed = true;
        return null;
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull CustomForm form) {
        if (this.executed) {
            return;
        }
        InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new ListMembersAction(this.account));
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull Inventory inventory) {
        if (this.executed) {
            return;
        }
        Bukkit.getScheduler().runTask(BankSystem.getInstance(), () -> InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new AccountAction(this.account)));
    }
}
