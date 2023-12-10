package de.tobi.bank.util.action;

import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.inventories.util.templates.JavaAndBedrockButtonsAction;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.bank.BankSystem;
import de.tobi.bank.util.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DeleteAccountAction implements JavaAndBedrockButtonsAction<ModalForm> {

    private final Account account;

    public DeleteAccountAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.delete.title");
    }

    @Override
    public @Nullable Integer mapSlotIdToButtonIndex(@NotNull Player player, @NotNull Inventory inventory, int slotId) {
        return switch (slotId) {
            case 12 -> 0;
            case 14 -> 1;
            default -> null;
        };
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, int buttonIndex) {
        if (!this.account.uuid().equals(player.getUniqueId())) {
            return new AccountAction(this.account);
        }
        if (buttonIndex == 1) {
            BankSystem.getInstance().deleteAccount(this.account.id());
            return new BankAction();
        }
        return new AccountAction(this.account);
    }

    @Override
    public @Nullable FormBuilder<?, ModalForm, ?> buildForm(@NotNull Player player) {
        return ModalForm
                .builder()
                .title(this.getDefaultTitle(player))
                .content(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.delete.confirm"))
                .button1(LanguageApi.getMessage(player.getName(), "inventory.back"))
                .button2(LanguageApi.getMessage(player.getName(), "inventory.confirm"));
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        inventory.setItem(12, new ItemBuilder(Material.RED_DYE).setName(LanguageApi.getMessage(player.getName(), "inventory.back")).build());
        inventory.setItem(14, new ItemBuilder(Material.LIME_DYE).setName(LanguageApi.getMessage(player.getName(), "inventory.confirm")).build());
    }
}
