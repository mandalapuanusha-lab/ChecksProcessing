package iispl.com.controller;

import java.util.List;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class BatchController extends SelectorComposer<Window> {

    @Wire private Listbox batchList;
    @Wire private Label validCount, selectedCount, approvedCount, batchAmount;
    @Wire private Hlayout successBox;
    @Wire private Label batchNoLbl, totalChecksLbl, totalAmtLbl;

    private CheckService service = new CheckService();

    private List<Check> validChecks;

    @Override
    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        loadData();
    }

    private void loadData() {

        batchList.getItems().clear();
        validChecks = service.getValidChecks();

        double totalAmt = 0;

        for (Check c : validChecks) {

            Listitem item = new Listitem();

            item.appendChild(new Listcell(""));
            item.appendChild(new Listcell(c.getCheckNo()));
            item.appendChild(new Listcell(c.getAccountNo()));
            item.appendChild(new Listcell("₹" + String.format("%,.0f", c.getAmount())));
            item.appendChild(new Listcell(c.getBankName()));

            Label status = new Label("valid");
            status.setStyle("background:#dbeafe; color:#2563eb; padding:4px 10px; border-radius:12px;");
            Listcell statusCell = new Listcell();
            statusCell.appendChild(status);

            item.appendChild(statusCell);

            batchList.appendChild(item);

            totalAmt += c.getAmount();
        }

        validCount.setValue(String.valueOf(validChecks.size()));
        batchAmount.setValue("₹" + String.format("%,.0f", totalAmt));
    }

    @Listen("onClick = #selectAllBtn")
    public void selectAll() {
        for (Listitem item : batchList.getItems()) {
            item.setSelected(true);
        }
        updateSelection();
    }

    @Listen("onSelect = #batchList")
    public void updateSelection() {

        int selected = batchList.getSelectedItems().size();
        selectedCount.setValue(String.valueOf(selected));
    }

    @Listen("onClick = #approveBtn")
    public void approveSelected() {

        int selected = batchList.getSelectedItems().size();

        approvedCount.setValue(String.valueOf(selected));
    }

    @Listen("onClick = #createBatchBtn")
    public void createBatch() {

        int total = batchList.getSelectedItems().size();

        successBox.setVisible(true);

        batchNoLbl.setValue("BATCH" + System.currentTimeMillis());
        totalChecksLbl.setValue(String.valueOf(total));
        totalAmtLbl.setValue(batchAmount.getValue());
    }
}