package com.example.rompecabezas.Views;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.rompecabezas.R;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private Button btnIniciar,btndos,btntres;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configurar Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar el DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Agregar el toggle (botón de menú)
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Configurar botón Iniciar
        btnIniciar = findViewById(R.id.btnIniciar);
        btnIniciar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a FilasColumnas Activity
                Intent intent = new Intent(MainActivity.this, FilasColumnas.class);
                startActivity(intent);
            }
        });
        btndos = findViewById(R.id.btnDos);
        btndos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a FilasColumnas Activity
                Intent intent = new Intent(MainActivity.this, JuegoDoble.class);
                startActivity(intent);
            }
        });
        btntres = findViewById(R.id.btnTres);
        btntres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navegar a FilasColumnas Activity
                Intent intent = new Intent(MainActivity.this, JuegadorContraIA.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_historial) {
            // Abrir la actividad de historial
            startActivity(new Intent(this, TiemposActivity.class));

        } else if (id == R.id.nav_profile) {
            // Abrir la actividad de perfil
            startActivity(new Intent(this, ProfileActivity.class));

        } else if (id == R.id.nav_login_logout) {
            // Manejar el login o logout
            // Agrega aquí la lógica para iniciar sesión o cerrar sesión

        } else if (id == R.id.nav_sign_up) {
            // Abrir la actividad de registro
            startActivity(new Intent(this, SignUpActivity.class));
        }

        // Cerrar el Drawer después de seleccionar una opción
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        // Si el drawer está abierto, ciérralo al presionar "Atrás"
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
