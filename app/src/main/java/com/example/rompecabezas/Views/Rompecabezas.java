package com.example.rompecabezas.Views;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.rompecabezas.Controllers.*;
import com.example.rompecabezas.Models.Fichas;
import com.example.rompecabezas.Models.Node;
import com.example.rompecabezas.R;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Rompecabezas extends AppCompatActivity {
    private Fichas[][] matrizFichas;
    LinearLayout lr;
    String nombre;
    Button btResolver, btDesordenar;
    boolean im = false;
    private long tiempoInicio;
    private Handler handlerCronometro;
    private Runnable actualizarCronometro;
    private long tiempoCronometro = 0L;
    private boolean cronometroCorriendo = false;  // Variable para verificar si el cronómetro está corriendo
    private TextView tvCronometro;  // TextView para mostrar el cronómetro

    // Base de datos SQLite para guardar el tiempo
    private SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rompecabezas);

        // Inicializar base de datos
        PuzzleDBHelper dbHelper = new PuzzleDBHelper(this);
         db = dbHelper.getWritableDatabase();
        tvCronometro = findViewById(R.id.tvCronometro);  // Asegúrate de que el id es correcto
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
        copiarArchivoDesdeAssets("patternDb_4.dat");

        lr = findViewById(R.id.lyVerticalO);
        btResolver = findViewById(R.id.btnResolver);
        btDesordenar = findViewById(R.id.btnDesordenar);
        ImageView ivReferencia = findViewById(R.id.ivReferencia);  // Referencia a la imagen de referencia
        nombre=obtenerDatoDePreferencias("nombre");
        String filas = obtenerDatoDePreferencias("filas");
        String columnas = obtenerDatoDePreferencias("columnas");
        String rutaImagen = obtenerDatoDePreferencias("rutaImagen");
        String rutaMatriz = obtenerDatoDePreferencias("rutaMatriz");
        String seleccion = obtenerDatoDePreferencias("seleccion");
        String nivel = obtenerDatoDePreferencias("nivel");
        ImageView ivPista = findViewById(R.id.ivPista);

