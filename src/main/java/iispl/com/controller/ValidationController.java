package iispl.com.controller;

import java.util.List;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;

import iispl.com.model.Check;
import iispl.com.service.CheckService;

public class ValidationController extends SelectorComposer<Window> {

    @Wire private Listbox validationList;
    @Wire private Label totalLbl, validLbl, invalidLbl, successMsg;
    

    private CheckService service = new CheckService();

    @Listen("onClick = #validateBtn")
    public void validateChecks() throws Exception {

        service.validateChecks();

        successMsg.setVisible(true); // show green toast

        loadData();
    }

    public void doAfterCompose(Window comp) throws Exception {
        super.doAfterCompose(comp);
        loadData();
    }

    private void loadData() throws Exception {

        validationList.getItems().clear();

        List<Check> checks = service.getAllChecks();

        int total = 0, valid = 0, invalid = 0;

        for (Check c : checks) {

            total++;

            Listitem item = new Listitem();

            item.appendChild(new Listcell(c.getCheckNo()));
            item.appendChild(new Listcell(c.getAccountNo()));

            // ✅ FORMAT AMOUNT (FIXED)
            String amt = String.format("₹%,.0f", c.getAmount());
            item.appendChild(new Listcell(amt));

            // ✅ STATUS BADGE
            Listcell statusCell = new Listcell();
            Label status = new Label(c.getStatus());

            if ("valid".equalsIgnoreCase(c.getStatus())) {
                status.setStyle("background:#dcfce7; color:#16a34a; padding:4px 12px; border-radius:12px;");
                valid++;
            } else {
                status.setStyle("background:#fee2e2; color:#dc2626; padding:4px 12px; border-radius:12px;");
                invalid++;
            }

            statusCell.appendChild(status);
            item.appendChild(statusCell);

            item.appendChild(new Listcell(c.getValidationMsg()));

            validationList.appendChild(item);
        }

        totalLbl.setValue(String.valueOf(total));
        validLbl.setValue(String.valueOf(valid));
        invalidLbl.setValue(String.valueOf(invalid));
    }
}