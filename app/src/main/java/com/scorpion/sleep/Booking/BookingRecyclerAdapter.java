package com.scorpion.sleep.Booking;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.scorpion.sleep.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by stephen on 2015-11-03.
 */
public class BookingRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int HEADER_VIEW = 0;
    private static final int DAY_VIEW = 1;

    private List<Day> data;
    private BookingActivity.BookingCallback bookingCallback;

    public BookingRecyclerAdapter(List<Day> data, BookingActivity.BookingCallback bookingCallback) {
        this.data = data;
        this.bookingCallback = bookingCallback;
    }

    private int getNumberDays() {
        return data.size();
    }

    private int getNumberSlots() {
        int counter = 0;
        for (Day d : data) {
            counter += d.getTimeslots().size();
        }
        return counter;
    }

    private Day getDayById(final int id)
    {
        int numDays = getNumberDays();
        return data.get(id % numDays);
    }

    private Timeslot getTimeslotById(final int id) {
        int numDays = getNumberDays();
        int newId = id - numDays;
        Day d = data.get(newId % numDays);
        Timeslot t = d.getTimeslots().get(newId / numDays);
        return t;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.booking_cell, viewGroup, false);
        if (type == HEADER_VIEW) {
            return new HeaderViewHolder(view);
        } else {
            return new TimeslotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int id) {
        if (viewHolder instanceof HeaderViewHolder) {

            String date = getDayById(id).getDate();
            try {
                date = convertDateToString(date);
                ((HeaderViewHolder) viewHolder).text.setText(date);
            } catch (ParseException e) {
                ((HeaderViewHolder) viewHolder).text.setText("ERR");
                e.printStackTrace();
            }

        } else if (viewHolder instanceof TimeslotViewHolder) {

            Timeslot time = getTimeslotById(id);
            ((TimeslotViewHolder) viewHolder).setId(id);

            if (time.isBooked()) {
                ((TimeslotViewHolder) viewHolder).setClickable(false);
                ((TimeslotViewHolder) viewHolder).text.setBackgroundColor(Color.parseColor("#FFB0B0B0"));

            } else {
                ((TimeslotViewHolder) viewHolder).setClickable(true);
                ((TimeslotViewHolder) viewHolder).text.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }

            ((TimeslotViewHolder) viewHolder).text.setText(convertIdToTime(time.getId()));

        }
    }

    @Override
    public int getItemCount() {
        return getNumberDays() + getNumberSlots();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < getNumberDays()) {
            return HEADER_VIEW;
        }
        return DAY_VIEW;
    }

    private String convertIdToTime(int timeId) {
        switch (timeId) {
            case 0:
                return "9:00";
            case 1:
                return "10:00";
            case 2:
                return "11:00";
            case 3:
                return "1:00";
            case 4:
                return "2:00";
            case 5:
                return "3:00";
            default:
                return "ERR";
        }
    }

    private String convertDateToString(String date) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateObject = format.parse(date);
        return dateObject.getDate() + "/" + (dateObject.getMonth() + 1);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {

        private TextView text;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.booking_text);
            text.setTypeface(null, Typeface.BOLD);
        }
    }

    class TimeslotViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private View itemView;
        private TextView text;
        private int id;

        public TimeslotViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            text = (TextView) itemView.findViewById(R.id.booking_text);
        }

        private void setId(int id) {
            this.id = id;
        }

        private void setClickable(boolean clickable) {
            if (clickable) {
                itemView.setOnClickListener(this);
            } else {
                itemView.setOnClickListener(null);
            }
        }

        @Override
        public void onClick(View view) {
            Day day = getDayById(id);
            Timeslot time = getTimeslotById(id);
            bookingCallback.onTimeClicked(day.getDate(), time.getId());
        }
    }
}
