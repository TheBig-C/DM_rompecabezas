package com.example.rompecabezas;

import android.content.Context;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class Fichas {
    private TextView textView;
    private  int id;
    // Constructor que recibe el contexto, el texto, el color de fondo y el color del texto
    public Fichas(Context context, String texto, String colorFondo, String colorTexto,int w,int h,int id) {
        textView = new TextView(context);
        this.id=id;
        // Configurando el TextView
        textView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(w, h));
        textView.setText(texto);
        textView.setTextSize(40);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor(colorFondo));
        textView.setTextColor(Color.parseColor(colorTexto));
    }
    public int[][] estadoMatriz(Fichas[][] matriz){
        int[][] estadoMatriz= new int[matriz.length][matriz[0].length];
        for(int i=0;i< matriz.length;i++){
            for(int j=0;j<matriz[0].length;j++){
                estadoMatriz[i][j]=matriz[i][j].getId();
            }
        }
        return estadoMatriz;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    // Método para obtener el TextView
    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }
}
