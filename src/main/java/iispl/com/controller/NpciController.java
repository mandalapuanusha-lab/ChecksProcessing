package iispl.com.controller;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import iispl.com.component.DataTable;
import iispl.com.component.SummaryCard;
import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class NpciController extends SelectorComposer<Component> {

    // DataTable macro component
    private DataTable npciTable;
    private Listbox   tableList;

    // SummaryCard macro components
    private SummaryCard npciTotalCard, npciPassCard, npciFailCard, npciAmtCard;

    @Wire private Vlayout npciSuccessBox;
    @Wire private Label   npciSuccessDetail;

    private final CheckService checkService = new CheckService();
    private List<Check> approvedChecks = new ArrayList<>();
    private boolean     npciValidated  = false;
    private boolean     submitted      = false;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        npciTable = (DataTable) comp.getFellow("npciTable");
        tableList = (npciTable != null) ? npciTable.getTableList() : null;

        npciTotalCard = findSummaryCard(comp, "npciTotalCard");
        npciPassCard  = findSummaryCard(comp, "npciPassCard");
        npciFailCard  = findSummaryCard(comp, "npciFailCard");
        npciAmtCard   = findSummaryCard(comp, "npciAmtCard");

        loadData();
    }

    private void loadData() {
        List<Check> all = checkService.getAllChecks();
        approvedChecks.clear();
        for (Check c : all) {
            if ("approved".equalsIgnoreCase(c.getStatus()) ||
                "batched".equalsIgnoreCase(c.getStatus())) {
                approvedChecks.add(c);
            }
        }
        renderTable(false);
        updateCards(false);
    }

    // ── Render table rows ─────────────────────────────────────────────────
    private void renderTable(boolean validated) {
        if (tableList == null) return;
        tableList.getItems().clear();

        for (Check c : approvedChecks) {
            Listitem item = new Listitem();
            item.setValue(c);
            item.setStyle("border-bottom:1px solid #f1f5f9;");

            addCell(item, safe(c.getCheckNo()));
            addCell(item, safe(c.getAccountNo()));
            addCell(item, "₹" + String.format("%,.0f", c.getAmount()));
            addCell(item, safe(c.getBankName()));

            // NPCI Status badge
            String npciStatus = validated ? npciCheck(c) : "pending";
            Label badge = new Label(npciStatus);
            badge.setStyle(npciBadge(npciStatus));
            Listcell sc = new Listcell();
            sc.setStyle("padding:12px 16px;");
            sc.appendChild(badge);
            item.appendChild(sc);

            // Issues column
            String issue = validated
                    ? ("passed".equals(npciStatus) ? "✓ Ready for clearing" : "✗ " + npciIssue(c))
                    : "-";
            Label issueLbl = new Label(issue);
            issueLbl.setStyle("passed".equals(npciStatus)
                    ? "color:#16a34a;font-size:13px;"
                    : "color:#dc2626;font-size:13px;");
            Listcell ic = new Listcell();
            ic.setStyle("padding:12px 16px;");
            ic.appendChild(issueLbl);
            item.appendChild(ic);

            tableList.appendChild(item);
        }

        if (npciTable != null) {
            npciTable.setTableCaption("Checks for NPCI Submission (" + approvedChecks.size() + ")");
            npciTable.refresh();
        }
    }

    // ── Update stat cards ─────────────────────────────────────────────────
    private void updateCards(boolean validated) {
        int total = approvedChecks.size();
        int pass = 0, fail = 0; double clearAmt = 0;
        if (validated) {
            for (Check c : approvedChecks) {
                if ("passed".equals(npciCheck(c))) { pass++; clearAmt += c.getAmount(); }
                else fail++;
            }
        }
        setCard(npciTotalCard, String.valueOf(total));
        setCard(npciPassCard,  validated ? String.valueOf(pass)  : "0");
        setCard(npciFailCard,  validated ? String.valueOf(fail)  : "0");
        setCard(npciAmtCard,   validated ? "₹" + String.format("%,.0f", clearAmt) : "₹0");
    }

    // ── Run NPCI Validation ───────────────────────────────────────────────
    @Listen("onClick = #runNpciBtn")
    public void runNpciValidation() {
        if (approvedChecks.isEmpty()) {
            Clients.showNotification(
                    "No approved checks found. Please complete Batch Creation first.",
                    "warning", null, "top_center", 3000);
            return;
        }
        npciValidated = true;
        renderTable(true);
        updateCards(true);
        long passed = approvedChecks.stream()
                .filter(c -> "passed".equals(npciCheck(c))).count();
        Clients.showNotification("All " + passed + " checks passed NPCI validation",
                "info", null, "top_right", 3000);
    }

    // ── Submit to NPCI ────────────────────────────────────────────────────
    @Listen("onClick = #submitNpciBtn")
    public void submitToNpci() {
        if (!npciValidated) {
            Clients.showNotification("Please run NPCI validation first.",
                    "warning", null, "top_center", 2500); return;
        }
        if (submitted) {
            Clients.showNotification("Already submitted to NPCI.",
                    "warning", null, "top_center", 2500); return;
        }
        long passed = approvedChecks.stream()
                .filter(c -> "passed".equals(npciCheck(c))).count();
        double totalAmt = approvedChecks.stream()
                .filter(c -> "passed".equals(npciCheck(c)))
                .mapToDouble(c -> c.getAmount() == null ? 0 : c.getAmount()).sum();
        if (passed == 0) {
            Clients.showNotification("No checks passed NPCI validation. Cannot submit.",
                    "error", null, "top_center", 3000); return;
        }
        submitted = true;
        if (npciSuccessBox    != null) npciSuccessBox.setVisible(true);
        if (npciSuccessDetail != null)
            npciSuccessDetail.setValue(passed + " check(s) totaling ₹"
                    + String.format("%,.0f", totalAmt) + " have been submitted for clearing");
        Clients.showNotification(
                "Successfully submitted " + passed + " checks to NPCI clearing house",
                "info", null, "top_right", 3000);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private String npciCheck(Check c) {
        if (c.getAmount() == null || c.getAmount() <= 0) return "failed";
        if (c.getAccountNo() == null || !c.getAccountNo().matches("\\d{12}")) return "failed";
        if (c.getIfsc()      == null || !c.getIfsc().matches("[A-Z]{4}0[A-Z0-9]{6}")) return "failed";
        if (c.getMicr()      == null || !c.getMicr().matches("\\d{9}")) return "failed";
        return "passed";
    }

    private String npciIssue(Check c) {
        if (c.getAmount() == null || c.getAmount() <= 0)   return "Invalid amount";
        if (c.getAccountNo() == null || !c.getAccountNo().matches("\\d{12}")) return "Invalid account number";
        if (c.getIfsc() == null || !c.getIfsc().matches("[A-Z]{4}0[A-Z0-9]{6}")) return "Invalid IFSC";
        if (c.getMicr() == null || !c.getMicr().matches("\\d{9}")) return "Invalid MICR";
        return "Unknown issue";
    }

    private String npciBadge(String s) {
        switch (s) {
            case "passed": return "background:#dcfce7;color:#16a34a;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "failed": return "background:#fee2e2;color:#dc2626;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            default:       return "background:#f1f5f9;color:#64748b;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
        }
    }

    private void addCell(Listitem item, String text) {
        Listcell cell = new Listcell(text);
        cell.setStyle("padding:12px 16px;font-size:14px;color:#1e293b;");
        item.appendChild(cell);
    }

    private void setCard(SummaryCard card, String value) {
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
