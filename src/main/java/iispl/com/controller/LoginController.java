package iispl.com.controller;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

public class LoginController extends SelectorComposer<Component> {

    @Wire private Textbox username;
    @Wire private Textbox password;
    @Wire private Label   errorMsg;

    // Demo credentials — replace with DB lookup for production
    private static final String VALID_USER = "admin";
    private static final String VALID_PASS = "admin123";

    @Listen("onClick = #loginBtn; onOK = #username; onOK = #password")
    public void login() {
        String user = username.getValue().trim();
        String pass = password.getValue().trim();

        // ── Validation ──
        if (user.isEmpty() && pass.isEmpty()) {
            showError("Please enter username and password.");
            return;
        }
        if (user.isEmpty()) {
            showError("Username is required.");
            return;
        }
        if (pass.isEmpty()) {
            showError("Password is required.");
            return;
        }
        if (user.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }
        if (pass.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }

        // ── Auth check ──
        if (VALID_USER.equals(user) && VALID_PASS.equals(pass)) {
            Executions.getCurrent().getSession().setAttribute("loggedUser", user);
            Executions.sendRedirect("/zul/checkentry.zul");
        } else {
            showError("Invalid username or password. Please try again.");
        }
    }

    private void showError(String msg) {
        errorMsg.setValue(msg);
        errorMsg.setVisible(true);
    }
}
