package iispl.com.controller;


import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Vlayout;

import iispl.com.component.DataTable;
import iispl.com.component.SummaryCard;
import iispl.com.model.Batch;
import iispl.com.model.Check;
import iispl.com.service.BatchService;
import iispl.com.service.CheckService;

public class BatchController extends SelectorComposer<Component> {

    // DataTable macro component
    private DataTable batchTable;
    private Listbox   tableList;

    // SummaryCard macro components
    private SummaryCard validCountCard, selectedCountCard, approvedCountCard, batchAmountCard;

    // @Wire-able ZUL components
    @Wire private Vlayout successBox;
    @Wire private Label   batchNoLbl, totalChecksLbl, totalAmtLbl;
    @Wire private Label   validCount, selectedCount, approvedCount, batchAmount;

    private final CheckService checkService = new CheckService();
    private final BatchService batchService = new BatchService();
    private List<Check>  validChecks;
    private boolean      batchAlreadyCreated = false;
    private Batch        lastBatch;

    private static final double LIMIT = 5_000_000.0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        batchTable = (DataTable) comp.getFellow("batchTable");
        tableList  = (batchTable != null) ? batchTable.getTableList() : null;

        validCountCard    = findSummaryCard(comp, "validCountCard");
        selectedCountCard = findSummaryCard(comp, "selectedCountCard");
        approvedCountCard = findSummaryCard(comp, "approvedCountCard");
        batchAmountCard   = findSummaryCard(comp, "batchAmountCard");

        loadData();
    }

    // ── Load ──────────────────────────────────────────────────────────────
    private void loadData() {
        if (tableList == null) return;
        tableList.getItems().clear();

        validChecks = checkService.getValidChecks();

        if (validChecks == null || validChecks.isEmpty()) {
            Clients.showNotification("No valid checks found. Please complete validation first.",
                    "warning", null, "top_center", 3000);
            setCard(validCountCard,  validCount,  "0");
            setCard(batchAmountCard, batchAmount, "₹0");
            if (batchTable != null) batchTable.refresh();
            return;
        }

        double totalAmt = 0;
        for (Check c : validChecks) {
            Listitem item = new Listitem();
            item.setValue(c);
            item.setStyle("border-bottom:1px solid #f1f5f9;");

            item.appendChild(new Listcell(""));   // checkbox column (ZK renders the checkbox)
            addCell(item, safe(c.getCheckNo()));
            addCell(item, safe(c.getAccountNo()));
            addCell(item, "₹" + String.format("%,.0f", c.getAmount()));
            addCell(item, safe(c.getBankName()));
            addBadgeCell(item, safe(c.getStatus()));

            tableList.appendChild(item);
            if (c.getAmount() != null) totalAmt += c.getAmount();
        }

        setCard(validCountCard,    validCount,    String.valueOf(validChecks.size()));
        setCard(batchAmountCard,   batchAmount,   "₹" + String.format("%,.0f", totalAmt));
        setCard(selectedCountCard, selectedCount, "0");
        setCard(approvedCountCard, approvedCount, "0");

        if (batchTable != null) {
            batchTable.setTableCaption("Valid Checks (" + validChecks.size() + ")");
            batchTable.refresh();
        }
    }

    // ── Select All ────────────────────────────────────────────────────────
    @Listen("onClick = #selectAllBtn")
    public void selectAll() {
        if (tableList == null || tableList.getItems().isEmpty()) {
            Clients.showNotification("No checks available.", "warning", null, "top_center", 2000);
            return;
        }
        for (Listitem item : tableList.getItems()) {
            Check c = (Check) item.getValue();
            item.setSelected(c != null && "valid".equalsIgnoreCase(c.getStatus()));
        }
        updateSelection();
    }

    @Listen("onSelect = #batchTable")   // fires when inner listbox selection changes
    public void updateSelection() {
        if (tableList == null) return;
        setCard(selectedCountCard, selectedCount,
                String.valueOf(tableList.getSelectedItems().size()));
    }

    // ── Approve ───────────────────────────────────────────────────────────
    @Listen("onClick = #approveBtn")
    public void approveSelected() {
        if (tableList == null || tableList.getSelectedItems().isEmpty()) {
            Clients.showNotification("Please select at least one check to approve.",
                    "warning", null, "top_center", 2500);
            return;
        }
        int n = tableList.getSelectedItems().size();
        setCard(approvedCountCard, approvedCount, String.valueOf(n));
        Clients.showNotification(n + " check(s) approved", "info", null, "top_right", 2000);
    }

    // ── Create Batch ──────────────────────────────────────────────────────
    @Listen("onClick = #createBatchBtn")
    public void createBatch() {
        if (batchAlreadyCreated) {
            Clients.showNotification("A batch has already been created.",
                    "error", null, "top_center", 3000); return;
        }
        int approved = 0;
        try { approved = Integer.parseInt(approvedCount != null ? approvedCount.getValue() : "0"); }
        catch (NumberFormatException e) { approved = 0; }
        if (approved == 0) {
            Clients.showNotification("Please approve at least one check before creating a batch.",
                    "warning", null, "top_center", 2500); return;
        }

        double totalAmt = 0;
        List<Check> selected = new ArrayList<>();
        for (Listitem item : tableList.getSelectedItems()) {
            Check c = (Check) item.getValue();
            if (c != null) {
                if (!"valid".equalsIgnoreCase(c.getStatus())) {
                    Clients.showNotification("Only valid checks can be batched.",
                            "error", null, "top_center", 3000); return;
                }
                totalAmt += c.getAmount();
                selected.add(c);
            }
        }
        if (totalAmt > LIMIT) {
            Clients.showNotification(
                    String.format("Batch amount ₹%,.0f exceeds limit of ₹%,.0f.", totalAmt, LIMIT),
                    "error", null, "top_center", 4000); return;
        }
        if (selected.isEmpty()) {
            Clients.showNotification("No checks selected.", "error", null, "top_center", 2500); return;
        }

        try {
            lastBatch = batchService.createBatch(selected);
            if (lastBatch == null) {
                Clients.showNotification("Batch creation failed.", "error", null, "top_center", 3000); return;
            }
            batchAlreadyCreated = true;
            loadData();
            if (successBox    != null) successBox.setVisible(true);
            if (batchNoLbl    != null) batchNoLbl.setValue(lastBatch.getBatchNo());
            if (totalChecksLbl!= null) totalChecksLbl.setValue(String.valueOf(lastBatch.getTotalChecks()));
            if (totalAmtLbl   != null) totalAmtLbl.setValue(String.format("₹%,.0f", lastBatch.getTotalAmount()));
            Clients.showNotification("Batch " + lastBatch.getBatchNo() + " created successfully with "
                    + lastBatch.getTotalChecks() + " checks", "info", null, "top_right", 3000);
        } catch (Exception e) {
            Clients.showNotification("Error creating batch: " + e.getMessage(),
                    "error", null, "top_center", 4000);
        }
    }

    @Listen("onClick = #npciBtn")
    public void goToNpci() {
        if (lastBatch != null)
            Executions.getCurrent().getSession().setAttribute("currentBatch", lastBatch);
        Executions.sendRedirect("/zul/npciclearing.zul");
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
            case "valid":    return "background:#dcfce7;color:#16a34a;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "approved": return "background:#dbeafe;color:#2563eb;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "invalid":  return "background:#fee2e2;color:#dc2626;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            default:         return "background:#f1f5f9;color:#64748b;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
        }
    }

    private void setCard(SummaryCard card, Label fallback, String value) {
        if (card     != null) card.setCardValue(value);
        if (fallback != null) fallback.setValue(value);
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
