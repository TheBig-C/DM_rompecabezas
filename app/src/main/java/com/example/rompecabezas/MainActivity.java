package com.example.rompecabezas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btniniciar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            btniniciar=findViewById(R.id.btnIniciar);
            btniniciar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent invocador = new Intent(MainActivity.this, FilasColumnas.class);
                    startActivity(invocador);

                }
            });

    }
}