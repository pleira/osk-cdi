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

package org.osk.time;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.deltaspike.core.api.config.annotation.ConfigProperty;
import org.osk.config.NumberConfig;
import org.osk.events.TimeStep;

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
 */

@ApplicationScoped
public class TimeHandler {
    private static final int TIME_SYSTEM = 0;
    private static final int TIME_SIMULATION = 1;
    private static final int TIME_STEPS = 2;
    private final long[] time = new long[3];
    private String simulatedMissionTimeString; // = "2010-03-01T22:55:00.000+0000";
    private int interval;     
    private int stepSize;

    @PostConstruct
    public void init() throws ParseException {
        // 1985-04-12T23:20:50.100Z
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        format.setLenient(true);
        time[TIME_SYSTEM] = 0;
        time[TIME_STEPS] = 0;
        time[TIME_SIMULATION]
                   = format.parse(simulatedMissionTimeString).getTime();
    }

    public long getSystemTime() {
        return time[TIME_SYSTEM];
    }

    public long getSimulatedMissionTime() {
        return time[TIME_SIMULATION];
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

    @Produces @TimeStep
    public double getStepSizeAsDouble() {
        return stepSize / 1000.0;
    }

    public void setSystemTime(final long systemTime) {
        time[TIME_SYSTEM] = systemTime;
    }

    public void update() {
        time[TIME_STEPS]++;
        time[TIME_SIMULATION] += stepSize;
        time[TIME_SYSTEM] += interval;
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
