/*
 * TimeHandler.java
 *
 * Created on 21. February 2009
 *
 * Central manager of the time of the simulation.
 *
 *-----------------------------------------------------------------------------
 * Modification History:
 *
 *  2009-02-21
 *      File created - A. Brandt:
 *      Initial version.
 *
 *
 *  2009-06
 *     Integrated logging support.
 *     T. Pieper
 *
 *
 *  2010-03
 *     Integrated date export in Celestia compatible time format.
 *     J. Eickhoff
 *
 *-----------------------------------------------------------------------------
 *
 *      File under GPL - see OpenSimKit Documentation.
 *
 *      No warranty and liability for correctness by author.
 *
 */

package org.opensimkit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.opensimkit.config.NumberConfig;
import org.opensimkit.manipulation.Manipulatable;
import org.opensimkit.manipulation.Readable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central manager of the simulation time.
 *
 * A single time is incapable of representing a point on the timeline (there
 * is always a date needed to make a time "unique"). E.g. 12:00:00 is not
 * unique, however 2009-01-10T12:00:00Z is (it includes the date). Thus in the
 * following when the word "time" is mentioned it shall always mean "date and
 * time". Because for simulation purposes we need exact times.
 *
 * We have different times inside the simulator:
 * <li>System time representing the time of computer on which the simulator is
 * executed. This time can include time zone information (it can be a local
 * time).</li>
 * <li>Simulation time representing the time inside the simulation, e.g. the
 * date on which the satellite shall be simulated. This time shall always be
 * represented in UTC.</li>
 * <li>Simulator ticks representing the seconds from the start of the
 * simulator. It has no date associated.</li>
 *
 * @author A. Brandt
 * @author T. Pieper
 * @author J. Eickhoff
 * @version 1.2
 * @since 2.5.1
 */

@ApplicationScoped
public class TimeHandler {
    private static final int TIME_SYSTEM = 0;
    private static final int TIME_SIMULATION = 1;
    private static final int TIME_STEPS = 2;
    private static final Logger LOG
            = LoggerFactory.getLogger(TimeHandler.class);

    private final String name = "timeHandler";
    private final long[] time = new long[3];
    private DateFormat formatCel;

     private String simulatedMissionTimeString; // = "2010-03-01T22:55:00.000+0000";
     private int interval;
     private int stepSize;

     private long simulatedMissionTime;
     private long systemTime;
     private long integrationSteps;

//    public TimeHandler(final String name) {
//        this.name = name;
//    }

    @PostConstruct
    public void init() {
        // 1985-04-12T23:20:50.100Z
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        format.setLenient(true);
        formatCel = new SimpleDateFormat("yyyy, MM, dd, HH, mm, ss");
        formatCel.setLenient(true);

        time[TIME_SYSTEM] = 0;
        time[TIME_STEPS] = 0;
        time[TIME_SIMULATION] = 0;
        try {
           time[TIME_SIMULATION]
                   = format.parse(simulatedMissionTimeString).getTime();
        } catch (ParseException ex) {
            LOG.error("Exception:", ex);
        }
    }

    public long getSystemTime() {
        return time[TIME_SYSTEM];
    }

    public long getSimulatedMissionTime() {
        return time[TIME_SIMULATION];
    }

    public String getCelestiaUTCJulian2000() {
        return formatCel.format(time[TIME_SIMULATION]);
    }
    
    public double getSimulatedMissionTimeAsDouble() {
        return time[TIME_SIMULATION] / 1000.;
    }

    public long getSimulatorTicks() {
        return time[TIME_STEPS];
    }

    public long getInterval() {
        return interval;
    }

    public long getStepSize() {
        return stepSize;
    }

    public double getStepSizeAsDouble() {
        return stepSize / 1000.;
    }

    public void setSystemTime(final long systemTime) {
        time[TIME_SYSTEM] = systemTime;
    }

    public void update() {
        time[TIME_STEPS]++;
        time[TIME_SIMULATION] += stepSize;
        time[TIME_SYSTEM] += interval;

        integrationSteps     = time[TIME_STEPS];
        simulatedMissionTime = time[TIME_SIMULATION];
        systemTime           = time[TIME_SYSTEM];
    }

    public String getName() {
        return name;
    }

	public String getSimulatedMissionTimeString() {
		return simulatedMissionTimeString;
	}

	@Inject
	public void setSimulatedMissionTimeString(
			@ConfigProperty(name = "time.simulatedMissionTimeString") String simulatedMissionTimeString) {
		this.simulatedMissionTimeString = simulatedMissionTimeString;
	}

	@Inject
	public void initInterval(@NumberConfig(name = "time.interval") Integer interval) {
		this.interval = interval;
	}

	@Inject
	public void setStepSize(@NumberConfig(name = "time.stepSize") Integer stepSize) {
		this.stepSize = stepSize;
	}

}
