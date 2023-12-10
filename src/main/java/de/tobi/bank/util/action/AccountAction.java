package de.tobi.bank.util.action;

import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.inventories.util.templates.JavaAndBedrockButtonsAction;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.bank.util.Account;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AccountAction implements JavaAndBedrockButtonsAction<SimpleForm> {

    private final Account account;

    public AccountAction(@NotNull Account account) {
        this.account = account;
    }

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.title");
    }

    @Override
    public int getNeededInventorySize(@NotNull Player player) {
        return 36;
    }

    @Override
    public @Nullable Integer mapSlotIdToButtonIndex(@NotNull Player player, @NotNull Inventory inventory, int slotId) {
        return switch (slotId) {
            case 9 -> 0;
            case 11 -> 1;
            case 13 -> 2;
            case 15 -> 3;
            case 17 -> 4;
            case 27 -> 5;
            default -> null;
        };
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, int buttonIndex) {
        return switch (buttonIndex) {
            case 0 -> new WithdrawAction(this.account);
            case 1 -> new DepositAction(this.account);
            case 2 -> new ListMembersAction(this.account);
            case 3 -> {
                if (!this.account.uuid().equals(player.getUniqueId())) {
                    yield new AccessableAccountsAction(player.getUniqueId());
                }
                yield new SetNameAction(this.account);
            }
            case 4 -> {
                if (!this.account.uuid().equals(player.getUniqueId())) {
                    yield this;
                }
                yield new DeleteAccountAction(this.account);
            }
            case 5 -> this.account.uuid().equals(player.getUniqueId()) ? new YourAccountsAction(player.getUniqueId()) : new AccessableAccountsAction(player.getUniqueId());
            default -> null;
        };
    }

    @Override
    public @Nullable FormBuilder<?, SimpleForm, ?> buildForm(@NotNull Player player) {
        SimpleForm.Builder builder =  SimpleForm
                .builder()
                .title(this.getDefaultTitle(player))
                .button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw").replace("%amount", String.valueOf(this.account.amount())))
                .button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit"))
                //.button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.transfer")) TODO
                .button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.members"));

        if (this.account.uuid().equals(player.getUniqueId())) {
            builder
                    .button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name"))
                    .button(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.delete"));
        }

        return builder.button(LanguageApi.getMessage(player.getName(), "inventory.back"));
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        String[] withdraw = LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.withdraw").replace("%amount", String.valueOf(this.account.amount())).split("\n");
        ItemBuilder itemBuilder = new ItemBuilder(Material.GOLD_INGOT).setName(withdraw[0]);
        for (int i = 1; i < withdraw.length; i++) {
            itemBuilder.addLoreLine(withdraw[i]);
        }

        inventory.setItem(9, itemBuilder.build());
        inventory.setItem(11, new ItemBuilder(Material.GOLD_NUGGET).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.deposit")).build());
        inventory.setItem(13, new ItemBuilder(Material.PLAYER_HEAD).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.members")).build());

        inventory.setItem(27, new ItemBuilder(Material.ENDER_EYE).setName(LanguageApi.getMessage(player.getName(), "inventory.back")).build());

        if (!this.account.uuid().equals(player.getUniqueId())) {
            return;
        }

        inventory.setItem(15, new ItemBuilder(Material.NAME_TAG).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.set_name")).build());
        inventory.setItem(17, new ItemBuilder(Material.BARRIER).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.account.delete")).build());
    }
}
