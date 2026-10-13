package com.edupilot.solicitudes;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/** Segunda pantalla: muestra las características de la solicitud actual. */
public class CaracteristicasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_caracteristicas);

        // La toolbar se registra como barra de la actividad
        Toolbar toolbar = findViewById(R.id.toolbarCaracteristicas);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.action_caracteristicas);
            // Flecha "atrás" (usa parentActivityName del manifest)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Datos enviados desde MainActivity mediante extras del Intent
        String titulo = getIntent().getStringExtra("titulo");
        String descripcion = getIntent().getStringExtra("descripcion");
        int total = getIntent().getIntExtra("total", 0);
        int atendidas = getIntent().getIntExtra("atendidas", 0);

        String texto = getString(R.string.detalle_formato,
                vacioPorGuion(titulo), vacioPorGuion(descripcion),
                total, atendidas, total - atendidas);
        ((TextView) findViewById(R.id.tvDetalle)).setText(texto);
    }

    private String vacioPorGuion(String s) {
        return (s == null || s.trim().isEmpty()) ? "—" : s;
    }
}