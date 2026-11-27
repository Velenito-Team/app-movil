package co.edu.udistrital.velenito.mobile.domain;

import java.math.BigDecimal;
import java.util.List;

/** Public allowlist: no customer identity or purchase cost can enter the view model. */
public final class Receipt {
    public final String id, date, store, payment, status;
    public final BigDecimal total;
    public final List<Line> lines;

    public Receipt(String id, String date, String store, String payment, String status,
            BigDecimal total, List<Line> lines) {
        this.id = id; this.date = date; this.store = store; this.payment = payment;
        this.status = status; this.total = total;
        this.lines = java.util.Collections.unmodifiableList(new java.util.ArrayList<>(lines));
    }
    public boolean cancelled() { return "anulada".equals(status); }
    public static final class Line {
        public final String name, unit;
        public final BigDecimal quantity, subtotal;
        public Line(String name, String unit, BigDecimal quantity, BigDecimal subtotal) {
            this.name = name; this.unit = unit; this.quantity = quantity; this.subtotal = subtotal;
        }
    }
}
