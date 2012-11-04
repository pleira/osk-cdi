/*
 * TabGenerator.java
 *
 * Created on 3. Juli 2007, 22:25
 *
 *  This is the implementation of a table result generator.
 *
 *-----------------------------------------------------------------------------
 *  Modification History
 *
 *  2004-12-05
 *      File created  J. Eickhoff:
 *
 *      Class architecture is a derivative from ObjectSim 2.0.3.,
 *      a simulation program published in:
 *
 *        Eickhoff, J.:
 *        Modulare Programmarchitektur fuer ein wissensbasiertes
 *        Simulationssystem mit erweiterter Anwendbarkeit in der
 *        Entwicklung und Betriebsberwachung verfahrenstechnischer
 *        Anlagen.
 *        PhD thesis in Department Process Engineering of
 *        TU Hamburg-Harburg, 1996.
 *
 *      See also file history cited there and see historic relation of
 *      this OpenSimKit class to a.m. ObjectSim explained in
 *      OpenSimKit Documentation.
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 *
 *  2005-09
 *      OpenSimKit V 2.2
 *      Modifications entered for XML input file parsing by
 *      Peter Heinrich  peterhe@student.ethz.ch
 *
 *  2006-03
 *      OpenSimKit V 2.3
 *      Modifications entered for I/O file handling.
 *      init() method argument list changed.
 *      J. Eickhoff
 *
 *  2008-05
 *      OpenSimKit V 2.4
 *      Ported from C++ to Java
 *      A. Brandt  alexander.brandt@gmail.com
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *  2011-01
 *     Fixed parsing typo in inputfile: delimeter -> delimiter.
 *     J. Eickhoff
 *
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL  see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by authors.
 *
 *
 *-----------------------------------------------------------------------------
 */
package org.opensimkit;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.weld.environment.se.beans.ParametersFactory;
import org.opensimkit.manipulation.ManipulationException;
import org.opensimkit.manipulation.Manipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the implementation of a table result generator.
 *
 * @author J. Eickhoff
 * @author A. Brandt
 * @author T. Pieper
 * @version 1.3
 * @since 2.4.0
 */
@ApplicationScoped
public class TabGenerator {
    private static final Logger LOG
            = LoggerFactory.getLogger(TabGenerator.class);
    private final List<TabgeneratorEntry> entries
            = new LinkedList<TabgeneratorEntry>();
//    private final Kernel   kernel;
    @Inject Manipulator manipulator;
    private double         start;
    private double         end;
    private int            factor;
    private int            stepcount;
    private int            flushcount;
    private String         name = "tabGenerator";
    private FileWriter     table;
    private int            localNAckFlag;
    private long           startTime;
    private long           endTime;
    private boolean        headerIsAligned;
    private String         delimiter;

    @Inject     ParametersFactory pF;

    /**
     * Constructor of the TabGenerator.
     *
     * @param kernel    Refernce to the kernel which instances the
     *                  TabGenerator.
     * @throws java.io.IOException
     */
    @Inject
    public TabGenerator()  {
        LOG.debug("{}:\t{}", this.name, "Constructor");

        start  = 0.;
        end    = 999.;
        factor = 1;

//        this.kernel = kernel;
        // manipulator = kernel.getManipulator();
    }

    /**
     * Initialize & set table output stream.
     *
     * @param outTabStream  Reference to the FileWriter.
     * @return  Error code
     * @throws IOException 
     */
    public int init(final FileWriter outTabStream) throws IOException {
        LOG.debug("{}:\t{}", this.name, "init");

        table = outTabStream;
        return 0;
    }

    public void setStart(final double start) {
        if (start < 0.) {
            this.start = 0.;
        } else {
            this.start = start;
        }
    }

    public void setEnd(final double end) {
        if (end <= start) {
            this.end = start + 1.;
        } else {
            this.end = end;
        }
    }

