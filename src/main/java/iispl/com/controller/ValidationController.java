package iispl.com.controller;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import iispl.com.component.DataTable;
import iispl.com.component.SummaryCard;
import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class ValidationController extends SelectorComposer<Component> {

    // DataTable macro component (resolved via getFellow after compose)
    private DataTable   validationTable;
    private Listbox     tableList;

    // SummaryCard macro components (resolved via recursive search)
    private SummaryCard totalCard;
    private SummaryCard validCard;
    private SummaryCard invalidCard;

    private final CheckService service = new CheckService();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        validationTable = (DataTable) comp.getFellow("validationTable");
        tableList       = (validationTable != null) ? validationTable.getTableList() : null;

        totalCard   = findSummaryCard(comp, "totalCard");
        validCard   = findSummaryCard(comp, "validCard");
        invalidCard = findSummaryCard(comp, "invalidCard");

        loadData();
    }

    // ── Run Validation ────────────────────────────────────────────────────
    @Listen("onClick = #validateBtn")
    public void validateChecks() throws Exception {
        List<Check> all = service.getAllChecks();
        if (all == null || all.isEmpty()) {
            Clients.showNotification("No checks found. Please add checks first.",
                    "warning", null, "top_center", 3000);
            return;
        }
        service.validateChecks();
        Clients.showNotification("Validation completed", "info", null, "top_right", 2500);
        loadData();
    }

    // ── Proceed to Batch ──────────────────────────────────────────────────
    @Listen("onClick = #proceedBatchBtn")
    public void proceedToBatch() {
        List<Check> valid = service.getValidChecks();
        if (valid == null || valid.isEmpty()) {
            Clients.showNotification("No valid checks. Run validation first.",
                    "error", null, "top_center", 3000);
            return;
        }
        org.zkoss.zk.ui.Executions.sendRedirect("/zul/batchcreations.zul");
    }

    // ── Load / Render ─────────────────────────────────────────────────────
    private void loadData() throws Exception {
        if (tableList == null) return;
        tableList.getItems().clear();

        List<Check> checks = service.getAllChecks();
        int total = 0, valid = 0, invalid = 0;

        if (checks != null) {
            for (Check c : checks) {
                total++;
                String status = (c.getStatus() == null) ? "pending" : c.getStatus();

                Listitem item = new Listitem();
                item.setStyle("border-bottom:1px solid #f1f5f9;");

                addCell(item, safe(c.getCheckNo()));
                addCell(item, safe(c.getAccountNo()));
                addCell(item, "₹" + String.format("%,.0f", c.getAmount()));
                addBadgeCell(item, status);

                // Validation message with icon
                String msg  = (c.getValidationMsg() == null || c.getValidationMsg().isEmpty())
                              ? "Not validated yet" : c.getValidationMsg();
                String icon = "valid".equalsIgnoreCase(status)   ? "✓ "
                            : "invalid".equalsIgnoreCase(status) ? "✗ " : "";
                String col  = "valid".equalsIgnoreCase(status)   ? "color:#16a34a;"
                            : "invalid".equalsIgnoreCase(status) ? "color:#dc2626;"
                            : "color:#64748b;";
                Label msgLbl = new Label(icon + msg);
                msgLbl.setStyle(col + "font-size:13px;");
                Listcell mc = new Listcell();
                mc.setStyle("padding:12px 16px;");
                mc.appendChild(msgLbl);
                item.appendChild(mc);

                tableList.appendChild(item);

                if ("valid".equalsIgnoreCase(status))        valid++;
                else if ("invalid".equalsIgnoreCase(status)) invalid++;
            }
        }

        setSummaryCard(totalCard,   String.valueOf(total));
        setSummaryCard(validCard,   String.valueOf(valid));
        setSummaryCard(invalidCard, String.valueOf(invalid));

        if (validationTable != null) {
            validationTable.setTableCaption("Validation Results (" + total + ")");
            validationTable.refresh();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private void addCell(Listitem item, String text) {
        Listcell cell = new Listcell(text);
        cell.setStyle("padding:12px 16px;font-size:14px;color:#1e293b;");
        item.appendChild(cell);
    }

    private void addBadgeCell(Listitem item, String status) {
        Label lbl = new Label(status);
        lbl.setStyle(badge(status));
        Listcell cell = new Listcell();
        cell.setStyle("padding:12px 16px;");
        cell.appendChild(lbl);
        item.appendChild(cell);
    }

    private String badge(String s) {
        if (s == null) s = "pending";
        switch (s.toLowerCase()) {
            case "valid":   return "background:#dcfce7;color:#16a34a;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "invalid": return "background:#fee2e2;color:#dc2626;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            default:        return "background:#f1f5f9;color:#64748b;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
        }
    }

    private void setSummaryCard(SummaryCard card, String value) {
        if (card != null) card.setCardValue(value);
    }

    private String safe(String s) { return s == null ? "" : s; }

    private SummaryCard findSummaryCard(Component root, String id) {
        if (id.equals(root.getId()) && root instanceof SummaryCard) return (SummaryCard) root;
        for (Component child : root.getChildren()) {
            SummaryCard f = findSummaryCard(child, id);
            if (f != null) return f;
        }
        return null;
    }
}
