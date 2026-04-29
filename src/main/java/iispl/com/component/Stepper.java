package iispl.com.component;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class Stepper extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;

    @Wire private Div   mc_circle1, mc_circle2, mc_circle3, mc_circle4;
    @Wire private Div   mc_line1, mc_line2, mc_line3;
    @Wire private Label mc_step1, mc_step2, mc_step3, mc_step4;

    private int activeStep = 1;

    private static final String CIRCLE_ACTIVE   = "width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:15px;background:#2563eb;color:white;";
    private static final String CIRCLE_INACTIVE = "width:36px;height:36px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-weight:700;font-size:15px;background:#e5e7eb;color:#9ca3af;";
    private static final String LABEL_ACTIVE    = "font-size:13px;color:#2563eb;font-weight:600;margin-top:4px;";
    private static final String LABEL_INACTIVE  = "font-size:13px;color:#9ca3af;margin-top:4px;";
    private static final String LINE_ACTIVE     = "flex:1;height:2px;background:#2563eb;margin:18px 6px 0;min-width:40px;";
    private static final String LINE_INACTIVE   = "flex:1;height:2px;background:#e5e7eb;margin:18px 6px 0;min-width:40px;";

    @Override
    public void afterCompose() {
        super.afterCompose();
        Selectors.wireComponents(this, this, false);
        applyStep();
    }

    private void applyStep() {
        if (mc_circle1 == null) return;
        applyCircle(mc_circle1, mc_step1, 1);
        applyCircle(mc_circle2, mc_step2, 2);
        applyCircle(mc_circle3, mc_step3, 3);
        applyCircle(mc_circle4, mc_step4, 4);
        if (mc_line1 != null) mc_line1.setStyle(activeStep >= 2 ? LINE_ACTIVE : LINE_INACTIVE);
        if (mc_line2 != null) mc_line2.setStyle(activeStep >= 3 ? LINE_ACTIVE : LINE_INACTIVE);
        if (mc_line3 != null) mc_line3.setStyle(activeStep >= 4 ? LINE_ACTIVE : LINE_INACTIVE);
    }

    private void applyCircle(Div circle, Label label, int step) {
        boolean active = activeStep >= step;
        boolean current = activeStep == step;
        circle.setStyle(active ? CIRCLE_ACTIVE : CIRCLE_INACTIVE);
        label.setStyle(current ? LABEL_ACTIVE : (active ? LABEL_ACTIVE : LABEL_INACTIVE));
    }

    public int getActiveStep() { return activeStep; }
    public void setActiveStep(int activeStep) {
        this.activeStep = activeStep;
        applyStep();
    }
}