    public void setFactor(final int newFactor) {
        if (newFactor <= 0) {
            factor = 1;
        } else {
            factor = newFactor;
        }
    }

    public void setHeaderAlignment(final boolean isAligned) {
        headerIsAligned = isAligned;
    }

    public void setDelimiter(final String delimiter) {
        if (delimiter != null) {
            this.delimiter = delimiter;
        } else {
            this.delimiter = "";
        }
    }

    public void addVariable(final String modelName, final String variableName,
            final String header, final String format) {
        Object instance;
        Object testResult;

        //System.out.println("Models and Vars added to table: " + modelName + " " + variableName);

        instance = manipulator.getInstance(modelName);

        if (instance != null) {
            testResult = null;

            try {
                testResult = manipulator.getAsString(instance, variableName);
            } catch (IllegalAccessException ex) {
                LOG.error("Exception:", ex);
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception:", ex);
            } catch (NoSuchFieldException ex) {
                LOG.error("Exception:", ex);
            }

            if (testResult != null) {
                TabgeneratorEntry tabGenEntry = new TabgeneratorEntry(instance,
                        modelName, variableName, header, format, 0);
                entries.add(tabGenEntry);

               LOG.debug("Added slot '{}' from model '{}' to the result table.",
                       variableName, modelName);
            }

        } else {
            LOG.info("Invalid model in "
                    + "/Output-Table/Parameter of TabGen : {}", modelName);
                    localNAckFlag = 1;
                    SimHeaders.negativeAckFlag = 1;
        }
    }

    /**
     * Initializing the step counter with 1.
     */
    public void initStepcounter() {
        stepcount = 1;
    }

    private void writeDelimiter() throws IOException {
        /* Java masks the tabulator when reading the string from the text file
         there second backslash is needed in the equals method. */
        if (delimiter.equals("\\t") == true) {
            table.write("\t");
        } else {
            table.write(delimiter);
        }
    }

    private void writeHeader() throws IOException {
        Iterator<?>       iterator = entries.iterator();
        Object            value = null;
        String            header;
        String            data;
        int               difference;
        TabgeneratorEntry entry;

        while (iterator.hasNext()) {
            entry = (TabgeneratorEntry) iterator.next();

            if (headerIsAligned) {
                try {
                    value = manipulator.get(entry.getInstance(),
                            entry.getVariable());
                } catch (IllegalAccessException ex) {
                    LOG.error("Exception:", ex);
                } catch (ClassNotFoundException ex) {
                    LOG.error("Exception:", ex);
                } catch (NoSuchFieldException ex) {
                    LOG.error("Exception:", ex);
                } catch (ManipulationException ex) {
                    LOG.error("Exception:", ex);
                }
                data = String.format(Locale.ENGLISH, entry.getFormat(),
                        value);
                header = entry.getHeader();

                difference = header.length() - data.length();

                if (difference == 0) {
                    /* Do nothing. */
                } else if (difference > 0) {
                    entry.setAlignmentSpaces(difference);
                } else if (difference < 0) {
                    writeAlignment(Math.abs(difference));
                }
            }

            table.write(entry.getHeader());

            if (iterator.hasNext() == true) {
                writeDelimiter();
            }
        }
    }

    private void writeAlignment(final int numberOfTimes) throws IOException {
        for (int i = 0; i < numberOfTimes; i++) {
                    table.write(" ");
                }
    }

    /**
     * Write output *table header line.
     * @param kernel 
     *
     * @throws java.io.IOException
     */
    public void tabInit() throws IOException {
        startTime = System.currentTimeMillis();

        LOG.debug("{}:\t{}", this.name, "tabInit");
        table       = new FileWriter(SimHeaders.myOutFileName);
        
        // Writing the headline information read in from the input file
        table.write("OpenSimKit output file created by " + Kernel.OSK_NAME
            + " Version " + Kernel.OSK_VERSION + SimHeaders.NEWLINE);
         table.write("Start time: "
                        + String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
                        startTime) + SimHeaders.NEWLINE);
//        table.write("System: " + kernel.getSysDescr() + SimHeaders.NEWLINE);
//        table.write("Case:   " + kernel.getCaseDescr() + SimHeaders.NEWLINE);
//        table.write("Note:   " + kernel.getNoteDescr() + SimHeaders.NEWLINE);
        table.write(SimHeaders.NEWLINE);

