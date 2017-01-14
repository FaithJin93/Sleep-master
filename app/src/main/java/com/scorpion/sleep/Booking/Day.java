package com.scorpion.sleep.Booking;

import java.util.List;

/**
 * Created by stephen on 2015-11-03.
 */
public class Day {
    private String date;
    private List<Timeslot> timeslots;

    public Day(String date, List<Timeslot> timeslots) {
        this.date = date;
        this.timeslots = timeslots;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(List<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }
}
