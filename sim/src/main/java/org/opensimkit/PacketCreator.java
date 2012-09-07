/*
 * PacketCreator.java
 *
 * Created on 21. December 2008, 21:32
 *
 *  A class which creates OSK packets and sends them to the MMI.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2008-12-21
 *      File created - A. Brandt:
 *      Initial version.
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 */
package org.opensimkit;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.opensimkit.manipulation.Manipulator;
import org.opensimkit.oskpacket.OSKPacket;
import org.opensimkit.oskpacket.OSKPacketProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class which creates OSK packets and sends them to the MMI.
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.2
 * @since 2.5.1
 */
@ApplicationScoped
public class PacketCreator {
	//@Inject    
    private static Logger LOG= LoggerFactory.getLogger(PacketCreator.class);
    //private final OutputStream outputStream;
    @Inject Manipulator manipulator;
    @Inject ComHandler  comHandler;
    
    // TODO: use injection
    private final OSKPacketProvider oSKPacketProvider
            = OSKPacketProvider.createTMProvider((byte) 0x0);
    
    private final OSKPacketProvider oSKPacketProvider2
            = OSKPacketProvider.createTMProvider((byte) 0x1);
    private final ByteBuffer buffer
            = ByteBuffer.allocate(OSKPacket.MAX_DATA_SIZE);
    private static final DateFormat dateFormat = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.SHORT, Locale.ENGLISH);

  //  static {
      //  this.outputStream = kernel.getOutputWriter();
        // this.comHandler   = kernel.getComHandler();
        // this.// manipulator = kernel.getManipulator();
//        try {
//            Date aSimTime = dateFormat.parse("01/06/2010 01:00 AM");
//             
//        } catch (ParseException ex) {
//            LOG.error("Exception: ", ex);
//        }
//    }

    public void sendData() throws IOException {
        buffer.clear();
        /* The model names are hardcoded here. This is only for demonstration.
         */
        Model model0  = comHandler.getItemKey("00_HPBottle");
        Model model8  = comHandler.getItemKey("08_PReg");
        Model model12 = comHandler.getItemKey("12_PReg");
        Model model15 = comHandler.getItemKey("15_PReg");
        Model model17 = comHandler.getItemKey("17_Tank");

        try {
            buffer.putDouble(0, manipulator.getDouble(model0, "ptotal"));
            buffer.putDouble(4, manipulator.getDouble(model8, "tout"));
            buffer.putDouble(8, manipulator.getDouble(model12, "tout"));
            buffer.putDouble(12, manipulator.getDouble(model15, "tout"));
            buffer.putDouble(16, manipulator.getDouble(model17, "tinFPG"));
            buffer.putDouble(20, manipulator.getDouble(model17, "tinOPG"));
            buffer.putDouble(24, manipulator.getDouble(model17, "poxt"));
            buffer.putDouble(28, manipulator.getDouble(model17, "tGOxT"));
            buffer.putDouble(32, manipulator.getDouble(model17, "tLOxT"));
            buffer.putDouble(36, manipulator.getDouble(model17, "PFuT"));
            buffer.putDouble(40, manipulator.getDouble(model17, "tGFuT"));
            buffer.putDouble(44, manipulator.getDouble(model17, "tLFuT"));
            
            Date simTime = dateFormat.parse("01/06/2010 01:00 AM");

            OSKPacket packet = oSKPacketProvider.sendNext(buffer.array(),
                    simTime.getTime(), System.currentTimeMillis());
            //outputStream.write(packet.array());
            //log.info("length: " + packet.array().length);
            /*
            <logVariable cid="c0"  variable="ptotal"/>
            <logVariable cid="c8"  variable="tout"/>
            <logVariable cid="c12" variable="tout"/>
            <logVariable cid="c15" variable="tout"/>
            <logVariable cid="c17" variable="tin0"/>
            <logVariable cid="c17" variable="tin1"/>
            <logVariable cid="c17" variable="poxt"/>
            <logVariable cid="c17" variable="tGOxT"/>
            <logVariable cid="c17" variable="tLOxT"/>
            <logVariable cid="c17" variable="PFuT"/>
            <logVariable cid="c17" variable="tGFuT"/>
            <logVariable cid="c17" variable="tLFuT"/>
             * */

            buffer.clear();
            buffer.putDouble(0, manipulator.getDouble(model0, "mass"));
            packet = oSKPacketProvider2.sendNext(buffer.array(),
                    simTime.getTime(), System.currentTimeMillis());
            //outputStream.write(packet.array());

        } catch (IllegalAccessException ex) {
            LOG.error("Exception: ", ex);
        } catch (ClassNotFoundException ex) {
            LOG.error("Exception: ", ex);
        } catch (NoSuchFieldException ex) {
            LOG.error("Exception: ", ex);
        } catch (NullPointerException ex) {
            //LOG.error("Exception: ", ex);
        } catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

                /*
                     <logVariable cid="c0"  variable="ptotal"/>
    <logVariable cid="c8"  variable="tout"/>
    <logVariable cid="c12" variable="tout"/>
    <logVariable cid="c15" variable="tout"/>
    <logVariable cid="c17" variable="tin0"/>
    <logVariable cid="c17" variable="tin1"/>
    <logVariable cid="c17" variable="poxt"/>
    <logVariable cid="c17" variable="tGOxT"/>
    <logVariable cid="c17" variable="tLOxT"/>
    <logVariable cid="c17" variable="PFuT"/>
    <logVariable cid="c17" variable="tGFuT"/>
    <logVariable cid="c17" variable="tLFuT"/>
                 * */

    }

}
