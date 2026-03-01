package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatedBooking {
    private int bookingid;
    private NewBooking booking;

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public NewBooking getBooking() {
        return booking;
    }

    public void setBooking(NewBooking booking) {
        this.booking = booking;
    }
}
