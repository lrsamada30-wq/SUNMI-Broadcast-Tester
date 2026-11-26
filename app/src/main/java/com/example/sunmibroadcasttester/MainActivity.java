package com.example.sunmibroadcasttester;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {

    private static final String ACTION_SCAN =
            "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED";
    private static final String EXTRA_DATA = "data";
    private static final String EXTRA_RAW = "source_byte";

    // SUNMI camera scanner integration documented by SUNMI.
    private static final String SUNMI_SCAN_ACTION = "com.summi.scan";
    private static final String SUNMI_SCAN_PACKAGE =
            "com.sunmi.sunmiqrcodescanner";
    private static final int REQUEST_SUNMI_CAMERA_SCAN = 1001;

    private TextView statusView;
    private TextView lastCodeView;
    private TextView detailsView;
    private TextView historyView;
    private EditText actionInput;

    private BroadcastReceiver receiver;
    private boolean registered = false;
    private final StringBuilder history = new StringBuilder();

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        buildUi();
        createReceiver();
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(0xFF202124);
        if (bold) {
            view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        view.setPadding(0, dp(5), 0, dp(5));
        return view;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        return button;
    }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(16), dp(18), dp(20));
        scroll.addView(root);

        root.addView(text("SUNMI Broadcast Tester", 23, true));
        root.addView(text(
                "Prueba el escáner de cámara SUNMI y la recepción por Broadcast",
                14, false));

        statusView = text("● RECEPTOR BROADCAST INICIANDO", 14, true);
        statusView.setTextColor(0xFF16803C);
        root.addView(statusView);

        Button cameraScan = makeButton("ABRIR ESCÁNER DE CÁMARA SUNMI");
        cameraScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openSunmiCameraScanner();
            }
        });
        root.addView(cameraScan, new LinearLayout.LayoutParams(-1, -2));

        root.addView(text("ÚLTIMO CÓDIGO RECIBIDO", 13, true));
        lastCodeView = text("Esperando lectura…", 25, true);
        lastCodeView.setTextIsSelectable(true);
        root.addView(lastCodeView);

        root.addView(text("ACCIÓN BROADCAST", 13, true));
        actionInput = new EditText(this);
        actionInput.setSingleLine(true);
        actionInput.setInputType(InputType.TYPE_CLASS_TEXT);
        actionInput.setText(ACTION_SCAN);
        actionInput.setTextSize(12);
        root.addView(actionInput, new LinearLayout.LayoutParams(-1, dp(52)));

        root.addView(text("DETALLES DEL INTENT / ESCANEO", 13, true));
        detailsView = text("Todavía no se ha recibido ningún resultado.", 13, false);
        detailsView.setTextIsSelectable(true);
        detailsView.setBackgroundColor(0xFFF1F3F4);
        detailsView.setPadding(dp(10), dp(10), dp(10), dp(10));
        root.addView(detailsView);

        root.addView(text("HISTORIAL DE LECTURAS", 13, true));
        historyView = text("Sin lecturas.", 13, false);
        historyView.setTextIsSelectable(true);
        root.addView(historyView);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        Button clear = makeButton("Limpiar");
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                history.setLength(0);
                historyView.setText("Sin lecturas.");
                lastCodeView.setText("Esperando lectura…");
                detailsView.setText("Historial limpiado.");
            }
        });

        Button copy = makeButton("Copiar detalles");
        copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager manager =
                        (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                manager.setPrimaryClip(
                        ClipData.newPlainText("SUNMI scan", detailsView.getText()));
                Toast.makeText(MainActivity.this,
                        "Detalles copiados", Toast.LENGTH_SHORT).show();
            }
        });

        row.addView(clear, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        root.addView(row);

        Button test = makeButton("Enviar Broadcast de prueba");
        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String action = actionInput.getText().toString().trim();
                if (action.length() == 0) {
                    Toast.makeText(MainActivity.this,
                            "Introduce una acción Broadcast",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(action);
                intent.setPackage(getPackageName());
                intent.putExtra(EXTRA_DATA, "TEST-SUNMI-1234567890");
                intent.putExtra("test_event", true);
                sendBroadcast(intent);
            }
        });
        root.addView(test, new LinearLayout.LayoutParams(-1, -2));

        root.addView(text("Integración de cámara SUNMI", 16, true));
        root.addView(text(
                "El botón superior abre el escáner integrado de SUNMI mediante "
                + "la acción com.summi.scan. El resultado se recibe en "
                + "onActivityResult(). Esta función es independiente del receptor "
                + "Broadcast.",
                13, false));

        root.addView(text("Broadcast SUNMI", 16, true));
        root.addView(text(
                "Action: " + ACTION_SCAN
                + "\nExtra texto: data"
                + "\nExtra bytes opcional: source_byte",
                12, false));

        setContentView(scroll);
    }

    private void openSunmiCameraScanner() {
        Intent intent = new Intent(SUNMI_SCAN_ACTION);
        intent.setPackage(SUNMI_SCAN_PACKAGE);

        // Explicit defaults documented by SUNMI; settings and album are visible.
        intent.putExtra("PLAY_SOUND", true);
        intent.putExtra("PLAY_VIBRATE", false);
        intent.putExtra("IDENTIFY_MORE_CODE", false);
        intent.putExtra("IS_SHOW_SETTING", true);
        intent.putExtra("IS_SHOW_ALBUM", true);

        try {
            startActivityForResult(intent, REQUEST_SUNMI_CAMERA_SCAN);
            detailsView.setText("Escáner de cámara SUNMI abierto. Esperando resultado…");
        } catch (ActivityNotFoundException exception) {
            detailsView.setText(
                    "No se encontró el escáner integrado de SUNMI.\n"
                    + "Paquete requerido: " + SUNMI_SCAN_PACKAGE
                    + "\nAcción: " + SUNMI_SCAN_ACTION
                    + "\nComprueba si el componente está instalado en este firmware.");
            Toast.makeText(this,
                    "No está instalado el escáner integrado SUNMI",
                    Toast.LENGTH_LONG).show();
        } catch (Exception exception) {
            detailsView.setText("No se pudo abrir el escáner SUNMI: "
                    + exception.getClass().getSimpleName()
                    + ": " + exception.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != REQUEST_SUNMI_CAMERA_SCAN) {
            return;
        }

        if (data == null) {
            detailsView.setText(
                    "El escáner SUNMI terminó sin devolver datos. "
                    + "ResultCode: " + resultCode);
            return;
        }

        Bundle bundle = data.getExtras();
        if (bundle == null) {
            detailsView.setText(
                    "El escáner SUNMI devolvió un Intent sin extras. "
                    + "ResultCode: " + resultCode);
            return;
        }

        Object resultObject;
        try {
            resultObject = bundle.getSerializable("data");
        } catch (Exception exception) {
            detailsView.setText("No se pudo leer el extra 'data': "
                    + exception.getMessage());
            return;
        }

        if (resultObject instanceof ArrayList) {
            ArrayList<?> results = (ArrayList<?>) resultObject;
            if (results.size() == 0) {
                detailsView.setText("El escáner no devolvió códigos. "
                        + "ResultCode: " + resultCode);
                return;
            }

            StringBuilder allResults = new StringBuilder();
            for (Object item : results) {
                if (item instanceof HashMap) {
                    HashMap<?, ?> map = (HashMap<?, ?>) item;
                    String type = String.valueOf(map.get("TYPE"));
                    Object valueObject = map.get("VALUE");
                    String value = valueObject == null
                            ? "" : String.valueOf(valueObject);
                    appendScanResult(type, value, "SUNMI camera scanner");
                    if (allResults.length() > 0) {
                        allResults.append("\n\n");
                    }
                    allResults.append("TYPE: ").append(type)
                            .append("\nVALUE: ").append(value);
                } else {
                    String value = String.valueOf(item);
                    appendScanResult("UNKNOWN", value, "SUNMI camera scanner");
                    if (allResults.length() > 0) {
                        allResults.append("\n\n");
                    }
                    allResults.append(value);
                }
            }
            detailsView.setText(
                    "Resultado recibido mediante onActivityResult()\n"
                    + "ResultCode: " + resultCode + "\n"
                    + allResults.toString());
            return;
        }

        // Fallback for firmware versions returning a String instead of ArrayList.
        if (resultObject instanceof String) {
            String value = (String) resultObject;
            appendScanResult("UNKNOWN", value, "SUNMI camera scanner");
            detailsView.setText(
                    "Resultado recibido mediante onActivityResult()\n"
                    + "VALUE: " + value);
            return;
        }

        detailsView.setText(
                "El escáner devolvió un formato no reconocido en el extra 'data'.\n"
                + "Tipo: "
                + (resultObject == null
                    ? "null" : resultObject.getClass().getName())
                + "\nExtras disponibles: " + bundle.keySet().toString());
    }

    private void appendScanResult(String type, String code, String source) {
        String stamp = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        lastCodeView.setText(code);
        history.insert(0, stamp + " [" + source + "] [" + type + "] "
                + code + "\n");

        if (history.length() > 6000) {
            history.setLength(6000);
        }
        historyView.setText(history.toString());
    }

    private void createReceiver() {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String code = intent.getStringExtra(EXTRA_DATA);
                String stamp = new SimpleDateFormat(
                        "HH:mm:ss", Locale.getDefault()).format(new Date());

                if (code == null) {
                    code = "(el Intent no contiene el extra 'data')";
                }

                lastCodeView.setText(code);

                StringBuilder details = new StringBuilder();
                details.append("Origen: Broadcast\n")
                        .append("Hora: ").append(stamp).append("\n")
                        .append("Action: ").append(intent.getAction()).append("\n")
                        .append("data: ").append(code).append("\n");

                Bundle extras = intent.getExtras();
                if (extras == null || extras.isEmpty()) {
                    details.append("Extras: ninguno\n");
                } else {
                    details.append("Extras recibidos:\n");
                    Set<String> keys = extras.keySet();
                    for (String key : keys) {
                        Object value;
                        try {
                            value = extras.get(key);
                        } catch (Exception exception) {
                            value = "<no legible>";
                        }

                        if (value instanceof byte[]) {
                            byte[] bytes = (byte[]) value;
                            StringBuilder hex = new StringBuilder();
                            for (byte b : bytes) {
                                hex.append(String.format(
                                        Locale.US, "%02X ", b & 0xff));
                            }
                            details.append("  ").append(key)
                                    .append(" = byte[")
                                    .append(bytes.length).append("]: ")
                                    .append(hex).append("\n");
                        } else {
                            details.append("  ").append(key)
                                    .append(" = ")
                                    .append(String.valueOf(value)).append("\n");
                        }
                    }
                }

                detailsView.setText(details.toString());
                history.insert(0, stamp + " [Broadcast] " + ccode + "\n");
                if (history.length() > 6000) {
                    history.setLength(6000);
                }
                historyView.setText(history.toString());
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!registered) {
            IntentFilter filter = new IntentFilter(ACTION_SCAN);
            registerReceiver(receiver, filter);
            registered = true;
            if (statusView != null) {
                statusView.setText("● ESCUCHANDO BROADCAST: " + ACTION_SCAN);
            }
        }
    }

    @Override
    protected void onPause() {
        if (registered) {
            try {
                unregisterReceiver(receiver);
            } catch (Exception ignored) {
            }
            registered = false;
        }
        super.onPause();
    }
}
