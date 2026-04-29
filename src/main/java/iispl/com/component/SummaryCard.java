package iispl.com.component;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;

public class SummaryCard extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;

    @Wire private Vlayout mc_cardContainer;
    @Wire private Label   mc_cardTitle;
    @Wire private Label   mc_cardValue;

    private String cardTitle  = "";
    private String cardValue  = "0";
    private String cardColor  = "#e7f0ff";
    private String labelColor = "#2563eb";

    // DO NOT call compose() in constructor — properties haven't been set yet
    // and the macro URI may not be resolved. ZK calls afterCompose() at the
    // right time after the component is added to the tree.

    @Override
    public void afterCompose() {
        super.afterCompose();
        Selectors.wireComponents(this, this, false);
        applyAll();
    }

    private void applyAll() {
        if (mc_cardContainer != null) {
            mc_cardContainer.setStyle(
                "background:" + cardColor + "; padding:20px; border-radius:12px; min-width:180px;");
        }
        if (mc_cardTitle != null) {
            mc_cardTitle.setValue(cardTitle);
            mc_cardTitle.setStyle("color:" + labelColor + "; font-size:14px;");
        }
        if (mc_cardValue != null) {
            mc_cardValue.setValue(cardValue);
            mc_cardValue.setStyle("color:" + labelColor + "; font-size:24px; font-weight:bold;");
        }
    }

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getCardTitle() { return cardTitle; }
    public void setCardTitle(String cardTitle) {
        this.cardTitle = cardTitle;
        if (mc_cardTitle != null) mc_cardTitle.setValue(cardTitle);
    }

    public String getCardValue() { return cardValue; }
    public void setCardValue(String cardValue) {
        this.cardValue = (cardValue == null) ? "0" : cardValue;
        if (mc_cardValue != null) mc_cardValue.setValue(this.cardValue);
    }

    public String getCardColor() { return cardColor; }
    public void setCardColor(String cardColor) {
        this.cardColor = cardColor;
        if (mc_cardContainer != null) {
            mc_cardContainer.setStyle(
                "background:" + cardColor + "; padding:20px; border-radius:12px; min-width:180px;");
        }
    }

    public String getLabelColor() { return labelColor; }
    public void setLabelColor(String labelColor) {
        this.labelColor = labelColor;
        if (mc_cardTitle != null)
            mc_cardTitle.setStyle("color:" + labelColor + "; font-size:14px;");
        if (mc_cardValue != null)
            mc_cardValue.setStyle("color:" + labelColor + "; font-size:24px; font-weight:bold;");
    }
}
