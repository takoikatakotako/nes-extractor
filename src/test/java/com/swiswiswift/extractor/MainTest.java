package com.swiswiswift.extractor;


import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MainTest {
    @Test
    public void testByteToBinaryString() throws Exception {
        assertEquals("00000000", Main.byteToBinaryString((byte) 0));
        assertEquals("00000010", Main.byteToBinaryString((byte) 2));
        assertEquals("00000100", Main.byteToBinaryString((byte) 4));
        assertEquals("00001000", Main.byteToBinaryString((byte) 8));
        assertEquals("00001001", Main.byteToBinaryString((byte) 9));
        assertEquals("11111111", Main.byteToBinaryString((byte) 255));
        assertEquals("01100110", Main.byteToBinaryString((byte) 0x66));
    }
}
