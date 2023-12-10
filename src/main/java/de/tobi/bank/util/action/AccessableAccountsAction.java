package de.tobi.bank.util.action;

import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.BedrockUIAction;
import de.tobi.api.inventories.util.InventoryAction;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.bank.BankSystem;
import de.tobi.bank.util.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.geysermc.cumulus.response.FormResponse;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class AccessableAccountsAction implements InventoryAction, BedrockUIAction<SimpleForm> {

    private final CompletableFuture<List<Account>> accounts;

    public AccessableAccountsAction(@NotNull UUID uuid) {
        this.accounts = BankSystem.getInstance().getAccountsAccessable(uuid);
    }

    @Override
    public boolean buildFormAsync(@NotNull Player player) {
        return true;
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull SimpleForm form, @NotNull FormResponse formResponse) {
        if (!(formResponse instanceof SimpleFormResponse simpleFormResponse)) {
            return null;
        }

        if (simpleFormResponse.clickedButtonId() == form.buttons().size() - 1) {
            return new BankAction();
        }

        Account account = this.accounts.join().get(simpleFormResponse.clickedButtonId());
        if (account == null) {
            return null;
        }

        return new AccountAction(account);
    }

    @Override
    public @Nullable FormBuilder<?, SimpleForm, ?> buildForm(@NotNull Player player) {
        SimpleForm.Builder builder = SimpleForm
                .builder()
                .title(this.getDefaultTitle(player));

        for (Account account : this.accounts.join()) {
            builder.button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account").replace("%name", account.name()).replace("%currency", account.currency().name()).replace("%amount", String.valueOf(account.amount())));
        }

        return builder.button(LanguageApi.getMessage(player.getName(), "inventory.back"));
    }

    @Override
    public int getNeededInventorySize(@NotNull Player player) {
        return 54;
    }

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.accessable_accounts.title");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 45) {
            return new BankAction();
        }

        List<Account> accounts = this.accounts.join();

        if (slot >= accounts.size()) {
            return this;
        }

        Account account = accounts.get(slot);
        if (account == null) {
            return this;
        }

        return new AccountAction(account);
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        this.accounts.thenAccept(accounts -> {
            for (int i = 0; i < Math.min(36, accounts.size()); i++) {
                Account account = accounts.get(i);
                String[] accountMessage = LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account").replace("%name", account.name()).replace("%currency", account.currency().name()).replace("%amount", String.valueOf(account.amount())).split("\n");
                ItemBuilder itemBuilder = new ItemBuilder(Material.PAPER).setName(accountMessage[0]);
                for (int j = 1; j < accountMessage.length; j++) {
                    itemBuilder.addLoreLine(accountMessage[j]);
                }
                inventory.setItem(i, itemBuilder.build());
            } // TODO: pageable
        });

        inventory.setItem(45, new ItemBuilder(Material.ENDER_EYE).setName(LanguageApi.getMessage(player.getName(), "inventory.back")).build());
    }
}
