package de.tobi.bank.util.action;

import de.tobi.api.inventories.InventoryManagementApi;
import de.tobi.api.inventories.util.Action;
import de.tobi.api.inventories.util.BedrockUIAction;
import de.tobi.api.inventories.util.InventoryAction;
import de.tobi.api.inventories.util.ItemBuilder;
import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.api.money.util.Currency;
import de.tobi.bank.BankSystem;
import de.tobi.bank.tutorial.BankTutorial;
import de.tobi.bank.util.action.BankAction;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.form.util.FormBuilder;
import org.geysermc.cumulus.response.CustomFormResponse;
import org.geysermc.cumulus.response.FormResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CreateAccountAction implements InventoryAction, BedrockUIAction<CustomForm> {

    @Override
    public @NotNull String getDefaultTitle(@NotNull Player player) {
        return LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.create_account.title");
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull CustomForm form, @NotNull FormResponse formResponse) {
        if (!(formResponse instanceof CustomFormResponse customFormResponse)) {
            return null;
        }

        Currency currency = Currency.values()[customFormResponse.asDropdown(0)];
        String name = customFormResponse.asInput(1);

        BankSystem.getInstance().createAccount(player.getUniqueId(), name, currency);
        if (BankSystem.TUTORIAL_ENABLED) {
            BankTutorial tutorial = BankTutorial.getCurrentModule(player);
            if (tutorial != null && tutorial.getStep() == 1) {
                tutorial.executeNextStep();
            }
        }
        return new BankAction();
    }

    @Override
    public void executeClose(@NotNull Player player, @NotNull CustomForm form) {
        InventoryManagementApi.addInventoryActionOrSendBedrockUI(player, new YourAccountsAction(player.getUniqueId()));
    }

    @Override
    public @Nullable FormBuilder<?, CustomForm, ?> buildForm(@NotNull Player player) {
        List<String> options = new ArrayList<>();
        for (Currency currency : Currency.values()) {
            options.add(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.currency." + currency.name().toLowerCase()));
        }

        return CustomForm
                .builder()
                .title(this.getDefaultTitle(player))
                .dropdown(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.currency"), options, 0)
                .input(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.name"), LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.name.placeholder"));
    }

    @Override
    public int getNeededInventorySize(@NotNull Player player) {
        return 36;
    }

    @Override
    public @Nullable Action execute(@NotNull Player player, @NotNull InventoryClickEvent event) {
        int slot = event.getSlot();

        if (slot == 27) {
            return new YourAccountsAction(player.getUniqueId());
        }

        Currency currency = switch (slot) {
            case 11 -> Currency.PRIME_MARKS;
            case 13 -> Currency.PRIME_GOLD;
            case 15 -> Currency.PRIME_ORBS;
            default -> null;
        };

        if (currency == null) {
            return this;
        }

        BankSystem.getInstance().createAccount(player.getUniqueId(), LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.new_account"), currency);
        if (BankSystem.TUTORIAL_ENABLED) {
            BankTutorial tutorial = BankTutorial.getCurrentModule(player);
            if (tutorial != null && tutorial.getStep() == 1) {
                tutorial.executeNextStep();
            }
        }
        return new BankAction();
    }

    @Override
    public void fillInventory(@NotNull Player player, @NotNull Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setName("").build();
        for (int i = 0; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }

        inventory.setItem(11, new ItemBuilder(Material.PAPER).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.currency.prime_marks")).build());
        inventory.setItem(13, new ItemBuilder(Material.GOLD_INGOT).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.currency.prime_gold")).build());
        inventory.setItem(15, new ItemBuilder(Material.EXPERIENCE_BOTTLE).setName(LanguageApi.getMessage(player.getName(), "grieferprime.banksystem.currency.prime_orbs")).build());
        inventory.setItem(27, new ItemBuilder(Material.ENDER_EYE).setName(LanguageApi.getMessage(player.getName(), "inventory.back")).build());
    }
}
