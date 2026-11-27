package co.edu.udistrital.velenito.mobile;

import static org.junit.Assert.*;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import co.edu.udistrital.velenito.mobile.ui.MainActivity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {
    @Test public void opensSharedLinkAndShowsCancelledFiadoWithoutPrivateData() throws Exception {
        var server = new TestServer(200, AcceptanceTest.sample("anulada", "fiado"));
        var intent = new android.content.Intent(android.content.Intent.ACTION_SEND)
                .setType("text/plain").putExtra(android.content.Intent.EXTRA_TEXT,
                        server.origin() + "/#comprobante/" + "a".repeat(64));
        var controller = Robolectric.buildActivity(MainActivity.class, intent).setup();
        try {
            View root = controller.get().findViewById(android.R.id.content);
            long deadline = System.nanoTime() + 10_000_000_000L;
            while (!AcceptanceTest.text(root).contains("Esta venta fue anulada.") && System.nanoTime() < deadline) {
                Thread.sleep(25); Shadows.shadowOf(Looper.getMainLooper()).idle();
            }
            String text = AcceptanceTest.text(root);
            assertTrue(text, text.contains("Esta venta fue anulada.")); assertTrue(text.contains("Café"));
            assertTrue(text.contains("0.125 kg")); assertTrue(text.contains("Forma de pago: Fiado"));
            assertFalse(text.contains("Cliente secreto")); assertFalse(text.contains("precioCompra"));
            EditText input = findInput(root); input.setText("token-invalido"); findOpen(root).performClick();
            assertTrue(AcceptanceTest.text(root).contains("Comprobante no disponible."));
            assertFalse(AcceptanceTest.text(root).contains("Café"));
        } finally { controller.pause().stop().destroy(); server.close(); }
    }
    private static EditText findInput(View view) {
        if (view instanceof EditText) return (EditText) view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            EditText found = findInput(((ViewGroup) view).getChildAt(i)); if (found != null) return found;
        }
        return null;
    }
    private static Button findOpen(View view) {
        if (view instanceof Button && "Abrir comprobante".contentEquals(((Button) view).getText())) return (Button) view;
        if (view instanceof ViewGroup) for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
            Button found = findOpen(((ViewGroup) view).getChildAt(i)); if (found != null) return found;
        }
        return null;
    }
}
