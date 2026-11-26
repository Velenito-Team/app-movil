package co.edu.udistrital.velenito.mobile.ui;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import co.edu.udistrital.velenito.mobile.domain.Receipt;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Native text views: untrusted API content is never interpreted as HTML. */
public final class ReceiptView extends LinearLayout {
    private static final int NAVY = Color.rgb(18, 44, 81);
    public ReceiptView(Context context) {
        super(context); setOrientation(VERTICAL); setPadding(dp(20), dp(16), dp(20), dp(20));
        setBackgroundColor(Color.WHITE); setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        setSaveEnabled(false);
    }
    public void loading() { removeAllViews(); text("Consultando comprobante…", 20, NAVY); }
    public void unavailable() {
        removeAllViews(); text(ReceiptLink.UNAVAILABLE, 22, NAVY);
        text("Revisa el enlace y la conexión, o solicita un nuevo comprobante a la tienda.", 16, Color.DKGRAY);
    }
    public void show(Receipt receipt) {
        removeAllViews();
        text(receipt.store, 25, NAVY);
        text("Comprobante de venta", 19, NAVY);
        text(receipt.cancelled() ? "Anulada" : "Confirmada", 22,
                receipt.cancelled() ? Color.rgb(160, 32, 32) : Color.rgb(25, 108, 70));
        if (receipt.cancelled()) text("Esta venta fue anulada.", 16, Color.DKGRAY);
        text(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", new Locale("es", "CO"))
                .withZone(ZoneId.of("America/Bogota")).format(Instant.parse(receipt.date)), 16, Color.DKGRAY);
        String method = switch (receipt.payment) {
            case "fiado" -> "Fiado"; case "nequi" -> "Nequi";
            case "daviplata" -> "Daviplata"; default -> "Efectivo";
        };
        text("Forma de pago: " + method, 18, NAVY);
        text("Productos", 21, NAVY);
        for (Receipt.Line line : receipt.lines) {
            text(line.name, 19, NAVY);
            text("Cantidad: " + line.quantity.stripTrailingZeros().toPlainString() + " " + line.unit
                    + "    Subtotal: " + money(line.subtotal), 16, Color.DKGRAY);
        }
        text("Total: " + money(receipt.total), 26, NAVY);
        text("Referencia: " + receipt.id, 13, Color.GRAY);
        text("Puedes consultar este comprobante sin iniciar sesión.", 15, Color.DKGRAY);
    }
    private void text(String value, int size, int color) {
        TextView view = new TextView(getContext()); view.setText(value); view.setTextSize(size);
        view.setTextColor(color); view.setPadding(0, dp(7), 0, dp(7)); view.setTextIsSelectable(true);
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }
    private String money(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("es", "CO"));
        format.setCurrency(java.util.Currency.getInstance("COP")); format.setMaximumFractionDigits(0);
        return format.format(amount) + " COP";
    }
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
}
