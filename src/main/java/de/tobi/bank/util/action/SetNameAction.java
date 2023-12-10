package de.tobi.bank.util.action;

import de.tobi.api.inventories.InventoryManagementApi;
import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.templates.JavaAndBedrockInputAction;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.bank.BankSystem;
import de.tobi.bank.util.Account;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.geysermc.cumulus.form.CustomForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SetNameAction implements JavaAndBedrockInputAction {

    private final Account account;
    private boolean executed;

    public SetNameAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @Nullable String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name.title");
    }

    @Override
    public @NotNull String getInputTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name.title");
    }

    @Override
    public @Nullable String getInputPlaceholder(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name.placeholder");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull String input) {
        if (this.executed) {
            return null;
        }
        if (input.length() > 30) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name.max_30"));
            return this;
        }

        BankSystem.getInstance().setName(this.account.id(), input);
        player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name.success").replace("%name", input));

        this.executed = true;
        return null;
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull CustomForm form) {
        if (this.executed) {
            return;
        }
        InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new AccountAction(this.account));
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull Inventory inventory) {
        if (this.executed) {
            return;
        }
        Bukkit.getScheduler().runTask(BankSystem.getInstance(), () -> InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new AccountAction(this.account)));
    }
}
