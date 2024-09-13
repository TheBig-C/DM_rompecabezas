package com.example.rompecabezas.Views;

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

public class JuegoDoble extends AppCompatActivity {
    private Fichas[][] matrizFichas;
    private LinearLayout lr;
    private TextView tvCronometroJugador1, tvCronometroJugador2;
    private Button btnCambiarTurno, btnDesordenar;
    private ImageView ivReferencia;
    private boolean turnoJugador1 = true;
    private long tiempoInicioJugador1, tiempoInicioJugador2;
    private long tiempoCronometroJugador1 = 0L, tiempoCronometroJugador2 = 0L;
    private Handler handlerCronometro;
    private Runnable actualizarCronometro;
    private boolean cronometroCorriendo = false;
    private Fichas[][] estadoDesordenado; // Para almacenar el estado del puzzle desordenado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juego_doble);

        // Inicializar vistas
        tvCronometroJugador1 = findViewById(R.id.tvCronometroJugador1);
        tvCronometroJugador2 = findViewById(R.id.tvCronometroJugador2);
        btnCambiarTurno = findViewById(R.id.btnCambiarTurno);
        btnDesordenar = findViewById(R.id.btnDesordenar);
        ivReferencia = findViewById(R.id.ivReferencia);
        lr = findViewById(R.id.lyVerticalO);

        // Inicializar el cronómetro
        handlerCronometro = new Handler();
        actualizarCronometro = new Runnable() {
            @Override
            public void run() {
                long tiempoActual = SystemClock.elapsedRealtime();
                if (turnoJugador1) {
                    long tiempoTranscurrido = tiempoActual - tiempoInicioJugador1;
                    tiempoCronometroJugador1 = tiempoTranscurrido / 1000;
                    int minutos = (int) tiempoCronometroJugador1 / 60;
                    int segundos = (int) tiempoCronometroJugador1 % 60;
                    tvCronometroJugador1.setText(String.format("Jugador 1: Tiempo %02d:%02d", minutos, segundos));
                } else {
                    long tiempoTranscurrido = tiempoActual - tiempoInicioJugador2;
                    tiempoCronometroJugador2 = tiempoTranscurrido / 1000;
                    int minutos = (int) tiempoCronometroJugador2 / 60;
                    int segundos = (int) tiempoCronometroJugador2 % 60;
                    tvCronometroJugador2.setText(String.format("Jugador 2: Tiempo %02d:%02d", minutos, segundos));
                }
                handlerCronometro.postDelayed(this, 1000);
            }
        };

        // Inicializar puzzle y cronómetro para el Jugador 1
        btnDesordenar.setOnClickListener(v -> desordenarPuzzleParaJugador1());

        // Resolver puzzle

        // Cambiar turno
        btnCambiarTurno.setOnClickListener(v -> cambiarTurno());

        iniciarJuego();
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
    private void iniciarJuego() {
        // Inicializamos el puzzle para el Jugador 1
        crearFichasTexto(4, 4, "dificil");
        for (int i = 0; i < 4; i++) {
            final int ii = i;
            for (int j = 0; j < 4; j++) {
                final int jj = j;
                matrizFichas[ii][jj].getView().setOnClickListener(v -> manejarClickFicha(ii, jj));
            }
        }

        // Iniciar el cronómetro del Jugador 1
        iniciarCronometroJugador1();
    }

