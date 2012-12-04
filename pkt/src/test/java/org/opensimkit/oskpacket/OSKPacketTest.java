/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.osk.oskpacket;

import java.nio.ByteBuffer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author OSK-J Team
 */
public final class OSKPacketTest {
    private OSKPacket emptyTMPacket;
    private OSKPacket emptyTCPacket;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        byte apid = 0;
        short ssc = 0;
        long simulatedTime = 0;
        long systemTime = 0;
        byte[] emptyData = new byte[0];
        emptyTMPacket = OSKPacket.createTMPacket(apid, ssc, simulatedTime,
                systemTime, emptyData);
         emptyTCPacket = OSKPacket.createTCPacket(apid, ssc, simulatedTime,
                systemTime, emptyData);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isTMPacket method, of class OSKPacket.
     */
    @Test
    public void testIsTMPacket() {
        System.out.println("isTMPacket");

        /* We create a TM packet. */
        OSKPacket instance = emptyTMPacket;
        boolean expResult = true;
        boolean result = instance.isTMPacket();
        Assert.assertEquals(expResult, result);

        /* We create a TC packet. */
        instance = emptyTCPacket;
        expResult = false;
        result = instance.isTMPacket();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of isTCPacket method, of class OSKPacket.
     */
    @Test
    public void testIsTCPacket() {
        System.out.println("isTCPacket");

        /* We create a TC packet. */
        OSKPacket instance = emptyTCPacket;
        boolean expResult = true;
        boolean result = instance.isTCPacket();
        Assert.assertEquals(expResult, result);

        /* We create a TM packet. */
        instance = emptyTMPacket;
        expResult = false;
        result = instance.isTCPacket();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getType method, of class OSKPacket.
     */
    @Test
    public void testGetType() {
        System.out.println("getType");

        OSKPacket instance = emptyTMPacket;
        byte expResult = OSKPacket.TMPACKET;
        byte result = instance.getType();
        Assert.assertEquals(expResult, result);

        instance = emptyTCPacket;
        expResult = OSKPacket.TCPACKET;
        result = instance.getType();
        Assert.assertEquals(expResult, result);

        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
            byte type = b;
            byte apid = 0x0;
            short ssc = 0;
            int simulatedTime = 0;
            int systemTime = 0;
            byte[] emptyData = new byte[0];

            instance = OSKPacket.createRawPacket(type, apid, ssc,
                    simulatedTime, systemTime, emptyData);
            byte expResult1 = b;
            byte result1 = instance.getType();
            Assert.assertEquals(expResult1, result1);
            byte expResult2 = 0x0;
            byte result2 = instance.getApplicationID();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of getAPID method, of class OSKPacket.
     */
    @Test
    public void testGetAPID() {
        System.out.println("getAPID");

        for (byte b = Byte.MIN_VALUE; b < Byte.MAX_VALUE; b++) {
            byte apid = b;
            short ssc = 0;
            int simulatedTime = 0;
            int systemTime = 0;
            byte[] emptyData = new byte[0];

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);
            /* Type must be 0. */
            byte expResult1 = 0;
            byte result1 = instance.getType();
            Assert.assertEquals(expResult1, result1);
            /* APID varies. */
            byte expResult = b;
            byte result = instance.getApplicationID();
            Assert.assertEquals(expResult, result);
            /* SSC must be 0. */
            short expResult2 = 0;
            short result2 = instance.getSequenceCount();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of getSequenceCount method, of class OSKPacket.
     */
    @Test
    public void testGetSequenceCount() {
        System.out.println("getSequenceCount");

        for (short s = Short.MIN_VALUE; s < Short.MAX_VALUE; s++) {
            byte apid = 0;
            short ssc = s;
            int simulatedTime = 0;
            int systemTime = 0;
            byte[] emptyData = new byte[0];

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);
            /* APID must be 0. */
            byte expResult1 = 0;
            byte result1 = instance.getApplicationID();
            Assert.assertEquals(expResult1, result1);
            /* SSC varies. */
            short expResult = s;
            short result = instance.getSequenceCount();
            Assert.assertEquals(expResult, result);
            /* Length must be 0. */
            int expResult2 = OSKPacket.HEADER_SIZE + 0 + OSKPacket.FOOTER_SIZE;
            int result2 = instance.getLength();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of getLength method, of class OSKPacket.
     */
    @Test
    public void testGetLength() {
        System.out.println("getLength");

        for (short i = 0; i < OSKPacket.MAX_DATA_SIZE; i++) {
            byte apid = 0;
            short ssc = 0;
            int simulatedTime = 0;
            int systemTime = 0;
            byte[] emptyData = new byte[i];

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);
//            System.out.println(instance.toString());
            /* SSC must be 0. */
            short expResult1 = 0;
            short result1 = instance.getSequenceCount();
            Assert.assertEquals(expResult1, result1);
            Assert.assertEquals(ssc, result1);
            /* Length varies. */
            int expResult = OSKPacket.HEADER_SIZE + i + OSKPacket.FOOTER_SIZE;
            int result = instance.getLength();
            Assert.assertEquals(expResult, result);
            /* SimulatedTime must be 0. */
            long expResult2 = 0;
            long result2 = instance.getSimulatedTime();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of getSimulatedTime method, of class OSKPacket.
     */
    @Test
    public void testGetSimulatedTime() {
        System.out.println("getSimulatedTime");

        for (int i = 0; i < Integer.MIN_VALUE; i++) {
            byte apid = 0;
            short ssc = 0;
            long simulatedTime = i;
            long systemTime = 0;
            byte[] emptyData = new byte[0];

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);
            /* Length must be OSKPacket.HEADER_SIZE + OSKPacket.FOOTER_SIZE. */
            int expResult1
                    = OSKPacket.HEADER_SIZE + 0 + OSKPacket.FOOTER_SIZE;
            int result1 = instance.getLength();
            Assert.assertEquals(expResult1, result1);
            /* SimulatedTime varies. */
            long expResult = i;
            long result = instance.getSimulatedTime();
            Assert.assertEquals(expResult, result);
            /* SystemTime must be 0. */
            long expResult2 = 0;
            long result2 = instance.getSystemTime();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of getSystemTime method, of class OSKPacket.
     */
    @Test
    public void testGetSystemTime() {
        System.out.println("getSystemTime");

        for (int i = 0; i < Integer.MIN_VALUE; i++) {
            byte apid = 0;
            short ssc = 0;
            long simulatedTime = 0;
            long systemTime = i;
            byte[] emptyData = new byte[0];

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);
            /* SimulatedTime must be 0. */
            long expResult1 = 0;
            long result1 = instance.getSimulatedTime();
            Assert.assertEquals(expResult1, result1);
            /* SystemTime varies. */
            long expResult = i;
            long result = instance.getSystemTime();
            Assert.assertEquals(expResult, result);
            /* Data must be 0. */
            int expResult2 = 0;
            int result2 = instance.getData().capacity();
            Assert.assertEquals(expResult2, result2);
        }
    }

    /**
     * Test of wrap(byte[]) method, of class OSKPacket.
     */
    @Test
    public void testWrapByte() {
        System.out.println("wrap(byte[])");

        for (int i = 0; i < 10; i++) {
            byte apid = 0;
            short ssc = 0;
            long simulatedTime = 0;
            long systemTime = i;
            byte[] emptyData = new byte[0];

            byte[] packet1;

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);

            //System.out.println(instance.toString());
            packet1 = instance.array();

            /* The wrapped packet has to be the same as the original. */
            OSKPacket wrappedInstance = OSKPacket.wrap(packet1);
            //System.out.println(wrappedInstance.toHexString());
            Assert.assertEquals(instance, wrappedInstance);
        }
    }

    /**
     * Test of wrap(ByteBuffer) method, of class OSKPacket.
     */
    @Test
    public void testWrapByteBuffer() {
        System.out.println("wrap(ByteBuffer)");

        for (int i = 0; i < 10; i++) {
            byte apid = 0;
            short ssc = 0;
            long simulatedTime = 0;
            long systemTime = i;
            byte[] emptyData = new byte[0];

            ByteBuffer packet1;

            OSKPacket instance = OSKPacket.createTMPacket(apid, ssc,
                    simulatedTime, systemTime, emptyData);

            //System.out.println(instance.toString());
            packet1 = instance.getRaw();

            /* The wrapped packet has to be the same as the original. */
            OSKPacket wrappedInstance = OSKPacket.wrap(packet1);
            //System.out.println(wrappedInstance.toHexString());
            Assert.assertEquals(instance, wrappedInstance);
        }
    }

    /**
     * Test of getData method, of class OSKPacket.
     */
//    @Test
//    public void testGetData() {
//        System.out.println("getData");
//
//        int offsetData = 22;
//        int offsetLength = 4;
//        byte packetType = 0;
//        byte apid = 0;
//        short ssc = 0;
//        int simulatedTime = 0;
//        int systemTime = 0;
//
//        for (int i = 0; i < OSKPacket.MAX_DATA_SIZE; i++) {
//            byte[] data = new byte[i];
//            ByteBuffer rawData = ByteBuffer.allocate(OSKPacket.MAX_PACKET_SIZE);
//
////            for (int j = 0; j < data.length; j++) {
////                data[j] = 0xF;
////            }
//
//            for (int j = 0; j < data.length; j++) {
//                data[j] = 0xF;
//                rawData.put(offsetData + j, data[j]);
//            }
//
//            /* Write the data length before checksum calculation. */
//            short length = (short) (OSKPacket.HEADER_SIZE + data.length
//                    + OSKPacket.FOOTER_SIZE);
//            rawData.putShort(offsetLength, length);
//
//            /* Calculate CRC. */
//            short checksum = CRC16CCITT.getValue(rawData.array(), 0,
//                    OSKPacket.HEADER_SIZE + data.length);
//
//            OSKPacket instance = OSKPacket.createRawPacket(packetType, apid,
//                    ssc, simulatedTime, systemTime, data);
//            //System.out.println(instance.toString());
//            /* SystemTime must be 0. */
//            long expResult = 0;
//            long result = instance.getSystemTime();
//            Assert.assertEquals(expResult, result);
//            /* Data varies. */
//            byte[] expResult1 = data;
//            byte[] result1 = instance.getData().array();
//            Assert.assertArrayEquals(expResult1, result1);
//            /* Checksum must be like thee calculated one. */
//            short expResult2 = checksum;
//            short result2 = instance.getChecksum();
//            Assert.assertEquals(expResult2, result2);
//        }
//    }


    @Test
    public void testGetTMPacket1() {
        System.out.println("getTMPacket1");
        byte type = 0xF;
        byte apid = 0x0;
        short ssc = 0;
        int simulatedTime = 0;
        int systemTime = 0;
        byte[] emptyData = new byte[0];

        OSKPacket instance = OSKPacket.createRawPacket(type, apid, ssc,
                simulatedTime, systemTime, emptyData);
        byte expResult1 = 0xF;
        byte result1 = instance.getType();
        Assert.assertEquals(expResult1, result1);
        byte expResult2 = 0x0;
        byte result2 = instance.getApplicationID();
        Assert.assertEquals(expResult2, result2);
    }

    @Test
    public void testGetTMPacket2() {
        System.out.println("getTMPacket2");
        byte type = 0;
        byte apid = 0xF;
        short ssc = 0x0;
        int simulatedTime = 0;
        int systemTime = 0;
        byte[] emptyData = new byte[0];

        OSKPacket instance = OSKPacket.createRawPacket(type, apid, ssc,
                simulatedTime, systemTime, emptyData);
        byte expResult1 = 0xF;
        byte result1 = instance.getApplicationID();
        Assert.assertEquals(expResult1, result1);
        short expResult2 =  0x0;
        short result2 = instance.getSequenceCount();
        Assert.assertEquals(expResult2, result2);
    }

    @Test
    public void testGetTMPacket3() {
        System.out.println("getTMPacket3");
        byte type = 0;
        byte apid = 0;
        short ssc = 0xFF;
        int simulatedTime = 0;
        int systemTime = 0;
        byte[] emptyData = new byte[0];

        OSKPacket instance = OSKPacket.createRawPacket(type, apid, ssc,
                simulatedTime, systemTime, emptyData);
        short expResult1 = 0xFF;
        short result1 = instance.getSequenceCount();
        Assert.assertEquals(expResult1, result1);
        int expResult2 = OSKPacket.HEADER_SIZE + OSKPacket.FOOTER_SIZE;
        int result2 = instance.getLength();
        Assert.assertEquals(expResult2, result2);
    }
}