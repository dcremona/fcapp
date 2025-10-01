package fcweb.ui.views.seriea;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.engine.jdbc.ClobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.wontlost.ckeditor.Constants.EditorType;
import com.wontlost.ckeditor.VaadinCKEditor;
import com.wontlost.ckeditor.VaadinCKEditorBuilder;

import common.util.Utils;
import fcweb.backend.data.Role;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcRegolamento;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.RegolamentoService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Regolamento")
@Route(value = "regolamento", layout = MainLayout.class)
@RolesAllowed("USER")
public class RegolamentoView extends VerticalLayout
		implements ComponentEventListener<ClickEvent<Button>>{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AccessoService accessoController;

	@Autowired
	private RegolamentoService regolamentoController;

	@Autowired
	private ResourceLoader resourceLoader;
	private String html = "";
	private FcRegolamento regolamento = null;

	private VaadinCKEditor decoupledEditor = null;

	private Button salvaDb;

	public RegolamentoView() {
		log.info("RegolamentoView()");
	}

	@PostConstruct
	void init() {
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initData();
		initLayout();
	}

	private void initData() {
		List<FcRegolamento> l = regolamentoController.findAll();
		try {

			BufferedReader br = null;
			BufferedReader br2 = null;
			if (l != null && !l.isEmpty()) {
				FcRegolamento r = l.get(0);
				regolamento = r;
				InputStreamReader isr2 = new InputStreamReader(r.getSrc().getAsciiStream());
				br2 = new BufferedReader(isr2);

				if (br2 != null) {
					String line2 = null;
					html = "";
					while ((line2 = br2.readLine()) != null) {
						html += line2;
					}
				}

			} else {
				Resource resource = resourceLoader.getResource("classpath:html/regolamento3.html");
				InputStreamReader isr = new InputStreamReader(resource.getInputStream());
				br = new BufferedReader(isr);

				if (br != null) {
					String line = null;
					html = "";
					while ((line = br.readLine()) != null) {
						html += line;
					}
				}
			}
			log.debug(html);

		} catch (Exception ex2) {
			log.error("ex2 " + ex2.getMessage());
		}
	}

	private void initLayout() {

		FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		boolean isAdmin = false;
		for (Role r : attore.getRoles()) {
			if (r.equals(Role.ADMIN)) {
				isAdmin = true;
				break;
			}
		}

		salvaDb = new Button("Salva");
		salvaDb.setIcon(VaadinIcon.DATABASE.create());
		salvaDb.addClickListener(this);
		salvaDb.setVisible(isAdmin);

		this.add(salvaDb);

		/** Document Editor */
		decoupledEditor = new VaadinCKEditorBuilder().with(builder -> {
			builder.editorType = EditorType.DECOUPLED;
			// builder.editorData = html;
		}).createVaadinCKEditor();
		decoupledEditor.setVisible(isAdmin);
		decoupledEditor.setValue(html);

		this.add(decoupledEditor);

		VerticalLayout previewHtml = new VerticalLayout();
		try {
			previewHtml.getElement().setProperty("innerHTML", html);
			this.add(previewHtml);
		} catch (Exception ex2) {
			log.error("ex2 " + ex2.getMessage());
		}
	}

	@Override
	public void onComponentEvent(ClickEvent<Button> event) {
		try {
			if (event.getSource() == salvaDb) {
				log.info("SALVA");

				String valueHtml = null;
				valueHtml = decoupledEditor.getValue();
				log.info(valueHtml);
				// valueHtml = StringUtils.encodeHtml(valueHtml);
				// Encoder encoder = Base64.getEncoder();
				// String encodedString =
				// encoder.encodeToString(valueHtml.getBytes());
				// LOG.debug(encodedString);
				if (regolamento == null) {
					regolamento = new FcRegolamento();
				}
				regolamento.setData(LocalDateTime.now());
				regolamento.setSrc(ClobProxy.generateProxy(valueHtml));

				regolamentoController.insertRegolamento(regolamento);

				CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
			}
		} catch (Exception e) {
			CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
		}
	}

}