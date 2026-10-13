package com.edupilot.solicitudes;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

/** Actividad principal: registro de solicitudes. */
public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "solicitudes_canal";

    private SolicitudStore store;
    private List<Solicitud> solicitudes;
    private ArrayAdapter<Solicitud> adapter;
    private EditText etTitulo;
    private EditText etDescripcion;

    // Android 13+ exige pedir el permiso POST_NOTIFICATIONS en tiempo de ejecución
    private final ActivityResultLauncher<String> permisoNotificaciones =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    mostrarNotificacion();
                } else {
                    Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar como barra de la aplicación (el tema debe ser NoActionBar)
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etTitulo = findViewById(R.id.etTitulo);
        etDescripcion = findViewById(R.id.etDescripcion);
        Button btnAgregar = findViewById(R.id.btnAgregar);
        Button btnCompartir = findViewById(R.id.btnCompartir);
        ListView lista = findViewById(R.id.lista);

        crearCanalNotificaciones();

        store = new SolicitudStore(this);
        solicitudes = store.load();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, solicitudes);
        lista.setAdapter(adapter);

        btnAgregar.setOnClickListener(v -> {
            String titulo = etTitulo.getText().toString().trim();
            if (titulo.isEmpty()) {
                etTitulo.setError(getString(R.string.error_titulo));
                return;
            }
            solicitudes.add(new Solicitud(titulo, etDescripcion.getText().toString().trim(), false));
            persistirYRefrescar();
            etTitulo.setText("");
            etDescripcion.setText("");
        });

        btnCompartir.setOnClickListener(v -> compartirFormulario());

        lista.setOnItemClickListener((parent, view, position, id) -> {
            Solicitud s = solicitudes.get(position);
            s.atendida = !s.atendida;
            persistirYRefrescar();
        });

        lista.setOnItemLongClickListener((parent, view, position, id) -> {
            solicitudes.remove(position);
            persistirYRefrescar();
            Toast.makeText(this, R.string.eliminada, Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    // ---------- Menú de la toolbar ----------

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Infla los botones definidos en res/menu/menu_main.xml
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_caracteristicas) {
            verCaracteristicas();
            return true;
        } else if (id == R.id.action_notificacion) {
            crearNotificacion();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------- Ver características (intent explícito) ----------

    private void verCaracteristicas() {
        int atendidas = 0;
        for (Solicitud s : solicitudes) {
            if (s.atendida) atendidas++;
        }
        Intent intent = new Intent(this, CaracteristicasActivity.class);
        intent.putExtra("titulo", etTitulo.getText().toString().trim());
        intent.putExtra("descripcion", etDescripcion.getText().toString().trim());
        intent.putExtra("total", solicitudes.size());
        intent.putExtra("atendidas", atendidas);
        startActivity(intent);
    }

    // ---------- Compartir (intent implícito ACTION_SEND) ----------

    private void compartirFormulario() {
        String titulo = etTitulo.getText().toString().trim();
        if (titulo.isEmpty()) {
            etTitulo.setError(getString(R.string.error_titulo));
            return;
        }
        String texto = getString(R.string.compartir_formato,
                titulo, etDescripcion.getText().toString().trim());

        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.compartir_asunto));
        send.putExtra(Intent.EXTRA_TEXT, texto);
        // El chooser deja al usuario elegir la app (WhatsApp, correo, etc.)
        startActivity(Intent.createChooser(send, getString(R.string.compartir_con)));
    }

    // ---------- Notificación ----------

    private void crearCanalNotificaciones() {
        // Desde Android 8 (API 26) toda notificación necesita un canal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel canal = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.canal_nombre),
                    NotificationManager.IMPORTANCE_DEFAULT);
            getSystemService(NotificationManager.class).createNotificationChannel(canal);
        }
    }

    private void crearNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        mostrarNotificacion();
    }

    private void mostrarNotificacion() {
        int pendientes = 0;
        for (Solicitud s : solicitudes) {
            if (!s.atendida) pendientes++;
        }

        // Al tocar la notificación se abre la app
        Intent abrir = new Intent(this, MainActivity.class);
        android.app.PendingIntent pendiente = android.app.PendingIntent.getActivity(
                this, 0, abrir,
                android.app.PendingIntent.FLAG_IMMUTABLE | android.app.PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(getString(R.string.notif_titulo))
                .setContentText(getString(R.string.notif_texto, pendientes))
                .setContentIntent(pendiente)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(this).notify(1, builder.build());
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.permiso_denegado, Toast.LENGTH_SHORT).show();
        }
    }

    private void persistirYRefrescar() {
        store.save(solicitudes);
        adapter.notifyDataSetChanged();
    }
}