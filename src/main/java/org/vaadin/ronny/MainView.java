package org.vaadin.ronny;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MainView() {

		Div red = new Div();
		red.getElement().getStyle().set("background-color", "red");
		red.setWidth("200px");
		red.setHeight("100px");
		Div green = new Div();
		green.getElement().getStyle().set("background-color", "green");
		green.setWidth("200px");
		green.setHeight("100px");

		Div yellow = new Div();
		yellow.getElement().getStyle().set("background-color", "yellow");
		yellow.setWidth("500px");
		yellow.setHeight("50px");

		Button button = new Button("Click me");
		AbsoluteLayout absoluteLayout = new AbsoluteLayout();
		final Html html = new Html("<h1><i>Hello</i> <b>world</b></h1>");
		button.addClickListener( e -> absoluteLayout.moveTo(html,324, 100));
		button.addClickListener( e -> absoluteLayout.replace(red, green));
		
		absoluteLayout.add(html, 195,269);
		absoluteLayout.add(button);

		absoluteLayout.add(red,50,100);
		absoluteLayout.add(green,75,120);
		absoluteLayout.add(yellow,60, 60);
		
		
		for(int points = 0; points<7; points++) {
			Div blueCircle = new Div();
			blueCircle.getElement().getStyle().set("background-color","blue");
			blueCircle.getElement().getStyle().set("border-radius", "50%");
			blueCircle.getElement().getStyle().set("height", "50px");
			blueCircle.getElement().getStyle().set("width", "50px");
			
			int posX = (int)(285 + 200*Math.cos(points*2*Math.PI/7));
			int posY = (int)(325 + 200*Math.sin(points*2*Math.PI/7));
			
			absoluteLayout.add(blueCircle, posX, posY);
			
			
		}

		//absoluteLayout.remove(red);
		//absoluteLayout.addComponentAtIndex(3, red);
		
		add(absoluteLayout);

    }
	
}
