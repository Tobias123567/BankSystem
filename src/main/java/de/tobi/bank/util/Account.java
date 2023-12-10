package de.tobi.bank.util;

import de.tobi.api.money.util.Currency;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record Account(int id, @NotNull String name, @NotNull UUID uuid, @NotNull Currency currency, int amount) {
}
