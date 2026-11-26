package co.edu.udistrital.velenito.mobile.data;

import co.edu.udistrital.velenito.mobile.domain.Receipt;
import co.edu.udistrital.velenito.mobile.domain.ReceiptLink;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;

public final class ReceiptJson {
    public static Receipt parse(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String status = text(json, "estado");
            String method = text(json, "metodo");
            if (!Arrays.asList("confirmada", "anulada").contains(status)
                    || !Arrays.asList("efectivo", "nequi", "daviplata", "fiado").contains(method)) throw new Exception();
            Instant.parse(text(json, "fecha"));
            JSONArray array = json.getJSONArray("lineas");
            if (array.length() == 0 || array.length() > 1000) throw new Exception();
            ArrayList<Receipt.Line> lines = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                BigDecimal quantity = amount(item, "cantidad");
                if (quantity.signum() <= 0) throw new Exception();
                lines.add(new Receipt.Line(text(item, "nombre"), text(item, "unidad"),
                        quantity, amount(item, "subtotal")));
            }
            return new Receipt(text(json, "id"), text(json, "fecha"), text(json, "tienda"),
                    method, status, amount(json, "total"), lines);
        } catch (Exception ex) { throw new IllegalArgumentException(ReceiptLink.UNAVAILABLE); }
    }
    private static String text(JSONObject object, String key) throws Exception {
        Object value = object.get(key);
        if (!(value instanceof String) || ((String) value).trim().isEmpty() || ((String) value).length() > 500)
            throw new Exception();
        return (String) value;
    }
    private static BigDecimal amount(JSONObject object, String key) throws Exception {
        BigDecimal value = new BigDecimal(object.get(key).toString());
        if (value.signum() < 0 || value.precision() > 20 || Math.abs(value.scale()) > 8) throw new Exception();
        return value;
    }
}