// Configuramos el listener del ImageView para manejar el clic
        ivPista.setOnClickListener(v -> {
            moverUnaFicha(matrizFichas);
        });

        if (filas == null || columnas == null) {
            Toast.makeText(this, "Datos faltantes para las filas o columnas", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        int f, c;
        try {
            f = Integer.parseInt(filas);
            c = Integer.parseInt(columnas);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Error al convertir filas o columnas", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (seleccion != null && seleccion.equals("Imagen")) {
            im = true;
            Bitmap imagenCompleta = null;
            if (rutaImagen != null && !rutaImagen.isEmpty()) {
                imagenCompleta = cargarImagenDesdeAlmacenamiento(rutaImagen);
                if (imagenCompleta == null) {
                    Toast.makeText(this, "Error al cargar la imagen desde el almacenamiento", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                // Cargar la imagen completa en el ImageView de referencia
                ivReferencia.setImageBitmap(imagenCompleta);
            }

            matrizFichas = cargarMatrizFichasDesdeAlmacenamiento(rutaMatriz);
            if (matrizFichas == null) {
                matrizFichas = new Fichas[f][c];  // Crear matriz vacía si no se pudo cargar
            }
            dividirImagen(imagenCompleta, f, c);

        } else {
            // Si es texto, ocultar la imagen de referencia
            ivReferencia.setVisibility(View.GONE);
            crearFichasTexto(f, c, seleccion);
        }

        for (int i = 0; i < f; i++) {
            final int ii = i;
            for (int j = 0; j < c; j++) {
                final int jj = j;
                matrizFichas[ii][jj].getView().setOnClickListener(v -> {manejarClickFicha(ii, jj);

                });
            }
        }

        // Configurar los botones
        btDesordenar.setOnClickListener(v -> {
            desordenar(nivel, matrizFichas);
            iniciarCronometro();  // Iniciar cronómetro al desordenar
        });

        btResolver.setOnClickListener(v -> {
            if(filas.equals(""+4)&& columnas.equals(""+4)){
                ordenar4x4(matrizFichas);

            }else{
                ordenar(matrizFichas);

            }
        });

        // Handler para actualizar cronómetro cada segundo
        // Handler para actualizar cronómetro cada segundo
        handlerCronometro = new Handler();
        actualizarCronometro = new Runnable() {
            @Override
            public void run() {
                long tiempoActual = SystemClock.elapsedRealtime() - tiempoInicio;
                tiempoCronometro = tiempoActual / 1000;  // Convertir a segundos
                int minutos = (int) tiempoCronometro / 60;
                int segundos = (int) tiempoCronometro % 60;

                // Actualizar el TextView con el tiempo formateado
                tvCronometro.setText(String.format("Tiempo: %02d:%02d", minutos, segundos));

                // Seguir actualizando el cronómetro si está corriendo
                handlerCronometro.postDelayed(this, 1000);
            }
        };
    }
    private void moverUnaFicha(Fichas[][] matrizFichas) {
        // Crear una lista abierta y el historial para el algoritmo A*
        ArrayList<Node> abierto = new ArrayList<>();
        ArrayList<Node> historial = new ArrayList<>();

        // Añadir el estado inicial del puzzle
        abierto.add(new Node(clonar(matrizFichas, getApplicationContext())));
        historial.add(new Node(clonar(matrizFichas, getApplicationContext())));

        while (!abierto.isEmpty()) {
            // Comprobamos si el primer nodo es la solución
            if (isSolucion(abierto.get(0).data)) {
                break; // No hacemos nada si ya está resuelto
            } else {
                // Generamos los posibles movimientos desde el estado actual
                ArrayList<Node> hijos = agregarChilds(abierto.get(0));
                abierto.remove(0);

                // Añadir hijos al historial y abiertos si no están ya en historial
                for (Node hijo : hijos) {
                    if (!isInHistorial(historial, hijo)) {
                        abierto.add(hijo);
                    }
                }
                agregarHistorial(historial, hijos);

                // Ordenar la lista abierta basándonos en la heurística H
                ArrayList<Integer> h = calcularH(abierto, 0);
                for (int k = 1; k < abierto.size(); k++) {
                    for (int l = 0; l < abierto.size() - k; l++) {
                        if (h.get(l) > h.get(l + 1)) {
                            Collections.swap(h, l, l + 1);
                            Collections.swap(abierto, l, l + 1);
                        }
                    }
                }
            }

            // Una vez obtenidos los posibles movimientos, movemos solo el primero
            Node primerMovimiento = abierto.get(0);  // Este es el siguiente paso más óptimo
            cargarSol(primerMovimiento, matrizFichas);  // Actualizamos la matriz con el primer movimiento

            // Detenemos después de mover una ficha
            break;
        }
    }

    private void copiarArchivoDesdeAssets(String nombreArchivo) {
        AssetManager assetManager = getAssets();
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            inputStream = assetManager.open(nombreArchivo);
            File archivoDestino = new File(getFilesDir(), nombreArchivo);
            outputStream = new FileOutputStream(archivoDestino);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
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

            // Verificar si el puzzle está resuelto
            if (isSolucion(matrizFichas)) {
                Toast.makeText(this, "¡Puzzle resuelto!", Toast.LENGTH_LONG).show();
                detenerCronometroYGuardar();  // Detener cronómetro y guardar tiempo en la BD
            }
        }
    }


    // Iniciar cronómetro
    private void iniciarCronometro() {
        if (!cronometroCorriendo) {  // Verificar que el cronómetro no esté corriendo
            tiempoInicio = SystemClock.elapsedRealtime();
            handlerCronometro.postDelayed(actualizarCronometro, 0);
            cronometroCorriendo = true;
            tvCronometro.setText("Tiempo: 00:00");  // Reiniciar el cronómetro a 0
        }
    }


    // Detener cronómetro y guardar el tiempo
    private void detenerCronometroYGuardar() {
        handlerCronometro.removeCallbacks(actualizarCronometro);
        cronometroCorriendo = false;  // Detener el cronómetro
        long tiempoTotal = tiempoCronometro;  // Tiempo en segundos

        // Guardar el tiempo en SQLite
        guardarTiempoEnBD(tiempoTotal);
    }

    private void guardarTiempoEnBD(long tiempoTotal) {
        // Asegúrate de que el valor de 'nombre' esté entre comillas simples en la sentencia SQL
        db.execSQL("INSERT INTO tiempos (tiempo, nombre) VALUES (" + tiempoTotal + ", '" + nombre + "')");
        Toast.makeText(this, "Tiempo guardado: " + tiempoTotal + " segundos", Toast.LENGTH_LONG).show();
    }

    // Guardar datos pequeños en SharedPreferences


    // Guardar datos en SharedPreferences
    private void guardarDatosEnPreferencias(String key, String value) {
        getSharedPreferences("prefs", MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    // Obtener datos desde SharedPreferences
    private String obtenerDatoDePreferencias(String key) {
        return getSharedPreferences("prefs", MODE_PRIVATE)
                .getString(key, null);
    }



    // Métodos ya existentes para guardar y cargar desde el almacenamiento interno
    // Guardar la imagen en almacenamiento interno


    // Cargar la imagen desde el almacenamiento interno
    private Bitmap cargarImagenDesdeAlmacenamiento(String rutaImagen) {
        File imgFile = new File(rutaImagen);
        if (imgFile.exists()) {
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        }
        return null;
    }

    // Guardar la matriz de fichas en almacenamiento interno

    // Cargar la matriz de fichas desde el almacenamiento interno
    private Fichas[][] cargarMatrizFichasDesdeAlmacenamiento(String rutaArchivo) {
        if (rutaArchivo == null || rutaArchivo.isEmpty()) {
            return null;  // Si la ruta es null o vacía, retornamos null
        }

        File file = new File(rutaArchivo);
        if (!file.exists()) {
            return null;  // Si el archivo no existe, retornamos null
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (Fichas[][]) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // En caso de error, retornamos null
        }
    }



    // Modificar este método para no pasar extras





    private void dividirImagen(Bitmap imagen, int filas, int columnas) {
        int anchoPieza = imagen.getWidth() / columnas;
        int altoPieza = imagen.getHeight() / filas;

        for (int i = 0; i < filas; i++) {
            LinearLayout aux = createHorizontalLinearLayout(getApplicationContext());

            for (int j = 0; j < columnas; j++) {
                Bitmap pieza = Bitmap.createBitmap(imagen, j * anchoPieza, i * altoPieza, anchoPieza, altoPieza);
                matrizFichas[i][j] = new Fichas(getApplicationContext(), pieza, dpToPx(80), dpToPx(80), (i * columnas) + j + 1);

                if (i == filas - 1 && j == columnas - 1) {
                    matrizFichas[i][j].getImageView().setImageBitmap(Bitmap.createBitmap(anchoPieza, altoPieza, Bitmap.Config.ARGB_8888));
                    matrizFichas[i][j].setId(filas * columnas);  // Establecer el ID para identificar la ficha blanca
                }

                if (matrizFichas[i][j].getView() != null) {
                    aux.addView(matrizFichas[i][j].getView());
                } else {
                    Log.e("Error", "View is null for Fichas at position: " + i + ", " + j);
                }

                if (j < columnas - 1) {
                    aux.addView(spaceVertical());
                }
            }

            lr.addView(aux);
            if (i < filas - 1) {
                lr.addView(createVerticalSpace(getApplicationContext()));
            }
        }
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


    private void ordenar(Fichas[][] matrizFichas) {
        System.out.println("ord");
        ArrayList<Node> abierto = new ArrayList<>();
        ArrayList<Node> historial = new ArrayList<>();
        int it = 0;
        ArrayList<Integer> h;

        abierto.add(new Node(clonar(matrizFichas, getApplicationContext())));  // Inicializar con una copia de la matriz original
        historial.add(new Node(clonar(matrizFichas, getApplicationContext())));

        while(true){

            if(isSolucion(abierto.get(0).data)){
                System.out.println("resuelto");
                cargarSol(abierto.get(0),matrizFichas);
                break;
            }else{
                mostrarV("antes",abierto);
                ArrayList<Node> aux=agregarChilds(abierto.get(0));
                abierto.remove(0);
                for(Node i:aux){
                    if(!isInHistorial(historial,i)){
                        abierto.add(i);
                    }
                }
                agregarHistorial(historial,aux);

                mostrarV("despues",abierto);
                h=calcularH(abierto,it);
              //  mostrar(abierto.get(0).data);
                for(int k=1;k<abierto.size();k++){
                    for(int l=0;l< abierto.size()-k;l++){
                        if(h.get(l)>h.get(l+1)){
                            Collections.swap(h,l,l+1);


                            Collections.swap(abierto,l,l+1);


                        }
                    }
                }
                mostrarV("despues2",abierto);


            }
            it++;
        }

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

    private void ordenar4x4(Fichas[][] matrizFichas) {
        int[][] initialBoard = new int[4][4];
        for (int i = 0; i < matrizFichas.length; i++) {
            for (int j = 0; j < matrizFichas[0].length; j++) {
                if (matrizFichas[i][j].getId() != 16) {
                    initialBoard[i][j] = matrizFichas[i][j].getId();
                } else {
                    initialBoard[i][j] = 0;  // La ficha en blanco
                }
            }
        }

        // Instancia de Python
        Python py = Python.getInstance();
        PyObject pyBoard = py.getModule("solve_puzzle").callAttr("solve_puzzle", (Object) initialBoard);

        // Si hay solución
        if (pyBoard != null) {
            // Convertir la solución obtenida desde Python a una lista de matrices int[][]
            List<int[][]> solutionBoards = new ArrayList<>();
            for (PyObject matrix : pyBoard.asList()) {
                int[][] boardState = new int[4][4];
                int row = 0;
                for (PyObject rowObject : matrix.asList()) {
                    boardState[row] = rowObject.toJava(int[].class);
                    row++;
                }
                solutionBoards.add(boardState);
            }

            // Ejecutar los pasos de la solución secuencialmente
            ejecutarPasosSecuencialmente(solutionBoards, matrizFichas);
        } else {
            Toast.makeText(this, "No se encontró una solución", Toast.LENGTH_SHORT).show();
        }

    }

    private void ejecutarPasosSecuencialmente(List<int[][]> solutionBoards, Fichas[][] matrizFichas) {
        Handler handler = new Handler();
        int delay = 500;  // Retraso de 500 ms entre pasos
        ejecutarSiguientePaso(0, solutionBoards, matrizFichas, handler, delay);
    }

    private void ejecutarSiguientePaso(int index, List<int[][]> solutionBoards, Fichas[][] matrizFichas, Handler handler, int delay) {
        if (index >= solutionBoards.size()) {
            return;  // No hay más pasos
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int[][] currentState = solutionBoards.get(index);

                // Posición actual de la ficha en blanco
                int[] posBlancaActual = encontrarX(matrizFichas);
                int ii = posBlancaActual[0], jj = posBlancaActual[1];

                // Encontrar la posición objetivo de la ficha en blanco
                int[] posBlancaObjetivo = new int[2];
                for (int i = 0; i < currentState.length; i++) {
                    for (int j = 0; j < currentState[i].length; j++) {
                        if (currentState[i][j] == 0) {
                            posBlancaObjetivo[0] = i;
                            posBlancaObjetivo[1] = j;
                            break;
                        }
                    }
                }

                // Determinar la dirección del movimiento
                String direccion = direccionCamino2(posBlancaActual, posBlancaObjetivo);
                Log.d("Rompecabezas", "a: " + ii + " " + jj);
                Log.d("Rompecabezas", "b: " + posBlancaObjetivo[0] + " " + posBlancaObjetivo[1]);

                boolean b = true;

                // Mover la ficha blanca en la dirección correcta
                if (direccion.equals("arriba") && ii > 0) {
                    ii--;
                } else if (direccion.equals("abajo") && ii < matrizFichas.length - 1) {
                    ii++;
                } else if (direccion.equals("izquierda") && jj > 0) {
                    jj--;
                } else if (direccion.equals("derecha") && jj < matrizFichas[0].length - 1) {
                    jj++;
                } else {
                    b = false;
                }

                if (b) {
                    intercambiarFichas(matrizFichas[ii][jj], matrizFichas[posBlancaActual[0]][posBlancaActual[1]]);
                }

                // Ejecutar el siguiente paso
                ejecutarSiguientePaso(index + 1, solutionBoards, matrizFichas, handler, delay);
            }
        }, delay);
    }


    private void intercambiarFichas(Fichas ficha1, Fichas ficha2) {
        if (!im) {
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
        } else {
            // Intercambiar imágenes de ImageViews
            Drawable drawable1 = ficha1.getImageView().getDrawable();
            Drawable drawable2 = ficha2.getImageView().getDrawable();
            int id1 = ficha1.getId();
            int id2 = ficha2.getId();

            ficha1.getImageView().setImageDrawable(drawable2);
            ficha2.getImageView().setImageDrawable(drawable1);
            ficha1.setId(id2);
            ficha2.setId(id1);
        }

    }

    private boolean isInHistorial(ArrayList<Node> historial, Node aux) {
        for(Node i:historial){
            if(isEqualsNode(i,aux)){
                return true;
            }
        }
        return false;
    }
    public boolean isEqualsNode(Node com,Node aux){
        for(int i=0;i<com.data.length;i++) {
            for (int j = 0; j < com.data[0].length; j++) {
                if(com.data[i][j].getId()!=aux.data[i][j].getId()){
                    return false;
                }
            }
        }
        return true;
    }

    public void mostrar(Fichas[][] m){
        System.out.println("jdfaj: \n");
        for(int i=0;i<m.length;i++){
            for(int j=0;j<m[0].length;j++){
                System.out.print(m[i][j].getId()+"\t");
            }
            System.out.println("\n");
        }
    }
    public void cargarSol(Node sol, Fichas[][] m) {
        ArrayList<Fichas[][]> cam = conseguirSol(sol, m);
        System.out.println("2");
        mostrarVd("sol: ", cam);
        Handler handler = new Handler();
        int delay = 500; // 500ms de retraso entre cada paso

        for (int index = 0; index < cam.size(); index++) {
            final int i = index;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fichas[][] currentState = cam.get(i);
                    int[] ad = encontrarX(m);
                    int ii = ad[0], jj = ad[1];
                    String direccion = direccionCamino(m, currentState);
                    System.out.println("direccion: " + direccion);

                    boolean b = true;

                    if (direccion.equals("arriba") && ii > 0) {
                        ii--;
                    } else if (direccion.equals("abajo") && ii < m.length - 1) {
                        ii++;
                    } else if (direccion.equals("izquierda") && jj > 0) {
                        jj--;
                    } else if (direccion.equals("derecha") && jj < m[0].length - 1) {
                        jj++;
                    } else {
                        b = false;
                    }

                    if (b) {
                        intercambiarFichas(m[ii][jj],m[ad[0]][ad[1]]);

                    }
                }
            }, delay * i);
        }


    }
    public String direccionCamino2(int[] posBlancaActual, int[] posBlancaObjetivo) {
        if (posBlancaActual[0] > posBlancaObjetivo[0]) {
            return "arriba";
        } else if (posBlancaActual[0] < posBlancaObjetivo[0]) {
            return "abajo";
        } else if (posBlancaActual[1] > posBlancaObjetivo[1]) {
            return "izquierda";
        } else if (posBlancaActual[1] < posBlancaObjetivo[1]) {
            return "derecha";
        }
        return "error";
    }

    public String direccionCamino(Fichas[][] m,Fichas[][] e){
        int[] a=encontrarX(m);
        int[] b=encontrarX(e);
        if((a[0]-1)==b[0]){
            return "arriba";
        }else if((a[0]+1)==b[0]){
            return "abajo";
        }else if((a[1]+1)==b[1]){
            return "derecha";
        }else if((a[1]-1)==b[1]){
            return "izquierda";
        }else{
            return "error";
        }
    }
    public ArrayList<Fichas[][]> conseguirSol(Node sol,Fichas[][] m){
        ArrayList<Fichas[][]> cam=new ArrayList<Fichas[][]>();
        Fichas[][] aux=sol.data;
        cam.add(sol.data);
        int maxIterations = 1000; // Ajusta según sea necesario
        int count = 0;
        while (true) {
            if (comparar(aux, m) || count >= maxIterations) {
                break;
            } else {
                sol = sol.next;
                aux = sol.data;
                cam.add(0, aux);
            }
            count++;
        }
        System.out.println("solucion1:");

        return cam;

    }
    public boolean comparar (Fichas[][] m1,Fichas[][] m2){
        for(int i=0;i<m1.length;i++){
            for(int j=0;j<m1[0].length;j++){
                if(m1[i][j].getId()!=m2[i][j].getId()){
                    return false;
                }
            }
        }
        return true;
    }
    public ArrayList<Integer> calcularH(ArrayList<Node> abi,int c){
        ArrayList<Integer> cal= new ArrayList<Integer>();
        for(Node i: abi){
            cal.add(i.calcularH4()+c);
        }
        return cal;
    }
    public void agregarHistorial (ArrayList<Node> h,ArrayList<Node> ch){
        for(Node i:ch){
            if(!isInHistorial(h,i)){
                h.add(i);

            }
        }

    }
    public ArrayList<Node> agregarChilds(Node m){
        ArrayList<Node> sol=new ArrayList<Node>();
        ArrayList<String> cam=camino(m.data);
        for (String i:cam){
            sol.add(child(i,m));
        }


        mostrarV("childs: ",sol);
        return  sol;
    }
    public Node child(String direccion, Node m){
        Fichas[][] matriz=clonar(m.data,getApplicationContext());
        System.out.println("origen: ");
        mostrar(matriz);
        int ii=0,jj=0;
        int[] ad=encontrarX(matriz);
        ii=ad[0];
        jj=ad[1];
        // Actualizamos la posición solo si es válido
        if (direccion.equals("arriba") && ii > 0) {
            ii--;
        } else if (direccion.equals("abajo") && ii < matriz.length - 1) {
            ii++;
        } else if (direccion.equals("izquierda") && jj > 0) {
            jj--;
        } else if (direccion.equals("derecha") && jj < matriz[0].length - 1) {
            jj++;
        }
        intercambiarFichas(matriz[ii][jj],matriz[ad[0]][ad[1]]);

        Node aux= new Node(matriz);
        aux.next=m;
        return aux;
    }
    public void mostrarVd(String t,ArrayList<Fichas[][]> sol){
        System.out.println(t+": \n");
        for(Fichas[][] i: sol){
            System.out.print("child "+sol.indexOf(i)+": ");
            mostrar(i);
        }

    }
    public void mostrarV(String t,ArrayList<Node> sol){
        System.out.println(t+": \n");
        for(Node i: sol){
            System.out.print("child "+sol.indexOf(i)+": ");
            mostrar(i.data);
        }

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

                if(!im){
                     texto= m[i][j].getTextView().getText().toString();
                     colorFondo = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getSolidColor()));
                     colorTexto = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getCurrentTextColor()));
                    width = m[i][j].getTextView().getLayoutParams().width;
                    height = m[i][j].getTextView().getLayoutParams().height;

                }else{
                    width = m[i][j].getImageView().getLayoutParams().width;
                    height = m[i][j].getImageView().getLayoutParams().height;
                }

                // Crear una nueva instancia de Fichas con los mismos valores
                if(!im){
                    clo[i][j] = new Fichas(context, texto, colorFondo, colorTexto, width, height, id);

                }else{
                    clo[i][j] = new Fichas(context, m[i][j].getImageView().getDrawable(), width, height, id);

                }
            }
        }
        return clo;
    }

    public boolean isSolucion(Fichas[][] estado ){
System.out.println("fadsfasd ");
mostrar(estado);
        for(int i=0;i<estado.length;i++){
            for(int j=0;j<estado[0].length;j++){
                if(estado[i][j].getId()!=((i*estado[0].length)+j+1)){
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
