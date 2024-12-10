package com.peter.claims.permission;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public enum PermissionState {
    ALLOWED,
    PROHIBITED,
    DEFAULT;

    public Text getText() {
        MutableText text = Text.of(toString()).copy();
        switch (this) {
            case ALLOWED -> text.formatted(Formatting.GREEN);
            case PROHIBITED -> text.formatted(Formatting.RED);
            case DEFAULT -> text.formatted(Formatting.GRAY);
        }
        return text;
    }
}