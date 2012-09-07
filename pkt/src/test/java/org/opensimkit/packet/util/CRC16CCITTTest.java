/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.opensimkit.packet.util;

import org.opensimkit.oskpacket.CRC16CCITT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author OSK-J Team
 */
public final class CRC16CCITTTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of crc16CCITT method, of class CRC16CCITT.
     */
    @Test
    public void testCrc16CcittTestcase1() {
        System.out.println("crc16CCITT Testcase 1 (00 00)");

        /* The input data 00 00 shall give 1D0F. */
        byte[] data = {0x00, 0x00};
        int offset = 0;
        int length = 2;
        byte[] expResult = {0x1D, 0x0F};
        byte[] result = CRC16CCITT.crc16CCITT(data, offset, length);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of crc16CCITT method, of class CRC16CCITT.
     */
    @Test
    public void testCrc16CcittTestcase2() {
        System.out.println("crc16CCITT Testcase 2 (00 00 00)");

        /* The input data 00 00 00 shall give CC9C. */
        byte[] data = {0x00, 0x00, 0x00};
        int offset = 0;
        int length = 3;
        byte[] expResult = {(byte) 0xCC, (byte) 0x9C};

        byte[] result = CRC16CCITT.crc16CCITT(data, offset, length);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of crc16CCITT method, of class CRC16CCITT.
     */
    @Test
    public void testCrc16CcittTestcase3() {
        System.out.println("crc16CCITT Testcase 3 (AB CD EF 01)");

        /* The input data 00 00 00 shall give 04A2. */
        byte[] data = {(byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x01};
        int offset = 0;
        int length = 4;
        byte[] expResult = {(byte) 0x04, (byte) 0xA2};

        byte[] result = CRC16CCITT.crc16CCITT(data, offset, length);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of crc16CCITT method, of class CRC16CCITT.
     */
    @Test
    public void testCrc16CcittTestcase4() {
        System.out.println("crc16CCITT Testcase 4 (14 56 F8 9A 00 01)");

        /* The input data 00 00 00 shall give 04A2. */
        byte[] data = {0x14, (byte) 0x56, (byte) 0xF8, (byte) 0x9A, 0x00, 0x01};
        int offset = 0;
        int length = 6;
        byte[] expResult = {(byte) 0x7F, (byte) 0xD5};

        short result = CRC16CCITT.getValue(data, offset, length);
       byte[] result2 = CRC16CCITT.convertIntToByteArray2(result);

        assertArrayEquals(expResult, result2);
    }

}