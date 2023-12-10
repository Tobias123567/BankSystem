package de.tobi.bank.util.action;

import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.BedrockUIAction;
import de.tobi.api.inventories.util.InventoryAction;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.api.playercache.PlayerCache;
import de.tobi.api.playercache.util.PlayerData;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ListMembersAction implements InventoryAction, BedrockUIAction<SimpleForm> {

    private final Account account;
    private final CompletableFuture<List<UUID>> members;

    public ListMembersAction(@NotNull Account account) {
        this.account = account;
        this.members = BankSystem.getInstance().getAccess(this.account.id());
    }

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.listmembers.title");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull SimpleForm form, @NotNull FormResponse formResponse) {
        if (!(formResponse instanceof SimpleFormResponse simpleFormResponse)) {
            return null;
        }

        if (simpleFormResponse.clickedButtonId() == form.buttons().size() - 1) {
            return new AccountAction(this.account);
        }

        if (!this.account.uuid().equals(player.getUniqueId())) {
            return this;
        }

        if (simpleFormResponse.clickedButtonId() == 0) {
            return new AddMemberAction(this.account);
        }

        UUID uuid = this.members.join().get(simpleFormResponse.clickedButtonId());
        if (uuid == null) {
            return null;
        }

        BankSystem.getInstance().revokeAccess(this.account.id(), uuid);
        return new ListMembersAction(this.account);
    }

    @Override
    public @Nullable FormBuilder<?, SimpleForm, ?> buildForm(@NotNull Player player) {
        SimpleForm.Builder builder = SimpleForm
                .builder()
                .title(this.getDefaultTitle(player));

        if (this.account.uuid().equals(player.getUniqueId())) {
            builder.button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.youraccounts.add_member"));
        }

        for (UUID uuid : this.members.join()) {
            String username = Optional.ofNullable(PlayerCache.getPlayerData(uuid)).map(PlayerData::username).orElse("Unknown");
            builder.button(username + "\n" + LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.youraccounts.click_to_remove_member"));
        }

        return builder.button(LanguageApi.getMessage(player.getName(), "inventory.back"));
    }

    @Override
    public int getNeededInventorySize(@NotNull Player player) {
        return 54;
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 45) {
            return new AccountAction(this.account);
        }

        if (slot == 49) {
            if (!this.account.uuid().equals(player.getUniqueId())) {
                return this;
            }
            return new AddMemberAction(this.account);
        }

        if (!this.account.uuid().equals(player.getUniqueId())) {
            return this;
        }

        List<UUID> members = this.members.join();

        if (slot >= members.size()) {
            return this;
        }

        UUID uuid = members.get(slot);
        if (uuid == null) {
            return this;
        }

        BankSystem.getInstance().revokeAccess(this.account.id(), uuid);
        return new ListMembersAction(this.account);
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        this.members.thenAccept(members -> {
            for (int i = 0; i < Math.min(36, members.size()); i++) {
                String username = Optional.ofNullable(PlayerCache.getPlayerData(members.get(i))).map(PlayerData::username).orElse("Unknown");
                inventory.setItem(i, new ItemBuilder(Material.PLAYER_HEAD).setName(username).setLore(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.youraccounts.click_to_remove_member")).setSkullOwner(username).build());
            } // TODO: pageable
        });

        if (this.account.uuid().equals(player.getUniqueId())) {
            inventory.setItem(49, new ItemBuilder(Material.EMERALD).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.youraccounts.add_member")).build());
        }

        inventory.setItem(45, new ItemBuilder(Material.ENDER_EYE).setName(LanguageApi.getMessage(player.getName(), "inventory.back")).build());
    }
}
