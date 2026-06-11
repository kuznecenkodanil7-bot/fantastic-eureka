package ru.helena.chatreminder;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public final class ChatReminderText {
    private ChatReminderText() {
    }

    public static Text fromLegacyAmpersand(String input) {
        if (input == null || input.isEmpty()) {
            return Text.empty();
        }

        MutableText result = Text.empty();
        StringBuilder currentPart = new StringBuilder();
        Style currentStyle = Style.EMPTY;

        for (int i = 0; i < input.length(); i++) {
            char currentChar = input.charAt(i);

            if (currentChar == '&' && i + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(i + 1));
                Formatting formatting = Formatting.byCode(code);

                if (formatting != null) {
                    appendPart(result, currentPart, currentStyle);
                    currentPart.setLength(0);

                    if (formatting == Formatting.RESET) {
                        currentStyle = Style.EMPTY;
                    } else {
                        currentStyle = currentStyle.withFormatting(formatting);
                    }

                    i++;
                    continue;
                }
            }

            currentPart.append(currentChar);
        }

        appendPart(result, currentPart, currentStyle);
        return result;
    }

    private static void appendPart(MutableText result, StringBuilder part, Style style) {
        if (part.length() == 0) {
            return;
        }

        result.append(Text.literal(part.toString()).setStyle(style));
    }
}
