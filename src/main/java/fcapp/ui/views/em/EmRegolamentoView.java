package fcapp.ui.views.em;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serial;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.engine.jdbc.ClobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

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
@Route(value = "regolamentoEm", layout = MainLayout.class)
@RolesAllowed("USER")
public class EmRegolamentoView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_REGOLAMENTO_PATH =
            "classpath:html/fcqatar2022_regolamento.html";

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ResourceLoader resourceLoader;
    private final AccessoService accessoService;
    private final RegolamentoService regolamentoService;

    private String html = "";
    private FcRegolamento regolamento;

    private VaadinCKEditor decoupledEditor;
    private Button salvaDb;

    public EmRegolamentoView(
            ResourceLoader resourceLoader,
            AccessoService accessoService,
            RegolamentoService regolamentoService) {
        log.info("EmRegolamentoView()");
        this.resourceLoader = resourceLoader;
        this.accessoService = accessoService;
        this.regolamentoService = regolamentoService;
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
            regolamento = loadFirstRegolamento();
            html = regolamento != null
                    ? readFromDatabase(regolamento)
                    : readFromResource(DEFAULT_REGOLAMENTO_PATH);

            log.debug("Regolamento caricato");
        } catch (Exception e) {
            log.error("Errore durante il caricamento del regolamento", e);
            html = "";
        }
    }

    private FcRegolamento loadFirstRegolamento() {
        List<FcRegolamento> regolamenti = regolamentoService.findAll();
        if (regolamenti == null || regolamenti.isEmpty()) {
            return null;
        }
        return regolamenti.get(0);
    }

    private String readFromDatabase(FcRegolamento fcRegolamento) throws Exception {
        try (Reader reader = fcRegolamento.getSrc().getCharacterStream()) {
            return readAll(reader);
        }
    }

    private String readFromResource(String path) throws IOException {
        Resource resource = resourceLoader.getResource(path);
        try (InputStreamReader reader =
                     new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return readAll(reader);
        }
    }

    private String readAll(Reader reader) throws IOException {
        StringBuilder content = new StringBuilder();

        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line);
            }
        }

        return content.toString();
    }

    private void initLayout() {
        FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
        boolean isAdmin = isAdmin(attore);

        salvaDb = buildSaveButton(isAdmin);
        decoupledEditor = buildEditor(isAdmin);

        add(salvaDb);
        add(decoupledEditor);
        add(buildPreviewHtml());
    }

    private boolean isAdmin(FcAttore attore) {
        if (attore == null || attore.getRoles() == null) {
            return false;
        }

        return attore.getRoles().stream().anyMatch(role -> role == Role.ADMIN);
    }

    private Button buildSaveButton(boolean isAdmin) {
        Button button = new Button("Salva");
        button.setIcon(VaadinIcon.DATABASE.create());
        button.setVisible(isAdmin);
        button.addClickListener(event -> saveRegolamento());
        return button;
    }

    private VaadinCKEditor buildEditor(boolean isAdmin) {
        VaadinCKEditor editor = new VaadinCKEditorBuilder().with(builder -> {
            builder.editorType = EditorType.DECOUPLED;
            builder.editorData = html;
        }).createVaadinCKEditor();

        editor.setReadOnly(!isAdmin);
        return editor;
    }

    private VerticalLayout buildPreviewHtml() {
        VerticalLayout previewHtml = new VerticalLayout();
        previewHtml.getElement().setProperty("innerHTML", html);
        return previewHtml;
    }

    private void saveRegolamento() {
        try {
            log.info("SALVA");

            String valueHtml = decoupledEditor.getValue();
            log.debug("Html size={}", valueHtml != null ? valueHtml.length() : 0);

            if (regolamento == null) {
                regolamento = new FcRegolamento();
            }

            regolamento.setData(LocalDateTime.now());
            regolamento.setSrc(ClobProxy.generateProxy(valueHtml));
            regolamentoService.save(regolamento);

            CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
        } catch (Exception e) {
            log.error("Errore durante il salvataggio del regolamento", e);
            CustomMessageDialog.showMessageErrorDetails(
                    CustomMessageDialog.MSG_ERROR_GENERIC,
                    e.getMessage());
        }
    }
}
