package com.example.rompecabezas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


public class FilasColumnas extends AppCompatActivity {

    Button  btndecrementarcolumnas,btnincrementarcolumnas,btndecrementarfilas,btnincrementarfilas,btnenviar,btnlimpiar;
    EditText etfilas,etcoulmnas,etnombre;

    Spinner spinnerDificultad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filas_columnas);
        etfilas=findViewById(R.id.etFilas);
        etcoulmnas=findViewById(R.id.etColumnas);
        etnombre=findViewById(R.id.etNombre);
        btndecrementarcolumnas=findViewById(R.id.btnDecrementarColumnas);
        btnincrementarcolumnas=findViewById(R.id.btnIncrementarColumnas);
        btndecrementarfilas=findViewById(R.id.btnDecrementarFilas);
        btnincrementarfilas=findViewById(R.id.btnIncrementarFilas);
        btnenviar=findViewById(R.id.btInvocar);
        btnlimpiar=findViewById(R.id.btLimpiar);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);

        // Configura el Spinner con las opciones de dificultad
        String[] dificultades = {"Facil", "Medio", "Dificil"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dificultades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapter);
        btnlimpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etfilas.setText("2");
                etcoulmnas.setText("2");
                etnombre.setText(null);
            }
        });
        btndecrementarcolumnas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(etcoulmnas.getText().toString())>2){
                    etcoulmnas.setText((Integer.parseInt(etcoulmnas.getText().toString())-1)+"");
                }else{
                    Toast.makeText(FilasColumnas.this, "Error, no se pueden usar menos de dos columnas", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnincrementarcolumnas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etcoulmnas.setText((Integer.parseInt(etcoulmnas.getText().toString())+1)+"");
            }
        });
        btndecrementarfilas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Integer.parseInt(etfilas.getText().toString())>2){
                    etfilas.setText((Integer.parseInt(etfilas.getText().toString())-1)+"");
                }else{
                    Toast.makeText(FilasColumnas.this, "Error, no se pueden usar menos de dos filas", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnincrementarfilas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etfilas.setText((Integer.parseInt(etfilas.getText().toString())+1)+"");
            }
        });
        btnenviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String b=etnombre.getText().toString();
                if(b!="") {
                    Bundle bolsa = new Bundle();
                    bolsa.putString("nombre", b);
                    bolsa.putString("filas",etcoulmnas.getText().toString());
                    bolsa.putString("columnas",etfilas.getText().toString());
                    bolsa.putString("dificultad",spinnerDificultad.getSelectedItem().toString());
                    Intent invocador = new Intent(FilasColumnas.this, Rompecabezas.class);
                    invocador.putExtras(bolsa);
                    startActivity(invocador);
                }else{
                    Toast.makeText(FilasColumnas.this, "Error, ingresar nombre", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}