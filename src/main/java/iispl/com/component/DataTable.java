package iispl.com.component;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

/**
 * DataTable — Reusable ZK macro component that wraps a styled Listbox.
 *
 * <p>Usage in ZUL (register macro first):
 * <pre>
 *   &lt;?component name="datatable"
 *               macroURI="/zul/components/datatable.zul"
 *               class="iispl.com.component.DataTable"?&gt;
 *
 *   &lt;datatable id="myTable"
 *              columns="CHECK NO.,ACCOUNT NO.,AMOUNT,STATUS"
 *              caption="Checks Added (0)"
 *              emptyMessage="No checks added yet"
 *              checkmark="false"
 *              showSearch="true"/&gt;
 * </pre>
 *
 * <p>In the controller, obtain the inner Listbox via:
 * <pre>
 *   DataTable dt = (DataTable) comp.getFellow("myTable");
 *   Listbox lb  = dt.getTableList();
 * </pre>
 *
 * <p>Then build Listitems normally and append to {@code lb}.
 * Call {@code dt.refresh()} after adding/removing rows to update
 * the row-count footer and empty-state panel.
 */
public class DataTable extends HtmlMacroComponent {

    private static final long serialVersionUID = 1L;

    // ── Wired children ───────────────────────────────────────────────────
    @Wire private Vlayout mc_tableContainer;
    @Wire private Hbox    mc_topBar;
    @Wire private Label   mc_caption;
    @Wire private Textbox mc_searchBox;
    @Wire private Listbox mc_tableList;
    @Wire private Vlayout mc_emptyState;
    @Wire private Label   mc_emptyMsg;
    @Wire private Div     mc_footer;
    @Wire private Label   mc_rowCount;

    // ── Component properties ─────────────────────────────────────────────
    /** Comma-separated column header labels, e.g. "CHECK NO.,ACCOUNT NO.,AMOUNT" */
    private String  columns      = "";
    /** Text shown in the empty-state panel when the table has no rows. */
    private String  emptyMessage = "No records found";
    /** Whether the Listbox should show ZK's built-in checkbox column. */
    private boolean checkmark    = false;
    /** Whether to show the search/filter textbox. */
    private boolean showSearch   = false;
    /** Optional heading text shown above the table. */
    private String  caption      = "";

    // Header style shared by all columns
    private static final String HDR_STYLE =
        "font-size:11px;font-weight:700;letter-spacing:0.06em;" +
        "color:#64748b;background:#f8fafc;padding:11px 16px;text-transform:uppercase;";

    // ── Lifecycle ─────────────────────────────────────────────────────────

