package com.example.rompecabezas.Controllers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PuzzleDBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "puzzle.db";
    private static final int DATABASE_VERSION = 2;  // Incrementa la versión de la base de datos

    public PuzzleDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Crear tabla para guardar el tiempo, con nombre de tipo TEXT
        db.execSQL("CREATE TABLE tiempos (id INTEGER PRIMARY KEY AUTOINCREMENT, tiempo INTEGER, nombre TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Actualizar la base de datos si es necesario
        db.execSQL("DROP TABLE IF EXISTS tiempos");
        onCreate(db);
    }
}
