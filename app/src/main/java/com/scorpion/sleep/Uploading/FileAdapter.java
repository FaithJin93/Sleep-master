package com.scorpion.sleep.Uploading;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.scorpion.sleep.R;

import java.io.File;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stephen on 2015-11-05.
 */
public class FileAdapter extends BaseAdapter {

    private List<File> _data;

    public FileAdapter(List<File> files) {
        _data = files;
    }

    @Override
    public int getCount() {
        return _data.size();
    }

    @Override
    public Object getItem(int i) {
        return _data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.file_item, viewGroup, false);
            holder = new ViewHolder();
            holder.filename = (TextView) view.findViewById(R.id.filename_textview);
            holder.filetime = (TextView) view.findViewById(R.id.filetime_textview);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        File current = _data.get(i);
        holder.filename.setText(current.getName());
        holder.filetime.setText(convertLongToDate(current.lastModified()));
        return view;
    }

    private String convertLongToDate(long time) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(time);
        int month = c.get(Calendar.MONTH) + 1;
        int day = c.get(Calendar.DAY_OF_MONTH);
        int year = c.get(Calendar.YEAR);
        return month + "/" + day + "/" + year;
    }

    static class ViewHolder {
        TextView filename;
        TextView filetime;
    }
}
