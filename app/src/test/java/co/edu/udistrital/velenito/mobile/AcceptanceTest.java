package co.edu.udistrital.velenito.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import co.edu.udistrital.velenito.mobile.data.ReceiptJson;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import co.edu.udistrital.velenito.mobile.ui.ReceiptView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AcceptanceTest {
    public static String sample(String status, String method) {
        return "{\"id\":\"venta-1\",\"fecha\":\"2026-11-26T15:00:00Z\",\"tienda\":\"Tienda El Veleñito\","
                + "\"metodo\":\"" + method + "\",\"estado\":\"" + status + "\",\"total\":1563,"
                + "\"cliente\":{\"nombre\":\"Cliente secreto\",\"documento\":\"123456789\",\"telefono\":\"3100000000\"},"
                + "\"precioCompra\":900,\"lineas\":[{\"nombre\":\"Café\",\"unidad\":\"kg\","
                + "\"cantidad\":0.125,\"subtotal\":1563,\"costoCompra\":700,\"precioCompra\":900}]}";
    }
    @Test public void accesoMuestraProductosCantidadesTotalYFormaDePagoSinLogin() {
        ReceiptView view = view(); view.show(ReceiptJson.parse(sample("confirmada", "nequi")));
        String content = text(view);
        assertTrue(content.contains("Café")); assertTrue(content.contains("0.125 kg"));
        assertTrue(content.contains("1.563")); assertTrue(content.contains("Forma de pago: Nequi"));
        assertFalse(content.contains("Contraseña")); assertTrue(content.contains("sin iniciar sesión"));
    }
    @Test public void privacidadFiadoIgnoraDatosDeClienteYCostosInclusoSiApiLosEnvia() {
        ReceiptView view = view(); view.show(ReceiptJson.parse(sample("confirmada", "fiado")));
        String content = text(view);
        assertTrue(content.contains("Fiado"));
        for (String secret : new String[]{"Cliente secreto", "123456789", "3100000000", "900", "700", "precioCompra", "costoCompra"})
            assertFalse(secret, content.contains(secret));
    }
    @Test public void anulacionMuestraAnuladaYConservaDetalleDelComprobante() {
        ReceiptView view = view(); view.show(ReceiptJson.parse(sample("anulada", "efectivo")));
        assertTrue(text(view).contains("Anulada")); assertTrue(text(view).contains("Café"));
    }
    @Test public void falloBorraElComprobanteAnteriorYOfreceErrorGenerico() {
        ReceiptView view = view(); view.show(ReceiptJson.parse(sample("confirmada", "fiado")));
        view.unavailable(); String content = text(view);
        assertTrue(content.contains(ReceiptLink.UNAVAILABLE)); assertFalse(content.contains("Café"));
        assertFalse(content.contains("token")); assertFalse(content.contains("404"));
    }
    private ReceiptView view() { Context context = RuntimeEnvironment.getApplication(); return new ReceiptView(context); }
    public static String text(View view) {
        StringBuilder result = new StringBuilder();
        if (view instanceof TextView) result.append(((TextView) view).getText()).append('\n');
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++)
            result.append(text(((ViewGroup) view).getChildAt(i)));
        return result.toString();
    }
}
