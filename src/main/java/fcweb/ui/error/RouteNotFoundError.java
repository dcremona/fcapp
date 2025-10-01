package fcweb.ui.error;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;

import jakarta.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
public class RouteNotFoundError extends Component
		implements HasErrorParameter<NotFoundException>{

	private static final long serialVersionUID = 1L;

	@Override
	public int setErrorParameter(BeforeEnterEvent event,
			ErrorParameter<NotFoundException> parameter) {
		getElement().setText("Could not navigate to '" + event.getLocation().getPath() + "'");
		return HttpServletResponse.SC_NOT_FOUND;
	}
}