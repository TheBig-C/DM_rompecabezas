package com.example.rompecabezas.Models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Fichas {
    private TextView textView;
    private ImageView imageView;
    private int id;
    private boolean isText;  // Indicador de si es un TextView o ImageView

    // Constructor para texto
    public Fichas(Context context, String texto, String colorFondo, String colorTexto, int w, int h, int id) {
        textView = new TextView(context);
        this.id = id;
        this.isText = true;  // Este objeto representa un TextView
        textView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(w, h));
        textView.setText(texto);
        textView.setTextSize(40);
        textView.setGravity(Gravity.CENTER);
        textView.setBackgroundColor(Color.parseColor(colorFondo));
        textView.setTextColor(Color.parseColor(colorTexto));
    }

    // Constructor para imagen
    public Fichas(Context context, Bitmap imagen, int w, int h, int id) {
        imageView = new ImageView(context);
        this.id = id;
        this.isText = false;  // Este objeto representa un ImageView
        imageView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(w, h));
        imageView.setImageBitmap(imagen);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public Fichas(Context context, Drawable drawable, int width, int height, int id) {
        imageView = new ImageView(context);
        this.id = id;
        this.isText = false;  // Este objeto representa un ImageView
        imageView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(width, height));
        imageView.setImageDrawable(drawable);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isText() {
        return isText;
    }

    public TextView getTextView() {
        return textView;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public View getView() {
        return isText ? textView : imageView;
    }
}