        table.write("Result-table:" + SimHeaders.NEWLINE);

        writeHeader();
        table.write(SimHeaders.NEWLINE);
        table.flush();
    }

    /**
     * Determine whether output timestep for *table writing is reached.
     *
     * @param time  Time at the current interval
     * @param tStepSize
     * @return  error code
     * @throws java.io.IOException
     */
    public int tabIntervalWrite(final double time, final double tStepSize)
        throws IOException {
        int         retval;

        LOG.debug("{}:\t{}", this.name, "tabIntervalWrite");

        flushcount = 0; //JH

        if (stepcount == factor) {
            tabWrite(time, tStepSize);
            stepcount = 0;
            retval = 1;
            //
            // After each 10th writeout the output stream is flushed to avoid
            // exceeding memory requirements for the whole program.
            //
            if (flushcount == 10) {
                table.flush();
                flushcount = 0;
            }
            flushcount += 1;
        } else {
            retval = 0;
        }
        stepcount += 1;
        return retval;
    }

    /**
     * Write output *table line.
     *
     * @param   time
     * @param   tStepSize
     * @throws  java.io.IOException
     */
    public void tabWrite(final double time, final double tStepSize)
        throws IOException {

        Iterator<?>       iterator = entries.iterator();
        Object            value    = null;
        TabgeneratorEntry entry;

        LOG.debug("{}:\t{}", this.name, "tabWrite");

        while (iterator.hasNext()) {
            entry = (TabgeneratorEntry) iterator.next();
            try {
                value = manipulator.get(entry.getInstance(),
                        entry.getVariable());
            } catch (IllegalAccessException ex) {
                LOG.error("Exception:", ex);
            } catch (ClassNotFoundException ex) {
                LOG.error("Exception:", ex);
            } catch (NoSuchFieldException ex) {
                LOG.error("Exception:", ex);
            } catch (ManipulationException ex) {
                LOG.error("Exception:", ex);
            }
            if (headerIsAligned) {
                writeAlignment(entry.getAlignmentSpaces());
            }
            table.write(String.format(Locale.ENGLISH, entry.getFormat(),
                    value));
            if (iterator.hasNext() == true) {
                writeDelimiter();
            }
        }
        table.write(SimHeaders.NEWLINE);
        table.flush();
    }

    /**
     * Write last output *table line.
     *
     * @param time
     * @param tStepSize
     * @return  error code
     * @throws java.io.IOException
     */
    public int tabEndWrite(final double time, final double tStepSize)
        throws IOException {
        Iterator<?> iterator = entries.iterator();
        String      value    = null;

        LOG.debug("{}:\t{}", this.name, "tabEndWrite");

        tabWrite(time, tStepSize);

        endTime = System.currentTimeMillis();
        table.write(SimHeaders.NEWLINE);
        table.write("End time: "
                        + String.format("%1$tFT%1$tH:%1$tM:%1$tS.%1$tL",
                        endTime) + SimHeaders.NEWLINE);
        table.write("Simulation duration: "
                + ((endTime - startTime)/ 86400000) + "d " //days
                + ((endTime - startTime)/ 3600000) + "h " //hours
                + ((endTime - startTime)/ 60000) + "m " //minutes
                + ((endTime - startTime)/ 1000) +  "s " //seconds
                + (endTime - startTime) + "mu "//microseconds
                + SimHeaders.NEWLINE);


        table.flush();
        table.close();
        return 0;
    }

}
