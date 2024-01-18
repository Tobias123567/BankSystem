package de.tobi.bank.tutorial;

import de.tobi.api.languagesystem.LanguageApi;
import de.tobi.tutorialsystem.TutorialSystem;
import de.tobi.tutorialsystem.util.ProgressLevel;
import de.tobi.tutorialsystem.util.TutorialModule;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BankTutorial extends TutorialModule {

    @Nullable
    public static BankTutorial getCurrentModule(@NotNull Player player) {
        return TutorialSystem.getCurrentModule(player) instanceof BankTutorial tutorial ? tutorial : null;
    }

    public static void finishModule(@NotNull Player player) {
        TutorialSystem.finishModule(player);
    }

    public BankTutorial(@NotNull Player player) {
        super(player, ProgressLevel.HIGH, 3);
    }

    @Override
    public @NotNull String getName() {
        return LanguageApi.getMessage(this.player.getName(), "grieferprime.banksystem.tutorial.name");
    }

    @Override
    public @NotNull Material getShownItem() {
        return Material.GOLD_BLOCK;
    }

    @Override
    public @Nullable String getShownTexture() {
        return "textures/blocks/gold_block.png";
    }

    @Override
    public void startStep() {
        this.player.sendMessage(LanguageApi.getMessage(this.player.getName(), "grieferprime.banksystem.tutorial.step" + this.step));
    }

    @Override
    public void endStep() {

    }
}