    @Override
    public void afterCompose() {
        super.afterCompose();
        Selectors.wireComponents(this, this, false);

        // Apply properties set via ZUL attributes before compose
        applyCaption();
        applyEmptyMessage();
        applyCheckmark();
        applyShowSearch();
        buildHeaders();
        refresh();

        // Live search
        if (mc_searchBox != null && showSearch) {
            mc_searchBox.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
                @Override
                public void onEvent(Event event) throws Exception {
                    filterRows(mc_searchBox.getValue());
                }
            });
        }
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Returns the inner {@link Listbox} so controllers can append
     * or remove {@link Listitem}s directly.
     * Always call {@link #refresh()} after mutating the list.
     */
    public Listbox getTableList() {
        return mc_tableList;
    }

    /**
     * Clears all rows, then calls {@link #refresh()}.
     * Convenience shortcut for controllers that rebuild the entire list.
     */
    public void clearRows() {
        if (mc_tableList != null) mc_tableList.getItems().clear();
        refresh();
    }

    /**
     * Updates the footer row-count label and toggles the empty-state panel.
     * Call this after every add/remove operation on the inner Listbox.
     */
    public void refresh() {
        if (mc_tableList == null) return;

        int count = mc_tableList.getItemCount();
        boolean empty = (count == 0);

        // empty state
        if (mc_emptyState != null) mc_emptyState.setVisible(empty);

        // footer count
        if (mc_rowCount != null) {
            mc_rowCount.setValue(count + (count == 1 ? " record" : " records"));
        }

        // auto-update caption if it ends with "(N)" pattern
        if (mc_caption != null) {
            String cap = caption;
            if (cap.matches(".*\\(\\d+\\)")) {
                cap = cap.replaceAll("\\(\\d+\\)$", "(" + count + ")");
                mc_caption.setValue(cap);
            }
        }
    }

    /**
     * Updates the caption text at runtime (e.g. "Checks Added (3)").
     */
    public void setTableCaption(String text) {
        this.caption = (text == null) ? "" : text;
        if (mc_caption != null) mc_caption.setValue(this.caption);
    }

    // ── Property setters (called by ZK from ZUL attributes) ──────────────

    public String getColumns() { return columns; }
    public void setColumns(String columns) {
        this.columns = (columns == null) ? "" : columns;
        if (mc_tableList != null) buildHeaders(); // rebuild if already composed
    }

    public String getEmptyMessage() { return emptyMessage; }
    public void setEmptyMessage(String msg) {
        this.emptyMessage = (msg == null) ? "No records found" : msg;
        applyEmptyMessage();
    }

    public boolean isCheckmark() { return checkmark; }
    public void setCheckmark(boolean checkmark) {
        this.checkmark = checkmark;
        applyCheckmark();
    }

    public boolean isShowSearch() { return showSearch; }
    public void setShowSearch(boolean showSearch) {
        this.showSearch = showSearch;
        applyShowSearch();
    }

    public String getCaption() { return caption; }
    public void setCaption(String caption) {
        this.caption = (caption == null) ? "" : caption;
        applyCaption();
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void buildHeaders() {
        if (mc_tableList == null || columns == null || columns.trim().isEmpty()) return;

        // Remove old listhead if present
        if (mc_tableList.getListhead() != null) {
            mc_tableList.getListhead().detach();
        }

        Listhead head = new Listhead();
        head.setSizable(true);

        for (String col : columns.split(",")) {
            Listheader lh = new Listheader(col.trim());
            lh.setStyle(HDR_STYLE);
            head.appendChild(lh);
        }

        mc_tableList.insertBefore(head, mc_tableList.getFirstChild());
    }

    private void applyCaption() {
        if (mc_caption != null) {
            mc_caption.setValue(caption);
            mc_caption.setVisible(!caption.isEmpty());
        }
        // Hide top bar entirely when both caption and search are absent
        updateTopBarVisibility();
    }

    private void applyEmptyMessage() {
        if (mc_emptyMsg != null) mc_emptyMsg.setValue(emptyMessage);
    }

    private void applyCheckmark() {
        if (mc_tableList != null) {
            mc_tableList.setCheckmark(checkmark);
            mc_tableList.setMultiple(checkmark);
        }
    }

    private void applyShowSearch() {
        if (mc_searchBox != null) mc_searchBox.setVisible(showSearch);
        updateTopBarVisibility();
    }

    private void updateTopBarVisibility() {
        if (mc_topBar != null) {
            boolean show = !caption.isEmpty() || showSearch;
            mc_topBar.setVisible(show);
        }
    }

    /** Case-insensitive row filter based on cell text content. */
    private void filterRows(String keyword) {
        if (mc_tableList == null) return;
        String kw = (keyword == null) ? "" : keyword.trim().toLowerCase();

        for (Listitem item : mc_tableList.getItems()) {
            if (kw.isEmpty()) {
                item.setVisible(true);
                continue;
            }
            boolean match = false;
            for (Object child : item.getChildren()) {
                if (child instanceof Listcell) {
                    Listcell cell = (Listcell) child;
                    String label = cell.getLabel();
                    if (label != null && label.toLowerCase().contains(kw)) {
                        match = true;
                        break;
                    }
                    // also check child labels inside the cell
                    if (!cell.getChildren().isEmpty()) {
                        for (Object gc : cell.getChildren()) {
                            if (gc instanceof Label) {
                                String v = ((Label) gc).getValue();
                                if (v != null && v.toLowerCase().contains(kw)) {
                                    match = true;
                                    break;
                                }
                            }
                        }
                    }
                }
                if (match) break;
            }
            item.setVisible(match);
        }
        refresh();
    }
}
