package com.example.rompecabezas;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Rompecabezas extends AppCompatActivity {
    private Fichas[][] matrizFichas;
    LinearLayout lr;
    Button btResolver,btDesordenar;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rompecabezas);
        lr = findViewById(R.id.lyVerticalO);
        btResolver=findViewById(R.id.btnResolver);
        btDesordenar=findViewById(R.id.btnDesordenar);

        // Obtener los datos del Intent
        Intent data = getIntent();
        String filas = data.getStringExtra("filas");
        String columnas = data.getStringExtra("columnas");
        String nombre = data.getStringExtra("nombre");
        String nivel= data.getStringExtra("dificultad");
        int f = Integer.parseInt(filas);
        int c = Integer.parseInt(columnas);
        Log.i("cec","ce:"+nivel);
        matrizFichas = new Fichas[f][c];

        for (int i = 0; i < f; i++) {
            // Crear un nuevo LinearLayout para cada fila
            LinearLayout aux = createHorizontalLinearLayout(getApplicationContext());

            for (int j = 0; j < c; j++) {
                String texto = getTexto(i, j); // Define el texto basado en la posición
                String colorFondo = getColorFondo(i, j); // Define el color de fondo basado en la posición
                matrizFichas[i][j] = new Fichas(getApplicationContext(),  (i==f-1 && j==c-1)? "X": texto, colorFondo, (i==f-1 && j==c-1)? "#000000" : "#FFFFFF",dpToPx(80),dpToPx(80),(i*c)+j+1);

                aux.addView(matrizFichas[i][j].getTextView());
                if (j < c - 1) {  // Agregar espacio entre TextViews, excepto después del último
                    aux.addView(spaceVertical());
                }
            }

            lr.addView(aux);

            if (i < f - 1) {  // Agregar espacio entre filas, excepto después de la última fila
                lr.addView(createVerticalSpace(getApplicationContext()));
            }
        }
        System.out.println("Inicio: ");
        mostrar(matrizFichas);
        for (int i = 0; i < f; i++) {
            // Crear un nuevo LinearLayout para cada fila
            final int ii=i;
            for (int j = 0; j < c; j++) {
                final int jj=j;
                matrizFichas[ii][jj].getTextView().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Fichas auxtext = null;
                        int mov=move(matrizFichas,ii,jj);
                        if(mov!=-1){
                            if(mov==1){
                                auxtext=matrizFichas[ii-1][jj];
                            }else if(mov==2){
                                auxtext=matrizFichas[ii+1][jj];
                            }else if(mov ==3){
                                auxtext=matrizFichas[ii][jj-1];
                            }else {
                                auxtext=matrizFichas[ii][jj+1];
                            }
                            String txt = matrizFichas[ii][jj].getTextView().getText().toString();
                            Drawable txtcolor = matrizFichas[ii][jj].getTextView().getBackground();
                            int textcolord = matrizFichas[ii][jj].getTextView().getCurrentTextColor();
                            int id=matrizFichas[ii][jj].getId();
                            matrizFichas[ii][jj].getTextView().setText(auxtext.getTextView().getText().toString());
                            matrizFichas[ii][jj].getTextView().setBackground(auxtext.getTextView().getBackground());
                            matrizFichas[ii][jj].getTextView().setTextColor(auxtext.getTextView().getCurrentTextColor());
                            matrizFichas[ii][jj].setId(auxtext.getId());
                            auxtext.getTextView().setText(txt);
                            auxtext.setId(id);
                            auxtext.getTextView().setBackground(txtcolor);
                            auxtext.getTextView().setTextColor(textcolord);
                        }


                    }
                });
            }
        }
        btResolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordenar(matrizFichas);

            }
        });
        btDesordenar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                desordenar(nivel,matrizFichas);
            }
        });
    }
    public void ordenar(Fichas[][] matrizFichas){
        ArrayList<Node> abierto=new ArrayList<Node>();
        ArrayList<Node> historial =new ArrayList<Node>();
        int it=0;
        ArrayList<Integer> h;
        abierto.add(new Node(matrizFichas));
        historial.add(new Node(matrizFichas));
        while(true){

            if(isSolucion(abierto.get(0).data)){
                System.out.println("resuelto");
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
                mostrar(abierto.get(0).data);
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
        cargarSol(abierto.get(0),matrizFichas);
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
                        Fichas auxtext = m[ii][jj];
                        String txt = m[ad[0]][ad[1]].getTextView().getText().toString();
                        Drawable txtcolor = m[ad[0]][ad[1]].getTextView().getBackground();
                        int textcolord = m[ad[0]][ad[1]].getTextView().getCurrentTextColor();
                        int id = m[ad[0]][ad[1]].getId();

                        m[ad[0]][ad[1]].getTextView().setText(auxtext.getTextView().getText().toString());
                        m[ad[0]][ad[1]].getTextView().setBackground(auxtext.getTextView().getBackground());
                        m[ad[0]][ad[1]].getTextView().setTextColor(auxtext.getTextView().getCurrentTextColor());
                        m[ad[0]][ad[1]].setId(auxtext.getId());

                        auxtext.getTextView().setText(txt);
                        auxtext.getTextView().setBackground(txtcolor);
                        auxtext.getTextView().setTextColor(textcolord);
                        auxtext.setId(id);
                    }
                }
            }, delay * i);
        }
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

        Fichas auxtext = matriz[ii][jj];
        String txt = matriz[ad[0]][ad[1]].getTextView().getText().toString();
        Drawable txtcolor = matriz[ad[0]][ad[1]].getTextView().getBackground();
        int textcolord = matriz[ad[0]][ad[1]].getTextView().getCurrentTextColor();
        int id=matriz[ad[0]][ad[1]].getId();
        matriz[ad[0]][ad[1]].getTextView().setText(auxtext.getTextView().getText().toString());
        matriz[ad[0]][ad[1]].getTextView().setBackground(auxtext.getTextView().getBackground());
        matriz[ad[0]][ad[1]].getTextView().setTextColor(auxtext.getTextView().getCurrentTextColor());
        matriz[ad[0]][ad[1]].setId(auxtext.getId());

        auxtext.getTextView().setText(txt);
        auxtext.getTextView().setBackground(txtcolor);
        auxtext.getTextView().setTextColor(textcolord);
        auxtext.setId(id);
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
                String texto = m[i][j].getTextView().getText().toString();
                int id = m[i][j].getId();
                String colorFondo = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getSolidColor()));
                String colorTexto = String.format("#%06X", (0xFFFFFF & m[i][j].getTextView().getCurrentTextColor()));
                int width = m[i][j].getTextView().getLayoutParams().width;
                int height = m[i][j].getTextView().getLayoutParams().height;

                // Crear una nueva instancia de Fichas con los mismos valores
                clo[i][j] = new Fichas(context, texto, colorFondo, colorTexto, width, height, id);
            }
        }
        return clo;
    }

    public boolean isSolucion(Fichas[][] estado ){

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
    public int[] encontrarX(Fichas[][] fichas){
        int ii=0,jj=0;
        for(int i =0;i<fichas.length;i++){
            for (int j=0;j<fichas[0].length;j++){
                if(fichas[i][j].getTextView().getText().toString()=="X"){
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
    public void desordenar(String nivel, Fichas[][] matriz) {
        int can = (nivel.equals("Facil")) ? 10 : (nivel.equals("Medio")) ? 20 : 30;

        System.out.println("desordenar");
        for (int i = 0; i < can; i++) {
            int[] ad = encontrarX(matriz);
            int ii = ad[0];
            int jj = ad[1];
            ArrayList<String> posibles = camino(matriz);
            Random random = new Random();
            int numeroAleatorio = random.nextInt(posibles.size()); // Genera un número entre 0 y posibles.size() - 1
            String direccion = posibles.get(numeroAleatorio);

            // Actualizamos la posición solo si es válido
            if (direccion.equals("arriba") && ii > 0) {
                ii--;
            } else if (direccion.equals("abajo") && ii < matriz.length - 1) {
                ii++;
            } else if (direccion.equals("izquierda") && jj > 0) {
                jj--;
            } else if (direccion.equals("derecha") && jj < matriz[0].length - 1) {
                jj++;
            } else {
                continue; // Si no es una dirección válida, saltamos la iteración
            }

            Fichas auxtext = matriz[ii][jj];
            String txt = matriz[ad[0]][ad[1]].getTextView().getText().toString();
            Drawable txtcolor = matriz[ad[0]][ad[1]].getTextView().getBackground();
            int textcolord = matriz[ad[0]][ad[1]].getTextView().getCurrentTextColor();
            int id=matriz[ad[0]][ad[1]].getId();
            matriz[ad[0]][ad[1]].getTextView().setText(auxtext.getTextView().getText().toString());
            matriz[ad[0]][ad[1]].getTextView().setBackground(auxtext.getTextView().getBackground());
            matriz[ad[0]][ad[1]].getTextView().setTextColor(auxtext.getTextView().getCurrentTextColor());
            matriz[ad[0]][ad[1]].setId(auxtext.getId());

            auxtext.getTextView().setText(txt);
            auxtext.getTextView().setBackground(txtcolor);
            auxtext.getTextView().setTextColor(textcolord);
            auxtext.setId(id);
            mostrar(matriz);

        }
    }

    public int move(Fichas[][] mat ,int i,int j ){
        int posible=-1;
        if(i>0){
            posible = (mat[i-1][j].getTextView().getText().toString()=="X")?1:posible;
        }
        if(i<(mat.length-1)){
            posible= (mat[i+1][j].getTextView().getText().toString()=="X")?2:posible;
        }
        if(j>0){
            posible= (mat[i][j-1].getTextView().getText().toString()=="X")?3:posible;
        }
        if(j<(mat[0].length-1)){
            posible= (mat[i][j+1].getTextView().getText().toString()=="X")?4:posible;
        }
        return posible;
    }


    private Space createVerticalSpace(Context context) {
        Space sp = new Space(context);

        // Configura las dimensiones del Space
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // wrap_content
                dpToPx(5) // 5dp convertido a píxeles
        );
        sp.setLayoutParams(params);

        return sp;
    }

    private LinearLayout createHorizontalLinearLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);

        // Configura las dimensiones y orientación del LinearLayout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, // wrap_content
                ViewGroup.LayoutParams.WRAP_CONTENT  // wrap_content
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

        // Configura las dimensiones del Space
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(5), // 5dp convertido a píxeles
                ViewGroup.LayoutParams.WRAP_CONTENT // wrap_content
        );
        sp.setLayoutParams(params);

        return sp;
    }

    // Método para obtener el color de fondo basado en la posición
    private String getColorFondo(int fila, int columna) {
        // Total de fichas en la matriz
        int totalFichas = matrizFichas.length * matrizFichas[0].length;

        // Generador de colores vivos
        String[] coloresVivos = {
                "#FF5733", "#FF8D33", "#FFC300", "#DAF7A6",
                "#33FF57", "#33FF8D", "#33FFC3", "#33DAFF",
                "#338DFF", "#5733FF", "#8D33FF", "#C300FF",
                "#FF33DA", "#FF3399", "#FF3366", "#FF3333"
        };

        // Calcula la posición actual en la matriz
        int posicion = fila * matrizFichas[0].length + columna;

        // Si es la última posición, retorna blanco
        if (posicion == totalFichas - 1) {
            return "#FFFFFF";
        }

        // Genera un color vivo en base a la posición
        return coloresVivos[posicion % coloresVivos.length];
    }

}
