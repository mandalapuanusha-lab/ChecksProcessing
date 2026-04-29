package iispl.com.controller;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import iispl.com.component.DataTable;
import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class CheckEntryController extends SelectorComposer<Component> {

    // ── Form fields ───────────────────────────────────────────────────────
    @Wire private Textbox   checkNo;
    @Wire private Datebox   date;
    @Wire private Textbox   accountNo;
    @Wire private Textbox   accountName;
    @Wire private Doublebox amount;
    @Wire private Textbox   amountInWords;
    @Wire private Textbox   ifsc;
    @Wire private Textbox   micr;
    @Wire private Textbox   bankName;
    @Wire private Textbox   branchName;

    // ── DataTable macro component (resolved after compose) ────────────────
    private DataTable checkTable;
    private Listbox   tableList;       // inner Listbox from DataTable

    private final CheckService service = new CheckService();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        // Resolve DataTable by id; cannot use @Wire on macro components
        checkTable = (DataTable) comp.getFellow("checkTable");
        tableList  = (checkTable != null) ? checkTable.getTableList() : null;
        loadChecks();
    }

    // ── Add Check ─────────────────────────────────────────────────────────
    @Listen("onClick = #addBtn")
    public void addCheck() {
        String chkNo    = checkNo.getValue().trim();
        String accNo    = accountNo.getValue().trim();
        String micrVal  = micr.getValue().trim();
        String ifscVal  = ifsc.getValue().trim();
        String amtWords = amountInWords.getValue().trim();

        // Validations
        if (!chkNo.matches("\\d{6}")) {
            err("Check Number must be exactly 6 digits"); return;
        }
        for (Check ex : service.getAllChecks()) {
            if (ex.getCheckNo().equals(chkNo)) {
                err("Duplicate Check Number not allowed"); return;
            }
        }
        if (!accNo.matches("\\d{12}")) {
            err("Account Number must be exactly 12 digits"); return;
        }
        if (!micrVal.matches("\\d{9}")) {
            err("MICR must be exactly 9 digits"); return;
        }
        if (!ifscVal.matches("[A-Z]{4}0[A-Z0-9]{6}")) {
            err("Invalid IFSC format (e.g. HDFC0001234)"); return;
        }
        if (amount.getValue() == null || amount.getValue() <= 0) {
            err("Amount must be greater than 0"); return;
        }
        if (amtWords.isEmpty()) {
            err("Enter Amount in Words"); return;
        }
        if (date.getValue() == null) {
            err("Select Date"); return;
        }

        Check c = new Check();
        c.setCheckNo(chkNo);
        c.setAccountNo(accNo);
        c.setAmount(amount.getValue());
        c.setAmountInWords(amtWords);
        c.setIfsc(ifscVal);
        c.setMicr(micrVal);
        c.setBankName(bankName.getValue().trim());
        c.setBranchName(branchName.getValue().trim());
        c.setDate(date.getValue());
        c.setStatus("pending");

        service.saveCheck(c);
        Clients.showNotification("Check added successfully", "info", null, "top_right", 2000);
        clearForm();
        loadChecks();
    }

    // ── Load / Render ─────────────────────────────────────────────────────
    private void loadChecks() {
        if (tableList == null) return;
        tableList.getItems().clear();

        List<Check> checks = service.getAllChecks();
        for (Check c : checks) {
            Listitem item = new Listitem();
            item.setStyle("border-bottom:1px solid #f1f5f9;");

            addCell(item, c.getCheckNo());
            addCell(item, c.getAccountNo());
            addCell(item, "₹" + String.format("%,.0f", c.getAmount()));
            addCell(item, safe(c.getBankName()));
            addCell(item, safe(c.getIfsc()));
            addBadgeCell(item, safe(c.getStatus()));

            tableList.appendChild(item);
        }

        // Update caption with row count
        if (checkTable != null) {
            checkTable.setTableCaption("Checks Added (" + checks.size() + ")");
            checkTable.refresh();
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
        switch (s.toLowerCase()) {
            case "valid":    return "background:#dcfce7;color:#16a34a;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "invalid":  return "background:#fee2e2;color:#dc2626;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "approved": return "background:#dbeafe;color:#2563eb;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            case "batched":  return "background:#e0e7ff;color:#4338ca;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
            default:         return "background:#f1f5f9;color:#64748b;padding:3px 12px;border-radius:999px;font-size:13px;font-weight:600;";
        }
    }

    private void err(String msg) {
        Clients.showNotification(msg, "error", null, "top_center", 3000);
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void clearForm() {
        checkNo.setValue(""); accountNo.setValue(""); accountName.setValue("");
        amount.setValue(null); amountInWords.setValue("");
        ifsc.setValue(""); micr.setValue("");
        bankName.setValue(""); branchName.setValue("");
        date.setValue(null);
    }
}
