package iispl.com.controller;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.*;

import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class CheckEntryController extends SelectorComposer<Component> {

    @Wire private Textbox checkNo;
    @Wire private Datebox date;
    @Wire private Textbox accountNo;
    @Wire private Textbox accountName;
    @Wire private Doublebox amount;
    @Wire private Textbox amountInWords;
    @Wire private Textbox ifsc;
    @Wire private Textbox micr;
    @Wire private Textbox bankName;
    @Wire private Textbox branchName;

    @Wire private Listbox checkList;

    private CheckService service = new CheckService();

    // =============================
    // ADD CHECK
    // =============================
    @Listen("onClick = #addBtn")
    public void addCheck() {

        String chkNo = checkNo.getValue().trim();
        String accNo = accountNo.getValue().trim();
        String micrVal = micr.getValue().trim();
        String ifscVal = ifsc.getValue().trim();
        String amtWords = amountInWords.getValue().trim();

        // -------- VALIDATIONS --------

        if (!chkNo.matches("\\d{6}")) {
            Clients.showNotification("Check Number must be exactly 6 digits");
            return;
        }

        // ✅ DUPLICATE CHECK NUMBER (IMPORTANT)
        for (Check existing : service.getAllChecks()) {
            if (existing.getCheckNo().equals(chkNo)) {
                Clients.showNotification("Duplicate Check Number not allowed");
                return;
            }
        }

        if (!accNo.matches("\\d{12}")) {
            Clients.showNotification("Account Number must be exactly 12 digits");
            return;
        }

        if (!micrVal.matches("\\d{9}")) {
            Clients.showNotification("MICR must be exactly 9 digits");
            return;
        }

        if (!ifscVal.matches("[A-Z]{4}0[A-Z0-9]{6}")) {
            Clients.showNotification("Invalid IFSC format");
            return;
        }

        if (amount.getValue() == null || amount.getValue() <= 0) {
            Clients.showNotification("Amount must be greater than 0");
            return;
        }

        if (amtWords.isEmpty()) {
            Clients.showNotification("Enter Amount in Words");
            return;
        }

        if (date.getValue() == null) {
            Clients.showNotification("Select Date");
            return;
        }

        // -------- SAVE --------
        Check c = new Check();

        c.setCheckNo(chkNo);
        c.setAccountNo(accNo);
      
        c.setAmount(amount.getValue());
        c.setAmountInWords(amtWords);
        c.setIfsc(ifscVal);
        c.setMicr(micrVal);
        c.setBankName(bankName.getValue());
        c.setBranchName(branchName.getValue());
     
        c.setStatus("pending");

        service.saveCheck(c);

        Clients.showNotification("Check Added Successfully", "info", null, "top_right", 2000);

        clearForm();
        loadChecks();
    }

    // =============================
    // LOAD DATA
    // =============================
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        loadChecks();
    }

    private void loadChecks() {

        checkList.getItems().clear();

        List<Check> checks = service.getAllChecks();

        for (Check c : checks) {

            Listitem item = new Listitem();

            item.appendChild(new Listcell(c.getCheckNo()));
            item.appendChild(new Listcell(c.getAccountNo()));

            // ✅ FORMAT AMOUNT
            String amt = "₹" + String.format("%,.0f", c.getAmount());
            item.appendChild(new Listcell(amt));

            item.appendChild(new Listcell(c.getBankName()));
            item.appendChild(new Listcell(c.getIfsc()));

            // ✅ SAFE STATUS
            String status = (c.getStatus() == null) ? "pending" : c.getStatus();
            item.appendChild(new Listcell(status));

            checkList.appendChild(item);
        }
    }

    // =============================
    // CLEAR FORM
    // =============================
    private void clearForm() {

        checkNo.setValue("");
        accountNo.setValue("");
        accountName.setValue("");
        amount.setValue(null);
        amountInWords.setValue("");
        ifsc.setValue("");
        micr.setValue("");
        bankName.setValue("");
        branchName.setValue("");
        date.setValue(null);
    }
}