package fcapp.ui.views.seriea;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.engine.jdbc.ClobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import fcapp.backend.data.Role;
import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcRegolamento;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.RegolamentoService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Regolamento")
@Route(value = "regolamento", layout = MainLayout.class)
@RolesAllowed("USER")
public class RegolamentoView extends VerticalLayout
        implements ComponentEventListener<ClickEvent<Button>> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_HTML_PATH = "classpath:html/regolamento3.html";
    private static final String SESSION_ATTORE = "ATTORE";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient ResourceLoader resourceLoader;
    private final transient AccessoService accessoService;
    private final transient RegolamentoService regolamentoService;

    private String html = "";
    private FcRegolamento regolamento;
    private VaadinCKEditor decoupledEditor;
    private Button salvaDb;

    public RegolamentoView(
            ResourceLoader resourceLoader,
            AccessoService accessoService,
            RegolamentoService regolamentoService) {

        this.resourceLoader = resourceLoader;
        this.accessoService = accessoService;
        this.regolamentoService = regolamentoService;

        log.info("RegolamentoView()");
    }

    @PostConstruct
    void init() {
        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initData();
        initLayout();
    }

    private void initData() {
        try {
            List<FcRegolamento> regolamenti = regolamentoService.findAll();

            if (regolamenti != null && !regolamenti.isEmpty()) {
                regolamento = regolamenti.get(0);
                html = readFromDatabase(regolamento);
            } else {
                html = readDefaultHtml();
            }

            log.debug(html);

        } catch (Exception e) {
            log.error("Errore initData", e);
        }
    }

    private String readFromDatabase(FcRegolamento regolamento) throws Exception {
        try (InputStreamReader reader = new InputStreamReader(regolamento.getSrc().getAsciiStream());
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return readAll(bufferedReader);
        }
    }

    private String readDefaultHtml() throws Exception {
        Resource resource = resourceLoader.getResource(DEFAULT_HTML_PATH);

        try (InputStreamReader reader = new InputStreamReader(resource.getInputStream());
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            return readAll(bufferedReader);
        }
    }

    private String readAll(BufferedReader reader) throws Exception {
        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }

        return builder.toString();
    }

    private void initLayout() {
        FcAttore attore = getSessionAttribute(SESSION_ATTORE, FcAttore.class);
        boolean isAdmin = isAdmin(attore);

        salvaDb = new Button("Salva");
        salvaDb.setIcon(VaadinIcon.DATABASE.create());
        salvaDb.addClickListener(this);
        salvaDb.setVisible(isAdmin);
        add(salvaDb);

        decoupledEditor = new VaadinCKEditorBuilder()
                .with(builder -> builder.editorType = EditorType.DECOUPLED)
                .createVaadinCKEditor();
        decoupledEditor.setVisible(isAdmin);
        decoupledEditor.setValue(html);
        add(decoupledEditor);

        add(buildPreviewHtml());
    }

    private boolean isAdmin(FcAttore attore) {
        if (attore == null || attore.getRoles() == null) {
            return false;
        }

        for (Role role : attore.getRoles()) {
            if (role.equals(Role.ADMIN)) {
                return true;
            }
        }
        return false;
    }

    private VerticalLayout buildPreviewHtml() {
        VerticalLayout previewHtml = new VerticalLayout();

        try {
            previewHtml.getElement().setProperty("innerHTML", html);
        } catch (Exception e) {
            log.error("Errore buildPreviewHtml", e);
        }

        return previewHtml;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        try {
            if (event.getSource() != salvaDb) {
                return;
            }

            log.info("SALVA");

            String valueHtml = decoupledEditor.getValue();
            log.info(valueHtml);

            if (regolamento == null) {
                regolamento = new FcRegolamento();
            }

            regolamento.setData(LocalDateTime.now());
            regolamento.setSrc(ClobProxy.generateProxy(valueHtml));

            regolamentoService.save(regolamento);

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);

        } catch (Exception e) {
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
