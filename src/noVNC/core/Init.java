package noVNC.core;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.ButtonElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class Init implements EntryPoint {

	@Override
	public void onModuleLoad() {
		//Window.alert("test?");
		
		final UI ui = new UI();
		
		// hook parts in
		ButtonElement connectBtn = (ButtonElement) Document.get().getElementById("noVNC_connect_button");
		Button.wrap(connectBtn).addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ui.connect();
			}
		});
		
		
		// initialize!
		ui.load();
	}

}
