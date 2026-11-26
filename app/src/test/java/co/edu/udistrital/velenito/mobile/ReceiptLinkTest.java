package co.edu.udistrital.velenito.mobile;

import static org.junit.Assert.*;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import org.junit.Test;

public class ReceiptLinkTest {
    private static final String TOKEN = "a".repeat(64);
    @Test public void understandsExistingWebQrAndUsesSameOriginApi() {
        assertEquals("https://tienda.example/api/invoices/receipt/" + TOKEN,
                ReceiptLink.parse("https://tienda.example/#comprobante/" + TOKEN, "").endpoint().toString());
    }
    @Test public void supportsBackendLinksAndNativeDeepLinks() {
        assertEquals(TOKEN, ReceiptLink.parse("https://tienda.example/api/invoices/receipt/" + TOKEN, "").token);
        assertEquals("http://192.168.1.4:8080/api/invoices/receipt/" + TOKEN,
                ReceiptLink.parse("velenito://comprobante/" + TOKEN, "http://192.168.1.4:8080").endpoint().toString());
    }
    @Test public void configuredServerReplacesLocalhostFromDevelopmentQr() {
        assertEquals("192.168.1.4", ReceiptLink.parse("http://localhost:8081/#comprobante/" + TOKEN,
                "http://192.168.1.4:8081").origin.getHost());
    }
    @Test public void invalidLinksReturnSameGenericError() {
        for (String input : new String[]{"", "no-token", "javascript:alert(1)",
                "https://user:secret@tienda.example/#comprobante/" + TOKEN,
                "https://tienda.example/?otro=1#comprobante/" + TOKEN,
                "https://tienda.example/#comprobante/" + TOKEN + "/extra",
                "https://tienda.example/#comprobante/" + "x".repeat(64),
                "https://tienda.example/#comprobante/%61" + "a".repeat(63)}) {
            assertEquals(ReceiptLink.UNAVAILABLE, assertThrows(IllegalArgumentException.class,
                    () -> ReceiptLink.parse(input, "")).getMessage());
        }
    }
    @Test public void rawTokenNeedsExplicitServer() {
        assertThrows(IllegalArgumentException.class, () -> ReceiptLink.parse(TOKEN, ""));
        assertEquals(TOKEN, ReceiptLink.parse(TOKEN, "https://tienda.example").token);
    }
}
