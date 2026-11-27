package co.edu.udistrital.velenito.mobile;

import static org.junit.Assert.*;
import co.edu.udistrital.velenito.mobile.data.ReceiptClient;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ReceiptClientTest {
    @Test public void actualHttpGetUsesNoAuthenticationAndReadsPublicReceipt() throws Exception {
        try (var server = new TestServer(200, AcceptanceTest.sample("anulada", "fiado"))) {
            var link = ReceiptLink.parse("a".repeat(64), server.origin());
            var receipt = new ReceiptClient().load(link);
            assertTrue(receipt.cancelled()); assertEquals("fiado", receipt.payment);
            assertEquals("GET /api/invoices/receipt/" + "a".repeat(64) + " HTTP/1.1", server.request);
            assertNull(server.headers.get("authorization")); assertNull(server.headers.get("cookie"));
            assertEquals("no-store", server.headers.get("cache-control"));
        }
    }
    @Test public void expiredMissingAndRedirectedTokensHaveIdenticalErrors() throws Exception {
        for (int status : new int[]{400, 401, 404, 410, 302, 500}) {
            try (var server = new TestServer(status, "{}")) {
                var link = ReceiptLink.parse("a".repeat(64), server.origin());
                assertEquals(ReceiptLink.UNAVAILABLE, assertThrows(java.io.IOException.class,
                        () -> new ReceiptClient().load(link)).getMessage());
            }
        }
    }
}
