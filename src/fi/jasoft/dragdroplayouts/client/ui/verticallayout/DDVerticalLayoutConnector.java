package fi.jasoft.dragdroplayouts.client.ui.verticallayout;

import com.vaadin.shared.ui.Connect;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.ui.orderedlayout.VerticalLayoutConnector;

import fi.jasoft.dragdroplayouts.DDVerticalLayout;
import fi.jasoft.dragdroplayouts.client.ui.VDragDropUtil;

@Connect(DDVerticalLayout.class)
public class DDVerticalLayoutConnector extends VerticalLayoutConnector
        implements Paintable {

    /**
     * {@inheritDoc}
     */
    @Override
    public VDDVerticalLayout getWidget() {
        return (VDDVerticalLayout) super.getWidget();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DDVerticalLayoutState getState() {
        return (DDVerticalLayoutState) super.getState();
    }

    @Override
    public void init() {
        super.init();
        VDragDropUtil.listenToStateChangeEvents(this, getWidget()
                .getMouseHandler(), getWidget().getIframeCoverUtility(),
                getWidget());
    }

    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {

        // Drop handlers
        UIDL ac = uidl.getChildByTagName("-ac");
        if (ac == null) {
            if (getWidget().getDropHandler() != null) {
                // remove dropHandler if not present anymore
                getWidget().setDropHandler(null);
            }
        } else {
            if (getWidget().getDropHandler() == null) {
                getWidget().setDropHandler(
                        new VDDVerticalLayoutDropHandler(getWidget(), this));
            }
            getWidget().getDropHandler().updateAcceptRules(ac);
        }
    }
}
