package com.scorpion.sleep;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.scorpion.sleep.util.ConversionUtils;

import java.util.List;

/**
 * Created by stephen on 2015-11-29.
 */
public class HistoryAdapter extends BaseAdapter implements SpinnerAdapter {

    private List<HistoryItem> _data;
    private LayoutInflater _inflater;

    public HistoryAdapter(Context c, List<HistoryItem> data) {
        _data = data;
        _inflater = LayoutInflater.from(c);
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
        TextView v = (TextView) _inflater.inflate(android.R.layout.simple_spinner_item, viewGroup, false);
        String date = ConversionUtils.dateToString(_data.get(i).getVisitDate());
        v.setText(date);
        return v;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        CheckedTextView v = (CheckedTextView) _inflater.inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
        String date = ConversionUtils.dateToString(_data.get(position).getVisitDate());
        v.setText(date);
        return v;
    }
}
