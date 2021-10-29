package com.swiswiswift.extractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) throws IOException {

        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/sample1.nes"));

        // 0-3: Constant $4E $45 $53 $1A ("NES" followed by MS-DOS end-of-file)

        // 4: Size of PRG ROM in 16 KB units
        int programROMUnit = bytes[4];
        System.out.println("PRG ROM Unit: " + programROMUnit);

        // 5: Size of CHR ROM in 8 KB units (Value 0 means the board uses CHR RAM)
        int characterROMUnit = bytes[5];
        System.out.println("CHR ROM Unit: " + characterROMUnit);

        int NES_HEADER_SIZE = 16;       // 0x0010
        int PROGRAM_ROM_SIZE = 16000;   // 0x4000;
        int CHARACTER_ROM_SIZE = 8000;  // 0x2000;

        int characterROMStart = NES_HEADER_SIZE + programROMUnit * PROGRAM_ROM_SIZE;
        int characterROMEnd = characterROMStart + characterROMUnit * CHARACTER_ROM_SIZE;

        byte[] characterROM = Arrays.copyOfRange(bytes, characterROMStart, characterROMEnd);

        int[][][] sprites = new int[characterROM.length / 16][8][8];

        for (int i = 0; i < characterROM.length / 16; i++) {

            byte[] spriteBinary = Arrays.copyOfRange(characterROM, i * 16, (i + 1) * 16);

            // sprite を初期化
            int[][] sprite = new int[8][8];
            for (int j = 0; j < 8; j++) {
                int[] row = new int[8];
                for (int k = 0; k < 8; k++) {
                    row[k] = 0;
                }
                sprite[j] = row;
            }

            for (int j = 0; j < 8; j++) {
                String fixedValueString = byteToFixedWidthInteger(spriteBinary[j]);
                for (int k = 0; k < 8; k++) {
                    if (fixedValueString.charAt(k) == '1') {
                        sprite[j][k] += 1;
                    }
                }
            }

            for (int j = 8; j < 16; j++) {
                String fixedValueString = byteToFixedWidthInteger(spriteBinary[j]);
                for (int k = 0; k < 8; k++) {
                    if (fixedValueString.charAt(k) == '1') {
                        sprite[j - 8][k] += 2;
                    }
                }
            }

            // sprites
            sprites[i] = sprite;
        }

        exportImage(sprites);
    }

    static String byteToFixedWidthInteger(byte value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    static void exportImage(int[][][] sprites) throws IOException {
        // Image Size.
        int width = 800;
        int height = 160;

        // Constructs a BufferedImage of one of the predefined image types.
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics which can be used to draw into the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();

        // fill all the image with white
        g2d.setColor(Color.white);
        g2d.fillRect(0, 0, width, height);

        // draw sprites
        for (int i = 0; i < 50; i++) {
            for (int j = 0; j < 10; j++) {
                int index = i * 10 + j;
                int row = index / 50;
                int column = index % 50;

                for (int k = 0; k < 8; k++) {
                    for (int l = 0; l < 8; l++) {
                        int value = sprites[index][k][l];
                        if (value == 0) {
                            g2d.setColor(Color.black);
                        } else if (value == 1) {
                            g2d.setColor(Color.gray);
                        } else if (value == 2) {
                            g2d.setColor(Color.lightGray);
                        } else if (value == 3) {
                            g2d.setColor(Color.white);
                        }
                        g2d.fillRect(column * 16 + l * 2, row * 16 + k * 2, 2, 2);
                    }
                }
            }
        }

        // Disposes of this graphics context and releases any system resources that it is using.
        g2d.dispose();

        // Save as PNG
        File file = new File("image.png");
        ImageIO.write(bufferedImage, "png", file);
    }
}
