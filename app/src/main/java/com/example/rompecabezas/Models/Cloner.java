package com.example.rompecabezas.Models;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.TextView;
import android.util.AttributeSet;
import android.view.ViewGroup.LayoutParams;

public class Cloner {
    public static TextView cloneTextView(Context context, TextView original) {
        if(original==null)
            return null;
        // Crear un nuevo TextView
        TextView clone = new TextView(context);

        // Copiar atributos del original al clon
        clone.setLayoutParams(new LayoutParams(original.getLayoutParams().width, original.getLayoutParams().height));
        clone.setText(original.getText());
        clone.setTextSize(original.getTextSize());
        clone.setGravity(original.getGravity());
        clone.setBackground(original.getBackground());
        clone.setTextColor(original.getCurrentTextColor());

        return clone;
    }
}
