package co.edu.udistrital.velenito.mobile.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import co.edu.udistrital.velenito.mobile.BuildConfig;
import co.edu.udistrital.velenito.mobile.data.ReceiptClient;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class MainActivity extends ComponentActivity {
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private int generation;
    private EditText input;
    private ReceiptView receipt;
    private TextView serverLabel;
    private Button open;
    private String server;
    private final ActivityResultLauncher<ScanOptions> scanner = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) { input.setText(result.getContents()); load(); }
    });

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        server = getPreferences(MODE_PRIVATE).getString("server", "");
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        LinearLayout page = new LinearLayout(this); page.setOrientation(LinearLayout.VERTICAL);
        page.setBackgroundColor(Color.rgb(243, 246, 250));
        page.setPadding(dp(20), dp(20), dp(20), dp(20));
        page.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(dp(20) + insets.getSystemWindowInsetLeft(), dp(20) + insets.getSystemWindowInsetTop(),
                    dp(20) + insets.getSystemWindowInsetRight(), dp(20) + insets.getSystemWindowInsetBottom());
            return insets;
        });
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.addView(page); setContentView(scroll);
        TextView title = new TextView(this); title.setText("El Veleñito"); title.setTextSize(32);
        title.setTextColor(Color.rgb(18, 44, 81)); page.addView(title);
        TextView intro = new TextView(this); intro.setText("Tus comprobantes, en tu celular.\nEscanea un QR o abre el enlace que te entregó la tienda.");
        intro.setTextSize(17); intro.setPadding(0, dp(12), 0, dp(16)); page.addView(intro);
        button(page, "Escanear QR", () -> scanner.launch(new ScanOptions().setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                .setPrompt("Apunta al QR del comprobante").setBeepEnabled(false).setOrientationLocked(false)));
        input = field("Enlace del comprobante"); page.addView(input);
        open = button(page, "Abrir comprobante", this::load);
        serverLabel = new TextView(this); serverLabel.setTextSize(13); page.addView(serverLabel); updateServerLabel();
        button(page, "Configurar servidor", this::settings);
        receipt = new ReceiptView(this); page.addView(receipt);
        accept(getIntent());
    }
    @Override protected void onNewIntent(Intent intent) { super.onNewIntent(intent); setIntent(intent); accept(intent); }
    private void accept(Intent intent) {
        if (intent == null) return;
        String link = Intent.ACTION_VIEW.equals(intent.getAction()) ? intent.getDataString()
                : Intent.ACTION_SEND.equals(intent.getAction()) ? intent.getStringExtra(Intent.EXTRA_TEXT) : null;
        if (link != null) { input.setText(link); load(); }
    }
    private void load() {
        int request = ++generation;
        final ReceiptLink link;
        try {
            link = ReceiptLink.parse(input.getText().toString(), server);
            if (!BuildConfig.DEBUG && !"https".equals(link.origin.getScheme())) throw new IllegalArgumentException();
        } catch (Exception ex) { open.setEnabled(true); receipt.unavailable(); return; }
        serverLabel.setText("Consultando: " + link.origin);
        receipt.loading(); open.setEnabled(false);
        worker.execute(() -> {
            try {
                var result = new ReceiptClient().load(link);
                runOnUiThread(() -> { if (!isDestroyed() && request == generation) { receipt.show(result); open.setEnabled(true); } });
            } catch (Exception ex) {
                runOnUiThread(() -> { if (!isDestroyed() && request == generation) { receipt.unavailable(); open.setEnabled(true); } });
            }
        });
    }
    private void settings() {
        EditText field = field("https://tu-servidor o http://IP-del-PC:8081"); field.setText(server);
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle("Servidor de comprobantes")
                .setMessage("Déjalo vacío para usar el servidor del enlace QR. En un celular, localhost apunta al propio teléfono. Usa la IP del computador y el puerto del frontend (8081) o del backend (8080).")
                .setView(field).setNegativeButton("Cancelar", null).setPositiveButton("Guardar", null).create();
        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            try {
                String value = field.getText().toString().trim();
                if (!value.isEmpty()) {
                    var uri = ReceiptLink.server(value);
                    if (!BuildConfig.DEBUG && !"https".equals(uri.getScheme())) throw new IllegalArgumentException();
                    value = uri.toString();
                }
                server = value; getPreferences(MODE_PRIVATE).edit().putString("server", server).apply();
                ++generation; open.setEnabled(true); receipt.removeAllViews(); updateServerLabel(); dialog.dismiss();
            } catch (Exception ex) { field.setError(BuildConfig.DEBUG ? "Usa una dirección http(s) sin rutas, usuario ni contraseña." : "Usa una dirección HTTPS sin rutas, usuario ni contraseña."); }
        })); dialog.show();
    }
    private void updateServerLabel() { serverLabel.setText(server.isEmpty() ? "Servidor: el indicado en el enlace" : "Servidor: " + server); }
    private EditText field(String hint) {
        EditText field = new EditText(this); field.setHint(hint);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        field.setAutofillHints((String[]) null); field.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        field.setSaveEnabled(false); field.setMaxLines(3); return field;
    }
    private Button button(LinearLayout parent, String title, Runnable action) {
        Button button = new Button(this); button.setText(title); button.setAllCaps(false);
        button.setTextSize(17); button.setOnClickListener(v -> action.run());
        parent.addView(button, new LinearLayout.LayoutParams(-1, -2)); return button;
    }
    @Override protected void onDestroy() { ++generation; worker.shutdownNow(); super.onDestroy(); }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
