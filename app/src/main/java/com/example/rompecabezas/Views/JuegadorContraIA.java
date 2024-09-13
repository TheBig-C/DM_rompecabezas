package com.example.rompecabezas.Views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.rompecabezas.Models.Fichas;
import com.example.rompecabezas.R;

import java.util.ArrayList;
import java.util.Random;

public class JuegadorContraIA extends AppCompatActivity {
    private Fichas[][] matrizFichas, matrizFichasIA;
    private LinearLayout lrJugador, lrIA;
    private TextView tvCronometroJugador, tvCronometroIA;
    private Button btnDesordenar, btnResolver;
    private ImageView ivReferencia;
    private long tiempoInicioJugador, tiempoInicioIA;
    private long tiempoCronometroJugador = 0L, tiempoCronometroIA = 0L;
    private Handler handlerCronometro, handlerIA;
    private Runnable actualizarCronometro, movimientoIA;
    private boolean cronometroCorriendo = false;
    private boolean iaResuelto = false;
    LinearLayout lr;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jugador_contra_ia);
        lr = findViewById(R.id.lyVerticalO);

        // Inicializar vistas
        tvCronometroJugador = findViewById(R.id.tvCronometroJugador);
        tvCronometroIA = findViewById(R.id.tvCronometroIA);
        btnDesordenar = findViewById(R.id.btnDesordenar);
        btnResolver = findViewById(R.id.btnResolver);
        ivReferencia = findViewById(R.id.ivReferencia);
        lrJugador = findViewById(R.id.lyVerticalJugador);
        lrIA = findViewById(R.id.lyVerticalIA);

        // Inicializar el cronómetro
        handlerCronometro = new Handler();
        handlerIA = new Handler();

        actualizarCronometro = new Runnable() {
            @Override
            public void run() {
                long tiempoActual = SystemClock.elapsedRealtime();
                long tiempoTranscurridoJugador = tiempoActual - tiempoInicioJugador;
                tiempoCronometroJugador = tiempoTranscurridoJugador / 1000;
                int minutosJugador = (int) tiempoCronometroJugador / 60;
                int segundosJugador = (int) tiempoCronometroJugador % 60;
                tvCronometroJugador.setText(String.format("Jugador: %02d:%02d", minutosJugador, segundosJugador));

                long tiempoTranscurridoIA = tiempoActual - tiempoInicioIA;
                tiempoCronometroIA = tiempoTranscurridoIA / 1000;
                int minutosIA = (int) tiempoCronometroIA / 60;
                int segundosIA = (int) tiempoCronometroIA % 60;
                tvCronometroIA.setText(String.format("IA: %02d:%02d", minutosIA, segundosIA));

                handlerCronometro.postDelayed(this, 1000);
            }
        };

        // Inicializar puzzle y cronómetro para el Jugador
        btnDesordenar.setOnClickListener(v -> desordenarPuzzle());

        iniciarJuego();
    }
    private void desordenar(String nivel, Fichas[][] matriz) {
        int cantidad = (nivel.equals("Facil")) ? 10 : (nivel.equals("Medio")) ? 20 : 30;

        for (int i = 0; i < cantidad; i++) {
            int[] posFichaBlanca = encontrarFichaBlanca(matriz);
            int ii = posFichaBlanca[0];
            int jj = posFichaBlanca[1];

            ArrayList<String> posiblesMovimientos = camino(matriz);
            Random random = new Random();
            String direccion = posiblesMovimientos.get(random.nextInt(posiblesMovimientos.size()));

            int nuevoI = ii, nuevoJ = jj;

            if (direccion.equals("arriba")) nuevoI--;
            else if (direccion.equals("abajo")) nuevoI++;
            else if (direccion.equals("izquierda")) nuevoJ--;
            else if (direccion.equals("derecha")) nuevoJ++;

            if (nuevoI >= 0 && nuevoI < matriz.length && nuevoJ >= 0 && nuevoJ < matriz[0].length) {
                intercambiarFichas(matriz[ii][jj], matriz[nuevoI][nuevoJ]);
            }
        }
    }
    private String getColorFondo(int fila, int columna) {
        int totalFichas = matrizFichas.length * matrizFichas[0].length;
        String[] coloresVivos = {
                "#FF5733", "#FF8D33", "#FFC300", "#DAF7A6",
                "#33FF57", "#33FF8D", "#33FFC3", "#33DAFF",
                "#338DFF", "#5733FF", "#8D33FF", "#C300FF",
                "#FF33DA", "#FF3399", "#FF3366", "#FF3333"
        };
        int posicion = fila * matrizFichas[0].length + columna;
        if (posicion == totalFichas - 1) {
            return "#FFFFFF";
        }
        return coloresVivos[posicion % coloresVivos.length];
    }
    private String getTexto(int fila, int columna) {
        char letra = (char) ('A' + fila * matrizFichas[0].length + columna);
        return String.valueOf(letra);
    }

    private void crearFichasTexto(int f, int c, String seleccion) {
        matrizFichas = new Fichas[f][c];  // Asegurarse de que la matriz esté inicializada
        for (int i = 0; i < f; i++) {
            LinearLayout aux = createHorizontalLinearLayout(getApplicationContext());

            for (int j = 0; j < c; j++) {
                String texto = getTexto(i, j);
                String colorFondo = getColorFondo(i, j);

                matrizFichas[i][j] = new Fichas(getApplicationContext(), seleccion=="Letras"?(i == f - 1 && j == c - 1) ? "" : texto:(i == f - 1 && j == c - 1) ? "" : i*f+j+1+"", colorFondo, (i == f - 1 && j == c - 1) ? "#000000" : "#FFFFFF", dpToPx(80), dpToPx(80), (i * c) + j + 1);

                if (i == f - 1 && j == c - 1) {
                    matrizFichas[i][j].setId(f * c);  // Establecer el ID para identificar la ficha blanca
                }

                if (matrizFichas[i][j].getView() != null) {
                    aux.addView(matrizFichas[i][j].getView());
                } else {
                    Log.e("Error", "View is null for Fichas at position: " + i + ", " + j);
                }

                if (j < c - 1) {
                    aux.addView(spaceVertical());
                }
            }

            lr.addView(aux);
            if (i < f - 1) {
                lr.addView(createVerticalSpace(getApplicationContext()));
            }
        }
    }
    private Space spaceVertical() {
        Space sp = new Space(getApplicationContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(5),
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        sp.setLayoutParams(params);
        return sp;
    }
    private Space createVerticalSpace(Context context) {
        Space sp = new Space(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                dpToPx(5)
        );
        sp.setLayoutParams(params);
        return sp;
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private LinearLayout createHorizontalLinearLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        return linearLayout;
    }
    private void iniciarJuego() {
        // Inicializamos los puzzles para el Jugador y la IA
        crearFichasTexto(4, 4, "jugador");
        crearFichasTexto(4, 4, "ia");

        for (int i = 0; i < 4; i++) {
            final int ii = i;
            for (int j = 0; j < 4; j++) {
                final int jj = j;
                matrizFichas[ii][jj].getView().setOnClickListener(v -> manejarClickFichaJugador(ii, jj));
            }
        }

        // Iniciar cronómetro para ambos
        iniciarCronometro();

        // Iniciar la IA con movimientos lentos
        movimientoIA = new Runnable() {
            @Override
            public void run() {
                if (!iaResuelto) {
                    moverUnaFichaIA();
                    handlerIA.postDelayed(this, 2000);  // La IA se moverá cada 2 segundos
                }
            }
        };
        handlerIA.postDelayed(movimientoIA, 2000);  // La IA empieza a moverse lentamente
    }

    private void manejarClickFichaJugador(int i, int j) {
        int mov = move(matrizFichas, i, j);
        if (mov != -1) {
            Fichas fichaBlanca = null;
            if (mov == 1) {
                fichaBlanca = matrizFichas[i - 1][j];
            } else if (mov == 2) {
                fichaBlanca = matrizFichas[i + 1][j];
            } else if (mov == 3) {
                fichaBlanca = matrizFichas[i][j - 1];
            } else if (mov == 4) {
                fichaBlanca = matrizFichas[i][j + 1];
            }

            intercambiarFichas(matrizFichas[i][j], fichaBlanca);
            actualizarInterfazJugador(); // Actualizar la interfaz tras el movimiento
        }
    }

    private void iniciarCronometro() {
        tiempoInicioJugador = SystemClock.elapsedRealtime();
        tiempoInicioIA = SystemClock.elapsedRealtime();
        handlerCronometro.postDelayed(actualizarCronometro, 0);
        cronometroCorriendo = true;
    }

    private void desordenarPuzzle() {
        desordenar("Medio", matrizFichas);
        desordenar("Medio", matrizFichasIA);
        actualizarInterfazJugador();
        actualizarInterfazIA();
        Toast.makeText(this, "Los puzzles han sido desordenados", Toast.LENGTH_SHORT).show();
    }
    private int[] encontrarFichaBlanca(Fichas[][] matriz) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[0].length; j++) {
                if (matriz[i][j].getId() == matriz.length * matriz[0].length) {
                    return new int[]{i, j};
                }
            }
        }
        return null; // Esto no debería ocurrir, ya que siempre debería haber una ficha blanca
    }

    public int move(Fichas[][] mat, int i, int j) {
        int posible = -1;
        if (i > 0) posible = (mat[i - 1][j].getId() == mat.length * mat[0].length) ? 1 : posible;
        if (i < (mat.length - 1)) posible = (mat[i + 1][j].getId() == mat.length * mat[0].length) ? 2 : posible;
        if (j > 0) posible = (mat[i][j - 1].getId() == mat.length * mat[0].length) ? 3 : posible;
        if (j < (mat[0].length - 1)) posible = (mat[i][j + 1].getId() == mat.length * mat[0].length) ? 4 : posible;
        return posible;
    }
    public boolean isSolucion(Fichas[][] estado ){
        System.out.println("fadsfasd ");
        for(int i=0;i<estado.length;i++){
            for(int j=0;j<estado[0].length;j++){
                if(estado[i][j].getId()!=((i*estado[0].length)+j+1)){
                    return false;
                }
            }
        }
        return true;
    }
    public int[] encontrarX(Fichas[][] fichas){
        int ii=0,jj=0;
        for(int i =0;i<fichas.length;i++){
            for (int j=0;j<fichas[0].length;j++){
                if(fichas[i][j].getId()==fichas.length*fichas[0].length){
                    ii=i;
                    jj=j;
                }
            }
        }
        return new int[]{ii, jj};
    }
    public ArrayList<String> camino(Fichas[][] fichas){
        ArrayList<String> res=new ArrayList<String>();
        int ii=0,jj=0;
        int[] ad=encontrarX(fichas);
        ii=ad[0];
        jj=ad[1];
        if(ii>0){
            res.add("arriba");
        }
        if(ii<(fichas.length-1)){
            res.add("abajo");
        }
        if(jj>0){
            res.add("izquierda");

        }
        if(jj<(fichas[0].length-1)){
            res.add("derecha");


        }
        return res;
    }
    private void moverUnaFichaIA() {
        // Mueve una ficha de la IA de forma automática y lenta
        if (isSolucion(matrizFichasIA)) {
            iaResuelto = true;
            Toast.makeText(this, "¡La IA ha resuelto el puzzle!", Toast.LENGTH_SHORT).show();
            return;
        }



        // Encontrar movimientos posibles y hacer un movimiento aleatorio
        int[] posFichaBlanca = encontrarFichaBlanca(matrizFichasIA);
        int ii = posFichaBlanca[0];
        int jj = posFichaBlanca[1];

        ArrayList<String> posiblesMovimientos = camino(matrizFichasIA);
        Random random = new Random();
        String direccion = posiblesMovimientos.get(random.nextInt(posiblesMovimientos.size()));

        int nuevoI = ii, nuevoJ = jj;
        if (direccion.equals("arriba")) nuevoI--;
        else if (direccion.equals("abajo")) nuevoI++;
        else if (direccion.equals("izquierda")) nuevoJ--;
        else if (direccion.equals("derecha")) nuevoJ++;

        if (nuevoI >= 0 && nuevoI < matrizFichasIA.length && nuevoJ >= 0 && nuevoJ < matrizFichasIA[0].length) {
            intercambiarFichas(matrizFichasIA[ii][jj], matrizFichasIA[nuevoI][nuevoJ]);
        }

        actualizarInterfazIA(); // Actualizar la interfaz de la IA
    }
    private void intercambiarFichas(Fichas ficha1, Fichas ficha2) {

        // Intercambiar textos y colores de TextViews
        String txt = ficha1.getTextView().getText().toString();
        Drawable background = ficha1.getTextView().getBackground();
        int textColor = ficha1.getTextView().getCurrentTextColor();
        int id = ficha1.getId();

        ficha1.getTextView().setText(ficha2.getTextView().getText().toString());
        ficha1.getTextView().setBackground(ficha2.getTextView().getBackground());
        ficha1.getTextView().setTextColor(ficha2.getTextView().getCurrentTextColor());
        ficha1.setId(ficha2.getId());

        ficha2.getTextView().setText(txt);
        ficha2.getTextView().setBackground(background);
        ficha2.getTextView().setTextColor(textColor);
        ficha2.setId(id);


    }

    private void actualizarInterfazJugador() {
        lrJugador.removeAllViews(); // Limpiamos el contenedor
        for (int i = 0; i < matrizFichas.length; i++) {
            LinearLayout fila = new LinearLayout(this);
            for (int j = 0; j < matrizFichas[i].length; j++) {
                View fichaView = matrizFichas[i][j].getView();
                if (fichaView.getParent() != null) {
                    ((ViewGroup) fichaView.getParent()).removeView(fichaView);
                }
                fila.addView(fichaView); // Agregar la vista al nuevo contenedor
            }
            lrJugador.addView(fila); // Añadir la fila completa al layout del jugador
        }
    }

    private void actualizarInterfazIA() {
        lrIA.removeAllViews(); // Limpiamos el contenedor de la IA
        for (int i = 0; i < matrizFichasIA.length; i++) {
            LinearLayout fila = new LinearLayout(this);
            for (int j = 0; j < matrizFichasIA[i].length; j++) {
                View fichaView = matrizFichasIA[i][j].getView();
                if (fichaView.getParent() != null) {
                    ((ViewGroup) fichaView.getParent()).removeView(fichaView);
                }
                fila.addView(fichaView); // Agregar la vista al nuevo contenedor
            }
            lrIA.addView(fila); // Añadir la fila completa al layout de la IA
        }
    }

    // Resto del código: funciones auxiliares como intercambiarFichas, move, encontrarFichaBlanca, desordenar, etc.

}
