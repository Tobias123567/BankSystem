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

public class WithdrawAction implements JavaAndBedrockInputAction {

    private final Account account;
    private boolean executed;

    public WithdrawAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @Nullable String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.title");
    }

    @Override
    public @NotNull String getInputTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.title");
    }

    @Override
    public @Nullable String getInputPlaceholder(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.placeholder");
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

        if (amount > this.account.amount()) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.not_enough_money"));
            return this;
        }

        if (this.account.uuid().equals(player.getUniqueId())) {
            BankSystem.getInstance().getAmount(this.account.id()).thenAccept(currentAmount -> this.execute(player, currentAmount, amount));
            this.executed = true;
            return null;
        }


        BankSystem.getInstance().getAccess(this.account.id()).thenAccept(uuids -> {
            if (!uuids.contains(player.getUniqueId())) {
                player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.no_access"));
                return;
            }

            Integer currentAmount = BankSystem.getInstance().getAmount(this.account.id()).join();

            this.execute(player, currentAmount, amount);
        });

        this.executed = true;
        return null;
    }

    private void execute(@NotNull Player player, Integer currentAmount, int amount) {
        if (currentAmount == null) {
            return;
        }
        if (amount > currentAmount) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.not_enough_money"));
            return;
        }

        BankSystem.getInstance().removeAmount(this.account.id(), amount).join();
        Integer newAmount = BankSystem.getInstance().getAmount(this.account.id()).join();
        if (newAmount == null || newAmount < 0) {
            return;
        }
        MoneyApi.addCurrency(player, this.account.currency(), amount);
        player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw.success").replace("%amount", String.valueOf(amount)));
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
