package com.example.rompecabezas.Views;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.rompecabezas.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FilasColumnas extends AppCompatActivity {

    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_PERMISSIONS = 102;
    private Uri imageUri;

    Button btndecrementarcolumnas, btnincrementarcolumnas, btndecrementarfilas, btnincrementarfilas, btnenviar, btnlimpiar, btnSeleccionarImagen;
    EditText etfilas, etcoulmnas, etnombre;
    Spinner spinnerDificultad;
    RadioGroup rgOpciones;
    RadioButton rbLetras, rbNumeros, rbImagen;
    ImageView ivVistaPrevia;
    Bitmap imagenSeleccionada = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filas_columnas);

        // Referencia a los elementos del layout
        etfilas = findViewById(R.id.etFilasfilas);
        etcoulmnas = findViewById(R.id.etColumnas);
        etnombre = findViewById(R.id.etNombre);
        btndecrementarcolumnas = findViewById(R.id.btnDecrementarColumnas);
        btnincrementarcolumnas = findViewById(R.id.btnIncrementarColumnas);
        btndecrementarfilas = findViewById(R.id.btnDecrementarFilas);
        btnincrementarfilas = findViewById(R.id.btnIncrementarFilas);
        btnenviar = findViewById(R.id.btInvocar);
        btnlimpiar = findViewById(R.id.btLimpiar);
        spinnerDificultad = findViewById(R.id.spinnerDificultad);
        rgOpciones = findViewById(R.id.rgOpciones);
        rbLetras = findViewById(R.id.rbLetras);
        rbNumeros = findViewById(R.id.rbNumeros);
        rbImagen = findViewById(R.id.rbImagen);
        btnSeleccionarImagen = findViewById(R.id.btnSeleccionarImagen);

        ivVistaPrevia = findViewById(R.id.ivVistaPrevia);

        // Configura el Spinner con las opciones de dificultad
        String[] dificultades = {"Facil", "Medio", "Dificil"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, dificultades);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDificultad.setAdapter(adapter);

        // Listener para el RadioGroup
        rgOpciones.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbImagen) {
                btnSeleccionarImagen.setVisibility(View.VISIBLE);
                ivVistaPrevia.setVisibility(View.VISIBLE);
            } else {
                btnSeleccionarImagen.setVisibility(View.GONE);
                ivVistaPrevia.setVisibility(View.GONE);
            }
        });

        // Configura el botón para seleccionar imagen de la galería
        btnSeleccionarImagen.setOnClickListener(v -> {
            requestPermissionsIfNecessary();
            showImagePickerDialog();
        });




        // Configura el botón de limpiar
        btnlimpiar.setOnClickListener(v -> {
            etfilas.setText("2");
            etcoulmnas.setText("2");
            etnombre.setText(null);
            rgOpciones.clearCheck(); // Limpiar la selección del RadioGroup
            ivVistaPrevia.setImageBitmap(null);
            imagenSeleccionada = null;
        });

        // Configura botones de incrementar y decrementar filas y columnas
        configurarBotonesFilasColumnas();

        // Configura el botón de enviar
        btnenviar.setOnClickListener(v -> {
            procesarEnvioDatos();
        });
    }

    // Método para manejar el procesamiento y envío de datos
    private void procesarEnvioDatos() {
        String nombre = etnombre.getText().toString();
        String seleccion = rbLetras.isChecked() ? "Letras" : rbNumeros.isChecked() ? "Numeros" : "Imagen";
        String filas = etfilas.getText().toString();
        String columnas = etcoulmnas.getText().toString();

        if (rbLetras.isChecked() && nombre.isEmpty()) {
            Toast.makeText(FilasColumnas.this, "Error, ingresar nombre", Toast.LENGTH_SHORT).show();
        } else if (rbImagen.isChecked() && imagenSeleccionada == null) {
            Toast.makeText(FilasColumnas.this, "Por favor, selecciona una imagen.", Toast.LENGTH_SHORT).show();
        } else {
            // Guardar los datos en SharedPreferences
            guardarDatosEnPreferencias("filas", filas);
            guardarDatosEnPreferencias("columnas", columnas);
            guardarDatosEnPreferencias("nombre", nombre);
            guardarDatosEnPreferencias("nivel", spinnerDificultad.getSelectedItem().toString());
            guardarDatosEnPreferencias("seleccion", seleccion);

            if (rbImagen.isChecked()) {
                // Guardar la imagen en almacenamiento interno y obtener la ruta
                String rutaImagen = guardarImagenEnAlmacenamientoInterno(imagenSeleccionada, "imagen_seleccionada");
                guardarDatosEnPreferencias("rutaImagen", rutaImagen);  // Guardar la ruta de la imagen
            }

            // Iniciar la actividad Rompecabezas
            Intent invocador = new Intent(FilasColumnas.this, Rompecabezas.class);
            startActivity(invocador);
        }
    }

    // Método para verificar y solicitar los permisos necesarios
    private void requestPermissionsIfNecessary() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA}, REQUEST_PERMISSIONS);
        } else {
            dispatchTakePictureIntent();
        }
    }

    // Método que muestra el selector de imagen o cámara
    private void showImagePickerDialog() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent chooser = Intent.createChooser(galleryIntent, "Selecciona una opción");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{cameraIntent});

        startActivityForResult(chooser, PICK_IMAGE);
    }

    // Método para manejar la captura de imagen desde la cámara
    // Método para manejar la captura de imagen desde la cámara
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Crear un archivo donde se almacenará la imagen
            File photoFile = null;
            try {
                photoFile = createImageFile();  // Crear archivo de imagen
            } catch (IOException ex) {
                ex.printStackTrace();
                Toast.makeText(this, "Error al crear archivo para la imagen", Toast.LENGTH_SHORT).show();
                return;  // Salir si no se puede crear el archivo
            }

            if (photoFile != null) {
                // Obtener la URI del archivo con el FileProvider
                imageUri = FileProvider.getUriForFile(this, "com.example.rompecabezas.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);  // Poner la URI en el Intent
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    // Método para crear un archivo temporal donde se guardará la imagen capturada
    // Método para crear un archivo temporal donde se guardará la imagen capturada
    private File createImageFile() throws IOException {
        // Crear un nombre de archivo único con la fecha y hora
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        // Directorio donde se almacenarán las imágenes
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        // Crear el archivo temporal de la imagen
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        return image;  // Devolver el archivo de imagen
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            // Manejar la selección de imagen desde la galería
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();
                try {
                    imagenSeleccionada = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    ivVistaPrevia.setImageBitmap(imagenSeleccionada);
                    ajustarFilasColumnas(imagenSeleccionada);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen seleccionada", Toast.LENGTH_SHORT).show();
                }
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Verificar si se ha capturado la imagen correctamente
            if (imageUri != null) {
                try {
                    // Cargar la imagen desde la URI generada
                    imagenSeleccionada = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    ivVistaPrevia.setImageBitmap(imagenSeleccionada);

                    // Verificación de carga exitosa
                    Toast.makeText(this, "Imagen capturada cargada correctamente", Toast.LENGTH_SHORT).show();

                    ajustarFilasColumnas(imagenSeleccionada);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Error al cargar la imagen capturada", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Error: URI de la imagen es nula", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Configura los botones de incrementar y decrementar filas y columnas
    private void configurarBotonesFilasColumnas() {
        btndecrementarcolumnas.setOnClickListener(v -> {
            if (Integer.parseInt(etcoulmnas.getText().toString()) > 2) {
                etcoulmnas.setText((Integer.parseInt(etcoulmnas.getText().toString()) - 1) + "");
            } else {
                Toast.makeText(FilasColumnas.this, "No se pueden usar menos de dos columnas", Toast.LENGTH_SHORT).show();
            }
        });

        btnincrementarcolumnas.setOnClickListener(v -> {
            if (Integer.parseInt(etcoulmnas.getText().toString()) < 4) {
                etcoulmnas.setText((Integer.parseInt(etcoulmnas.getText().toString()) + 1) + "");
            } else {
                Toast.makeText(FilasColumnas.this, "No se pueden usar más de 4 columnas", Toast.LENGTH_SHORT).show();
            }
        });

        btndecrementarfilas.setOnClickListener(v -> {
            if (Integer.parseInt(etfilas.getText().toString()) > 2) {
                etfilas.setText((Integer.parseInt(etfilas.getText().toString()) - 1) + "");
            } else {
                Toast.makeText(FilasColumnas.this, "No se pueden usar menos de dos filas", Toast.LENGTH_SHORT).show();
            }
        });

        btnincrementarfilas.setOnClickListener(v -> {
            if (Integer.parseInt(etfilas.getText().toString()) < 4) {
                etfilas.setText((Integer.parseInt(etfilas.getText().toString()) + 1) + "");
            } else {
                Toast.makeText(FilasColumnas.this, "No se pueden usar más de 4 filas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Método para ajustar el número de filas y columnas según la orientación de la imagen
    private void ajustarFilasColumnas(Bitmap imagen) {
        int width = imagen.getWidth();
        int height = imagen.getHeight();

        if (width > height) {
            etcoulmnas.setText("4");
            etfilas.setText("3");
        } else {
            etcoulmnas.setText("3");
            etfilas.setText("4");
        }
    }

    // Método para guardar datos en SharedPreferences
    private void guardarDatosEnPreferencias(String key, String value) {
        getSharedPreferences("prefs", MODE_PRIVATE)
                .edit()
                .putString(key, value)
                .apply();
    }

    // Método para guardar una imagen en almacenamiento interno
    private String guardarImagenEnAlmacenamientoInterno(Bitmap imagen, String nombreImagen) {
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        File directory = cw.getDir("imagenes", Context.MODE_PRIVATE);
        File file = new File(directory, nombreImagen + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(file)) {
            imagen.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.close();
            Toast.makeText(this, "Imagen guardada en: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();  // Mensaje de confirmación
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file.getAbsolutePath();  // Devolver la ruta de la imagen guardada
    }

}
