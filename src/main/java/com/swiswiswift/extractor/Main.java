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

        // バイナリファイルを読み込む
        byte[] bytes = Files.readAllBytes(Paths.get("src/main/resources/sample1.nes"));

        // NESファイルの0-3番目は $4E $45 $53 $1A と決まっている。("NES"+制御文字)
        System.out.println("0: " + Integer.toHexString(bytes[0]));
        System.out.println("1: " + Integer.toHexString(bytes[1]));
        System.out.println("2: " + Integer.toHexString(bytes[2]));
        System.out.println("3: " + Integer.toHexString(bytes[3]));

        // 4番目はプログラムROMのユニット数が入る。プログラムROMは1ユニット16KB。
        int programROMUnit = bytes[4];
        System.out.println("PRG ROM Unit: " + programROMUnit);

        // 5番目はキャラクターROMのユニット数が入る。キャラクターROMは1ユニット8KB。
        int characterROMUnit = bytes[5];
        System.out.println("CHR ROM Unit: " + characterROMUnit);

        // ヘッダーは16Byte, プログラムROMは1ユニット16KB, キャラクターROMは1ユニット8KB。
        int NES_HEADER_SIZE = 16;       // 0x0010
        int PROGRAM_ROM_UNIT_SIZE = 16000;   // 0x4000;
        int CHARACTER_ROM_UNIT_SIZE = 8000;  // 0x2000;

        // キャラクターROMのスタートと終わりのインデックスを取得。
        int characterROMStart = NES_HEADER_SIZE + programROMUnit * PROGRAM_ROM_UNIT_SIZE;
        int characterROMEnd = characterROMStart + characterROMUnit * CHARACTER_ROM_UNIT_SIZE;

        // バイナリからキャラクターROMを取り出す。
        byte[] characterROM = Arrays.copyOfRange(bytes, characterROMStart, characterROMEnd);

        // sprite用の配列を作成
        int[][][] sprites = new int[characterROM.length / 16][8][8];

        for (int i = 0; i < characterROM.length / 16; i++) {
            // 1sprite のデータを切り出す
            byte[] spriteBinary = Arrays.copyOfRange(characterROM, i * 16, (i + 1) * 16);

            // sprite を初期化
            int[][] sprite = new int[8][8];
            for (int j = 0; j < 8; j++) {
                sprite[j] = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
            }

            for (int j = 0; j < 8; j++) {
                String fixedValueString = byteToBinaryString(spriteBinary[j]);
                for (int k = 0; k < 8; k++) {
                    if (fixedValueString.charAt(k) == '1') {
                        sprite[j][k] += 1;
                    }
                }
            }

            for (int j = 8; j < 16; j++) {
                String fixedValueString = byteToBinaryString(spriteBinary[j]);
                for (int k = 0; k < 8; k++) {
                    if (fixedValueString.charAt(k) == '1') {
                        sprite[j - 8][k] += 2;
                    }
                }
            }

            // sprite を追加
            sprites[i] = sprite;
        }
        exportImage(sprites);
    }

    /**
     * byteを渡すと8文字のバイナリ文字列に変換する関数
     *
     * @param value ex. 8
     * @return ex. 00001001
     */
    static String byteToBinaryString(byte value) {
        return String.format("%8s", Integer.toBinaryString(value & 0xFF)).replace(' ', '0');
    }

    /**
     * モック用のSpriteの配列を返す
     * @return Stripe の配列
     */
    static int[][][] createMockStripes() {
        int[][][] sprites = new int[500][8][8];

        //　カビゴンのスプライト
        int[][] sprite = new int[8][8];
        sprite[0] = new int[]{3, 1, 1, 3, 3, 1, 1, 3};
        sprite[1] = new int[]{3, 1, 1, 1, 1, 1, 1, 3};
        sprite[2] = new int[]{1, 2, 2, 1, 1, 2, 2, 1};
        sprite[3] = new int[]{1, 2, 0, 2, 2, 0, 2, 1};
        sprite[4] = new int[]{1, 2, 2, 2, 2, 2, 2, 1};
        sprite[5] = new int[]{1, 2, 0, 0, 0, 0, 2, 1};
        sprite[6] = new int[]{1, 2, 2, 2, 2, 2, 2, 1};
        sprite[7] = new int[]{3, 1, 1, 1, 1, 1, 1, 3};

        // カビゴンのスプライトを500個詰める
        for (int i = 0; i < 500; i++) {
            sprites[i] = sprite;
        }
        return sprites;
    }

    /**
     * Sprite(int[][])の配列を渡すと画像にする関数。要素数は500である必要がある。
     *
     * @param sprites Sprite(int[][])の配列
     * @throws IOException Exception
     */
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
