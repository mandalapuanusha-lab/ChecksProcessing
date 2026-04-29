package iispl.com.component;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;

/**
 * StatusBadge — coloured pill label for check status.
 *
 * Usage in ZUL:
 *   <comp:statusbadge status="valid"/>
 *   <comp:statusbadge status="invalid"/>
 *   <comp:statusbadge status="pending"/>
 *   <comp:statusbadge status="approved"/>
 */
public class StatusBadge extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;

    @Wire private Label mc_statusLabel;

    private String status = "pending";

    public StatusBadge() {
        compose();
    }

    @Override
    public void afterCompose() {
        super.afterCompose();
        Selectors.wireComponents(this, this, false);
        applyStatus();
    }

    private void applyStatus() {
        if (mc_statusLabel == null) return;
        mc_statusLabel.setValue(status);
        mc_statusLabel.setStyle(resolveStyle(status));
    }

    private String resolveStyle(String s) {
        if (s == null) s = "pending";
        switch (s.toLowerCase()) {
            case "valid":    return "background:#dcfce7; color:#16a34a; padding:4px 12px; border-radius:12px; font-size:13px;";
            case "invalid":  return "background:#fee2e2; color:#dc2626; padding:4px 12px; border-radius:12px; font-size:13px;";
            case "approved": return "background:#dbeafe; color:#2563eb; padding:4px 12px; border-radius:12px; font-size:13px;";
            case "batched":  return "background:#f3e8ff; color:#9333ea; padding:4px 12px; border-radius:12px; font-size:13px;";
            default:         return "background:#f3f4f6; color:#6b7280; padding:4px 12px; border-radius:12px; font-size:13px;";
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        applyStatus();
    }
}
