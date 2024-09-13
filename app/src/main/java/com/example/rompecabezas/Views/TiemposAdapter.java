package com.example.rompecabezas.Views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.rompecabezas.R;

import java.util.ArrayList;

public class TiemposAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TiempoRecord> tiemposList;

    public TiemposAdapter(Context context, ArrayList<TiempoRecord> tiemposList) {
        this.context = context;
        this.tiemposList = tiemposList;
    }

    @Override
    public int getCount() {
        return tiemposList.size();
    }

    @Override
    public Object getItem(int position) {
        return tiemposList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_tiempo, parent, false);
        }

        TextView nombreTextView = convertView.findViewById(R.id.nombreTextView);
        TextView tiempoTextView = convertView.findViewById(R.id.tiempoTextView);

        TiempoRecord record = tiemposList.get(position);
        nombreTextView.setText(record.getNombre());
        tiempoTextView.setText(record.getTiempo() + " segundos");

        return convertView;
    }
}
