package org.codetome.zircon.examples;

import org.codetome.zircon.Position;
import org.codetome.zircon.TextCharacter;
import org.codetome.zircon.TextColor;
import org.codetome.zircon.builder.TerminalBuilder;
import org.codetome.zircon.builder.TextCharacterBuilder;
import org.codetome.zircon.builder.TextColorFactory;
import org.codetome.zircon.graphics.layer.DefaultLayer;
import org.codetome.zircon.screen.Screen;
import org.codetome.zircon.terminal.Size;
import org.jetbrains.annotations.NotNull;

public class LayersExample {

    public static void main(String[] args) {
        // for this example we only need a default terminal (no extra config)
        final Screen screen = TerminalBuilder.newBuilder().buildScreen();
        screen.setCursorVisible(false); // we don't want the cursor right now

        final String firstRow = "This is white text on black";
        for (int x = 0; x < firstRow.length(); x++) {
            screen.setCharacter(
                    new Position(x + 1, 1),
                    buildWhiteOnBlack(firstRow.charAt(x)));
        }

        final String secondRow = "Like the row above but with blue overlay.";
        for (int x = 0; x < secondRow.length(); x++) {
            screen.setCharacter(
                    new Position(x + 1, 2),
                    buildWhiteOnBlack(secondRow.charAt(x)));
        }

        addOverlayAt(screen,
                new Position(1, 2),
                new Size(secondRow.length(), 1),
                TextColorFactory.fromRGB(50, 50, 200, 127));

        screen.display();
    }

    private static void addOverlayAt(Screen screen, Position offset, Size size, TextColor color) {
        screen.addOverlay(new DefaultLayer(
                size,
                TextCharacterBuilder.newBuilder()
                        .backgroundColor(color)
                        .character(' ')
                        .build(),
                offset
        ));
    }

    @NotNull
    private static TextCharacter buildWhiteOnBlack(char c) {
        return TextCharacterBuilder.newBuilder()
                .character(c)
                .backgroundColor(TextColorFactory.fromRGB(0, 0, 0, 255))
                .foregroundColor(TextColorFactory.fromRGB(255, 255, 255, 255))
                .build();
    }

}
