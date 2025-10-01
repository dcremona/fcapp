package org.vaadin.ronny;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasOrderedComponents;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.orderedlayout.ThemableLayout;
import com.vaadin.flow.dom.Style;

/**
 * AbsoluteLayout is a component container which allows to put the subcomponents
 * at specific coordinates and thus have absolute control over the layout.
 * 
 * @author Ronny Edler
 *
 */
@Tag("absolute-layout")
public class AbsoluteLayout extends Component
		implements HasSize,HasOrderedComponents,ThemableLayout{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance of the AbsoluteLayout which uses 100% of the
	 * height and width available to it.
	 */
	public AbsoluteLayout() {
		setSizeFull();
		getElement().setAttribute("position", "relative");
	}

	/**
	 * Creates a new instance of the AbsoluteLayout with the given width and
	 * height. Everything outside this rectangle with <i>not</i> be displayed
	 * (it will be rendered though).
	 *
	 * @param width
	 *            A nonnegative width
	 * @param height
	 *            A nonnegative height
	 */
	public AbsoluteLayout(int width, int height) {
		if (width < 0) {
			throw new IllegalArgumentException("width must be non-negative");
		}
		if (height < 0) {
			throw new IllegalArgumentException("height must be non-negative");
		}

		Style style = getElement().getStyle();
		style.set("position", "relative");
		style.set("top", "0px");
		style.set("left", "0px");
		style.set("width", width + "px");
		style.set("height", height + "px");
		style.set("overflow", "hidden");
	}

	@Override
	public void add(Component... components) {
		for (Component c : components) {
			add(c, 0, 0);
		}
	}

	/**
	 * Add a component at the given position. Layouting starts at the top left
	 * corner of the layout and is relative to that.
	 * 
	 * @param c
	 *            {@link Component} to add
	 * @param left
	 *            distance from left side
	 * @param top
	 *            distance from top
	 */
	public void add(Component c, int left, int top) {
		HasOrderedComponents.super.add(c);

		Style style = c.getElement().getStyle();
		style.set("position", "absolute");
		style.set("left", left + "px");
		style.set("top", top + "px");

	}

	/**
	 * Moves a given component within the layout.
	 * 
	 * @param c
	 *            {@link Component} to move
	 * @param top
	 *            new distance from top
	 * @param left
	 *            new distance from the left
	 */
	public void moveTo(Component c, int left, int top) {
		if (!getChildren().anyMatch(comp -> comp.equals(c))) {
			throw new IllegalArgumentException("Component is not a child of this layout.");
		}
		Style style = c.getElement().getStyle();
		style.set("position", "absolute");
		style.set("left", "" + left + "px");
		style.set("top", "" + top + "px");
	}

	// /**
	// * Adds a given text as {@link Text} at the top left position (0,0) of the
	// layout.
	// * It is recommended to use {@link #add(Component, int, int)} with an
	// instance
	// of {@link Text} (or similar) instead
	// * to control the position the component will be added.
	// */
	// @Override
	// public void add(String text) {
	// HasOrderedComponents.super.add(text);
	// }

	/**
	 * Replaces a component and moves the new component to the replacee's old
	 * position.
	 */
	@Override
	public void replace(Component oldComponent, Component newComponent) {
		HasOrderedComponents.super.replace(oldComponent, newComponent);
		if (oldComponent == null || newComponent == null || (oldComponent.equals(newComponent))) {
			return; // NO-OP
		}
		// copy position to new component to keep the layout consistent
		Style oldStyle = oldComponent.getElement().getStyle();
		newComponent.getElement().getStyle().set("left", oldStyle.get("left"));
		newComponent.getElement().getStyle().set("top", oldStyle.get("top"));
	}

}