    private void manejarClickFicha(int i, int j) {
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
            actualizarInterfaz(); // Actualizar la interfaz tras el movimiento
        }
    }

    private void iniciarCronometroJugador1() {
        tiempoInicioJugador1 = SystemClock.elapsedRealtime();
        handlerCronometro.postDelayed(actualizarCronometro, 0);
        cronometroCorriendo = true;
    }

    private void desordenarPuzzleParaJugador1() {
        desordenar("Medio", matrizFichas);
        estadoDesordenado = clonar(matrizFichas, getApplicationContext()); // Guardar el estado desordenado
        actualizarInterfaz(); // Actualizar la interfaz gráfica después de desordenar
        Toast.makeText(this, "El puzzle ha sido desordenado para el Jugador 1", Toast.LENGTH_SHORT).show();
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
    private void cambiarTurno() {
        if (turnoJugador1) {
            // Jugador 1 ha terminado, detener su cronómetro
            handlerCronometro.removeCallbacks(actualizarCronometro);
            cronometroCorriendo = false;
            turnoJugador1 = false;

            // Preparar el turno del Jugador 2
            cargarEstadoDesordenado(); // Cargar el mismo estado desordenado para el Jugador 2
            Toast.makeText(this, "Es el turno del Jugador 2", Toast.LENGTH_SHORT).show();

            // Iniciar el cronómetro del Jugador 2
            iniciarCronometroJugador2();

            btnCambiarTurno.setVisibility(View.GONE); // Ocultar el botón después de cambiar el turno
        } else {
            // Jugador 2 ha terminado, mostrar los resultados
            handlerCronometro.removeCallbacks(actualizarCronometro);
            cronometroCorriendo = false;

            mostrarResultados();
        }
    }

    private void iniciarCronometroJugador2() {
        tiempoInicioJugador2 = SystemClock.elapsedRealtime();
        handlerCronometro.postDelayed(actualizarCronometro, 0);
        cronometroCorriendo = true;
    }

    private void cargarEstadoDesordenado() {
        // Limpiar la interfaz antes de cargar el nuevo puzzle
        lr.removeAllViews();

        // Cargar el estado desordenado del Jugador 1 para el Jugador 2
        matrizFichas = clonar(estadoDesordenado, getApplicationContext());

        actualizarInterfaz(); // Actualizar la interfaz gráfica con el puzzle desordenado
    }
    public Fichas[][] clonar(Fichas[][] m, Context context) {
        Fichas[][] clo = new Fichas[m.length][m[0].length];
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                // Obtener los valores del objeto Fichas original
                String texto = "";
                String colorFondo="",colorTexto="";
                int id=0,width=0,height=0;
                id = m[i][j].getId();


                    texto= m[i][j].getTextView().getText().toString();
                    colorFondo = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getSolidColor()));
                    colorTexto = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getCurrentTextColor()));
                    width = m[i][j].getTextView().getLayoutParams().width;
                    height = m[i][j].getTextView().getLayoutParams().height;




                    clo[i][j] = new Fichas(context, texto, colorFondo, colorTexto, width, height, id);


            }
        }
        return clo;
    }
    private void actualizarInterfaz() {
        // Esta función actualiza la interfaz gráfica con el estado de la matriz
        lr.removeAllViews(); // Limpiamos el contenedor para evitar duplicaciones

        for (int i = 0; i < matrizFichas.length; i++) {
            LinearLayout fila = new LinearLayout(this);

            for (int j = 0; j < matrizFichas[i].length; j++) {
                View fichaView = matrizFichas[i][j].getView();

                // Verificar si la vista ya tiene un padre
                if (fichaView.getParent() != null) {
                    // Remover la vista de su padre si ya lo tiene
                    ((ViewGroup) fichaView.getParent()).removeView(fichaView);
                }

                fila.addView(fichaView); // Agregar la vista al nuevo contenedor
            }
            lr.addView(fila); // Añadir la fila completa al layout principal
        }
    }

    private void mostrarResultados() {
        // Comparamos los tiempos de ambos jugadores y mostramos quién ganó
        String ganador;
        if (tiempoCronometroJugador1 < tiempoCronometroJugador2) {
            ganador = "Jugador 1";
        } else if (tiempoCronometroJugador1 > tiempoCronometroJugador2) {
            ganador = "Jugador 2";
        } else {
            ganador = "Empate";
        }

        Toast.makeText(this, "El ganador es: " + ganador, Toast.LENGTH_LONG).show();
    }

    private void resolverPuzzle() {
        if (turnoJugador1) {
            if (isSolucion(matrizFichas)) {
                Toast.makeText(this, "¡Jugador 1 ha resuelto el puzzle!", Toast.LENGTH_SHORT).show();
                btnCambiarTurno.setVisibility(View.VISIBLE); // Mostrar el botón para cambiar turno
            }
        } else {
            if (isSolucion(matrizFichas)) {
                Toast.makeText(this, "¡Jugador 2 ha resuelto el puzzle!", Toast.LENGTH_SHORT).show();
                mostrarResultados(); // Mostrar los resultados al finalizar
            }
        }
    }

    private boolean isSolucion(Fichas[][] estado) {
        for (int i = 0; i < estado.length; i++) {
            for (int j = 0; j < estado[i].length; j++) {
                if (estado[i][j].getId() != (i * estado[i].length) + j + 1) {
                    return false;
                }
            }
        }
        return true;
    }

    public int[][] matrizSol(int f,int c){
        int[][] sol= new int[f][c];
        for(int i=0;i<f;i++){
            for(int j=0;j<c;j++){
                sol[i][j]=(i*c)+j+1;
            }
        }
        return sol;
    }
    public int[] encontrardX(Integer[] fichas, int boardSize) {
        int ii = 0, jj = 0;
        // Imprimir la fila

        for (int i = 0; i < fichas.length; i++) {
            // Verificar si el valor es el mayor en el tablero (es decir, el espacio en blanco).
            if (fichas[i] == 0) {

                // Calcular las coordenadas fila (ii) y columna (jj) en base al índice del array
                ii = i / boardSize;  // Fila
                jj = i % boardSize;  // Columna

                break;
            }
        }
        return new int[]{ii, jj};
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

    public int move(Fichas[][] mat, int i, int j) {
        int posible = -1;
        if (i > 0) posible = (mat[i - 1][j].getId() == mat.length * mat[0].length) ? 1 : posible;
        if (i < (mat.length - 1)) posible = (mat[i + 1][j].getId() == mat.length * mat[0].length) ? 2 : posible;
        if (j > 0) posible = (mat[i][j - 1].getId() == mat.length * mat[0].length) ? 3 : posible;
        if (j < (mat[0].length - 1)) posible = (mat[i][j + 1].getId() == mat.length * mat[0].length) ? 4 : posible;
        return posible;
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
    private String getTexto(int fila, int columna) {
        char letra = (char) ('A' + fila * matrizFichas[0].length + columna);
        return String.valueOf(letra);
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
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


}
