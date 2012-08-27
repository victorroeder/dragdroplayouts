/*
 * Copyright 2012 John Ahlroos
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fi.jasoft.dragdroplayouts;

import java.util.Map;

import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.TargetDetailsImpl;
import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.MouseEventDetails;
import com.vaadin.terminal.gwt.client.ui.dd.VerticalDropLocation;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

import fi.jasoft.dragdroplayouts.client.ui.Constants;
import fi.jasoft.dragdroplayouts.client.ui.LayoutDragMode;
import fi.jasoft.dragdroplayouts.client.ui.util.IframeCoverUtility;
import fi.jasoft.dragdroplayouts.client.ui.verticallayout.DDVerticalLayoutState;
import fi.jasoft.dragdroplayouts.events.LayoutBoundTransferable;
import fi.jasoft.dragdroplayouts.interfaces.DragFilter;
import fi.jasoft.dragdroplayouts.interfaces.LayoutDragSource;
import fi.jasoft.dragdroplayouts.interfaces.ShimSupport;

/**
 * Vertical layout with drag and drop support
 * 
 * @author John Ahlroos / www.jasoft.fi
 * @since 0.4.0
 */
@SuppressWarnings("serial")
public class DDVerticalLayout extends VerticalLayout implements
        LayoutDragSource, DropTarget, ShimSupport {
    /**
     * The drop handler which handles dropped components in the layout.
     */
    private DropHandler dropHandler;

    /**
     * Contains the component over which the drop was made and the index on
     * which the drop was made.
     */
    public class VerticalLayoutTargetDetails extends TargetDetailsImpl {

        private Component over;

        private int index = -1;

        protected VerticalLayoutTargetDetails(Map<String, Object> rawDropData) {
            super(rawDropData, DDVerticalLayout.this);

            // Get over which component (if any) the drop was made and the
            // index of it
            if (getData(Constants.DROP_DETAIL_TO) != null) {
                index = Integer.valueOf(getData(Constants.DROP_DETAIL_TO)
                        .toString());
                if (index >= 0 && index < components.size()) {
                    over = components.get(index);
                }
            }

            // Was the drop over no specific cell
            if (over == null) {
                over = DDVerticalLayout.this;
            }
        }

        /**
         * The component over which the drop was made.
         * 
         * @return Null if the drop was not over a component, else the component
         */
        public Component getOverComponent() {
            return over;
        }

        /**
         * The index over which the drop was made. If the drop was not made over
         * any component then it returns -1.
         * 
         * @return The index of the component or -1 if over no component.
         */
        public int getOverIndex() {
            return index;
        }

        /**
         * Some details about the mouse event
         * 
         * @return details about the actual event that caused the event details.
         *         Practically mouse move or mouse up.
         */
        public MouseEventDetails getMouseEvent() {
            return MouseEventDetails
                    .deSerialize((String) getData(Constants.DROP_DETAIL_MOUSE_EVENT));
        }

        /**
         * Get the horizontal position of the dropped component within the
         * underlying cell.
         * 
         * @return The drop location
         */
        public VerticalDropLocation getDropLocation() {
            return VerticalDropLocation
                    .valueOf((String) getData(Constants.DROP_DETAIL_VERTICAL_DROP_LOCATION));
        }
    }

    /**
     * {@inheritDoc}
     * 
     */
    public void paintContent(PaintTarget target) throws PaintException {

        if (dropHandler != null && isEnabled()) {
            dropHandler.getAcceptCriterion().paint(target);
        }

        // Drop ratio
        target.addAttribute(Constants.ATTRIBUTE_VERTICAL_DROP_RATIO, getState()
                .getCellTopBottomDropRatio());

        // Drag mode
        if (isEnabled()) {
            target.addAttribute(Constants.DRAGMODE_ATTRIBUTE, getState()
                    .getDragMode().ordinal());
        } else {
            target.addAttribute(Constants.DRAGMODE_ATTRIBUTE,
                    LayoutDragMode.NONE.ordinal());
        }

        // Shims
        target.addAttribute(IframeCoverUtility.SHIM_ATTRIBUTE, getState()
                .isIframeShims());

        // Paint the dragfilter into the paint target
        new DragFilterPaintable(this).paint(target);
    }

    public TargetDetails translateDropTargetDetails(
            Map<String, Object> clientVariables) {
        return new VerticalLayoutTargetDetails(clientVariables);
    }

    /**
     * Get the transferable created by a drag event.
     */
    public Transferable getTransferable(Map<String, Object> rawVariables) {
        return new LayoutBoundTransferable(this, rawVariables);
    }

    /**
     * Returns the drop handler which handles drop events from dropping
     * components on the layout. Returns Null if dropping is disabled.
     */
    public DropHandler getDropHandler() {
        return dropHandler;
    }

    /**
     * Sets the current handler which handles dropped components on the layout.
     * By setting a drop handler dropping components on the layout is enabled.
     * By setting the dropHandler to null dropping is disabled.
     * 
     * @param dropHandler
     *            The drop handler to handle drop events or null to disable
     *            dropping
     */
    public void setDropHandler(DropHandler dropHandler) {
        if (this.dropHandler != dropHandler) {
            this.dropHandler = dropHandler;
            requestRepaint();
        }
    }

    /**
     * Returns the mode of which dragging is visualized.
     * 
     * @return
     */
    public LayoutDragMode getDragMode() {
        return getState().getDragMode();
    }

    /**
     * Enables dragging components from the layout.
     * 
     * @param mode
     *            The mode of which how the dragging should be visualized.
     */
    public void setDragMode(LayoutDragMode mode) {
        getState().setDragMode(mode);
    }

    /**
     * Sets the ratio which determines how a cell is divided into drop zones.
     * The ratio is measured from the top and bottom borders. For example,
     * setting the ratio to 0.3 will divide the drop zone in three equal parts
     * (left,middle,right). Setting the ratio to 0.5 will disable dropping in
     * the middle and setting it to 0 will disable dropping at the sides.
     * 
     * @param ratio
     *            A ratio between 0 and 0.5. Default is 0.2
     */
    public void setComponentVerticalDropRatio(float ratio) {
        if (getState().getCellTopBottomDropRatio() != ratio) {
            if (ratio >= 0 && ratio <= 0.5) {
                getState().setCellTopBottomDropRatio(ratio);
            } else {
                throw new IllegalArgumentException(
                        "Ratio must be between 0 and 0.5");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setShim(boolean shim) {
        getState().setIframeShims(shim);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isShimmed() {
        return getState().isIframeShims();
    }

    /**
     * {@inheritDoc}
     */
    public DragFilter getDragFilter() {
        return getState().getDragFilter();
    }

    /**
     * {@inheritDoc}
     */
    public void setDragFilter(DragFilter dragFilter) {
        getState().setDragFilter(dragFilter);
    }

    @Override
    public DDVerticalLayoutState getState() {
        return (DDVerticalLayoutState) super.getState();
    }
}
