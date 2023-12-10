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

public class AddMemberAction implements JavaAndBedrockInputAction {

    private final Account account;

    public AddMemberAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @NotNull String getInputTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.add_member.title");
    }

    @Override
    public @Nullable String getInputPlaceholder(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.add_member.placeholder");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull String input) {
        if (!this.account.uuid().equals(player.getUniqueId())) {
            return new ListMembersAction(this.account);
        }

        Player target = Bukkit.getPlayer(input);
        if (target == null) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.add_member.not_online"));
            return this;
        }

        if (player == target) {
            player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.add_member.not_self"));
        }

        BankSystem.getInstance().grantAccess(this.account.id(), target.getUniqueId());
        player.sendMessage(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.add_member.success").replace("%player", target.getName()));
        return null;
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull CustomForm form) {
        InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new ListMembersAction(this.account));
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull Inventory inventory) {
        Bukkit.getScheduler().runTask(BankSystem.getInstance(), () -> InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new AccountAction(this.account)));
    }
}
