package com.example.rompecabezas.Views;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.Controllers.PuzzleDBHelper;
import com.example.rompecabezas.R;

import java.util.ArrayList;

public class TiemposActivity extends AppCompatActivity {

    private ListView listView;
    private TiemposAdapter tiemposAdapter;
    private ArrayList<TiempoRecord> tiempoRecords;
    private PuzzleDBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tiempos);

        listView = findViewById(R.id.listViewTiempos);
        tiempoRecords = new ArrayList<>();
        dbHelper = new PuzzleDBHelper(this);

        // Cargar los tiempos desde la base de datos
        cargarTiemposDesdeBD();

        // Crear el adapter y asignarlo al ListView
        tiemposAdapter = new TiemposAdapter(this, tiempoRecords);
        listView.setAdapter(tiemposAdapter);
    }

    private void cargarTiemposDesdeBD() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tiempos", null);

        if (cursor.moveToFirst()) {
            do {
                String nombre = cursor.getString(cursor.getColumnIndex("nombre"));
                long tiempo = cursor.getLong(cursor.getColumnIndex("tiempo"));
                tiempoRecords.add(new TiempoRecord(nombre, tiempo));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
}
