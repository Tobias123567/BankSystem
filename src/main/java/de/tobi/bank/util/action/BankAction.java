package de.tobi.bank.util.action;

import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.inventories.util.templates.JavaAndBedrockButtonsAction;
import de.tobi.api.languagesystem.LanguageApi;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.ModalForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BankAction implements JavaAndBedrockButtonsAction<ModalForm> {

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.title");
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
        switch (buttonIndex) {
            case 0 -> {
                return new YourAccountsAction(player.getUniqueId());
            }
            case 1 -> {
                return new AccessableAccountsAction(player.getUniqueId());
            }
        }
        return this;
    }

    @Override
    public @Nullable FormBuilder<?, ModalForm, ?> buildForm(@NotNull Player player) {
        return ModalForm
                .builder()
                .title(this.getDefaultTitle(player))
                .button1(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.accounts.yours"))
                .button2(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.accounts.accessable"));
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        inventory.setItem(12, new ItemBuilder(Material.GOLD_INGOT).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.accounts.yours")).build());
        inventory.setItem(14, new ItemBuilder(Material.PLAYER_HEAD).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.accounts.accessable")).build());
    }
}
