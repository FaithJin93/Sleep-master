package com.scorpion.sleep.Booking;

/**
 * Created by stephen on 2015-11-03.
 */
public class Timeslot {
    private int id;
    private boolean booked;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Timeslot(int id, boolean booked) {
        this.id = id;
        this.booked = booked;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }
}
