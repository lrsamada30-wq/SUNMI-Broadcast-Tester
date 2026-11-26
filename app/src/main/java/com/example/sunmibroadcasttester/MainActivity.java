package com.example.sunmibroadcasttester;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private static final String ACTION_SCAN = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED";
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_RAW = "source_byte";

    private TextView statusView, lastCodeView, detailsView, historyView;
    private EditText actionInput;
    private BroadcastReceiver receiver;
    private boolean registered = false;
    private final StringBuilder history = new StringBuilder();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        createReceiver();
    }

    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private TextView text(String value, int size, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value); t.setTextSize(size); t.setTextColor(0xFF202124);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(5), 0, dp(5));
        return t;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(20));
        scroll.addView(root);

        TextView title = text("SUNMI Broadcast Tester", 23, true);
        root.addView(title);
        root.addView(text("Diagnóstico del lector láser mediante Broadcast Intent", 14, false));

        statusView = text("● RECEPTOR ACTIVO AL ABRIR LA PANTALLA", 14, true);
        statusView.setTextColor(0xFF16803C);
        root.addView(statusView);
        root.addView(text("ÚLTIMO CÓDIGO RECIBIDO", 13, true));
        lastCodeView = text("Esperando lectura…", 25, true);
        lastCodeView.setTextIsSelectable(true);
        root.addView(lastCodeView);

        root.addView(text("ACCIÓN BROADCAST", 13, true));
        actionInput = new EditText(this);
        actionInput.setSingleLine(true);
        actionInput.setText(ACTION_SCAN);
        actionInput.setTextSize(12);
        root.addView(actionInput, new LinearLayout.LayoutParams(-1, dp(52)));

        root.addView(text("DETALLES DEL INTENT", 13, true));
        detailsView = text("Todavía no se ha recibido ningún Intent.", 13, false);
        detailsView.setTextIsSelectable(true);
        detailsView.setBackgroundColor(0xFFF1F3F4);
        detailsView.setPadding(dp(10), dp(10), dp(10), dp(10));
        root.addView(detailsView);

        root.addView(text("HISTORIAL DE LECTURAS", 13, true));
        historyView = text("Sin lecturas.", 13, false);
        historyView.setTextIsSelectable(true);
        root.addView(historyView);

        LinearLayout row1 = new LinearLayout(this); row1.setOrientation(LinearLayout.HORIZONTAL);
        Button clear = new Button(this); clear.setText("Limpiar");
        clear.setOnClickListener(v -> { history.setLength(0); historyView.setText("Sin lecturas."); lastCodeView.setText("Esperando lectura…"); detailsView.setText("Historial limpiado."); });
        Button copy = new Button(this); copy.setText("Copiar detalles");
        copy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("SUNMI scan", detailsView.getText()));
            Toast.makeText(this, "Detalles copiados", Toast.LENGTH_SHORT).show();
        });
        row1.addView(clear, new LinearLayout.LayoutParams(0, -2, 1));
        row1.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row1);

        Button test = new Button(this); test.setText("Enviar Broadcast de prueba");
        test.setOnClickListener(v -> {
            Intent i = new Intent(actionInput.getText().toString().trim());
            i.putExtra(EXTRA_DATA, "TEST-SUNMI-1234567890");
            i.putExtra("test_event", true);
            sendBroadcast(i);
        });
        root.addView(test, new LinearLayout.LayoutParams(-1, -2));

        root.addView(text("Configuración en SUNMI", 16, true));
        root.addView(text("En la aplicación de escáner del terminal, selecciona salida Broadcast/Intent y desactiva la salida tipo teclado si deseas probar exclusivamente Broadcast. La acción debe coincidir con la indicada arriba. Esta aplicación escucha mientras está abierta en primer plano.", 13, false));
        root.addView(text("Acción: " + ACTION_SCAN + "\nExtra texto: data\nExtra bytes (si el firmware lo proporciona): source_byte", 12, false));

        setContentView(scroll);
    }

    private void createReceiver() {
        receiver = new BroadcastReceiver() {
            @Override public void onReceive(Context context, Intent intent) {
                String code = intent.getStringExtra(EXTRA_DATA);
                String stamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                if (code == null) code = "(el Intent no contiene el extra 'data')";
                lastCodeView.setText(code);

                StringBuilder d = new StringBuilder();
                d.append("Hora: ").append(stamp).append("\n");
                d.append("Action: ").append(intent.getAction()).append("\n");
                d.append("data: ").append(code).append("\n");
                Bundle extras = intent.getExtras();
                if (extras == null || extras.isEmpty()) {
                    d.append("Extras: ninguno\n");
                } else {
                    d.append("Extras recibidos:\n");
                    Set<String> keys = extras.keySet();
                    for (String key : keys) {
                        Object value;
                        try { value = extras.get(key); } catch (Exception e) { value = "<no legible>"; }
                        if (value instanceof byte[]) {
                            byte[] bytes = (byte[]) value;
                            StringBuilder hex = new StringBuilder();
                            for (byte b : bytes) hex.append(String.format(Locale.US, "%02X ", b & 0xff));
                            d.append("  ").append(key).append(" = byte[").append(bytes.length).append("]: ").append(hex).append("\n");
                        } else {
                            d.append("  ").append(key).append(" = ").append(String.valueOf(value)).append("\n");
                        }
                    }
                }
                detailsView.setText(d.toString());
                history.insert(0, stamp + "  " + code + "\n");
                if (history.length() > 6000) history.setLength(6000);
                historyView.setText(history.toString());
            }
        };
    }

    @Override protected void onResume() {
        super.onResume();
        if (!registered) {
            IntentFilter filter = new IntentFilter(ACTION_SCAN);
            registerReceiver(receiver, filter);
            registered = true;
            if (statusView != null) statusView.setText("● ESCUCHANDO: " + ACTION_SCAN);
        }
    }

    @Override protected void onPause() {
        if (registered) {
            try { unregisterReceiver(receiver); } catch (Exception ignored) {}
            registered = false;
        }
        super.onPause();
    }
}
