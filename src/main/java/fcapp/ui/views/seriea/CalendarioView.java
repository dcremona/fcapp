package fcapp.ui.views.seriea;

import java.io.Serial;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiornata;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiornataService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Calendario")
@Route(value = "calendario", layout = MainLayout.class)
@RolesAllowed("USER")
public class CalendarioView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String REPORT_CALENDARIO = "classpath:reports/calendario.jasper";
    private static final String DECIMAL_PATTERN = "#0.00";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient JdbcTemplate jdbcTemplate;
    private final transient ResourceLoader resourceLoader;
    private final transient GiornataService giornataService;
    private final transient AccessoService accessoService;

    private List<FcGiornata> model = new ArrayList<>();

    public CalendarioView(
            JdbcTemplate jdbcTemplate,
            ResourceLoader resourceLoader,
            GiornataService giornataService,
            AccessoService accessoService) {

        this.jdbcTemplate = jdbcTemplate;
        this.resourceLoader = resourceLoader;
        this.giornataService = giornataService;
        this.accessoService = accessoService;

        log.info("CalendarioView()");
    }

    @PostConstruct
    void init() {
        log.info("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initData();
        initLayout();
    }

    private void initData() {
        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        if (campionato == null) {
            return;
        }

        List<FcGiornata> primaFase = new ArrayList<>();
        List<FcGiornata> secondaFase = new ArrayList<>();

        for (FcGiornata giornata : giornataService.findAll()) {
            int codiceGiornata = giornata.getFcGiornataInfo().getCodiceGiornata();
            if (codiceGiornata < 20) {
                primaFase.add(giornata);
            } else {
                secondaFase.add(giornata);
            }
        }

        if (campionato.getIdCampionato() == 1) {
            model = primaFase;
        } else if (campionato.getIdCampionato() == 2) {
            model = secondaFase;
        }
    }

    private void initLayout() {
        FcCampionato campionato = getSessionAttribute("CAMPIONATO", FcCampionato.class);
        if (campionato == null) {
            return;
        }

        VerticalLayout gridPrimaFaseAndata = buildSectionLayout(Costants.GREEN);
        VerticalLayout gridPrimaFaseRitorno = buildSectionLayout(Costants.GREEN);
        VerticalLayout gridQuarti = buildSectionLayout(Costants.MISTYROSE);
        VerticalLayout gridSemi = buildSectionLayout(Costants.LIGHT_YELLOW);
        VerticalLayout gridFinali = buildSectionLayout(Costants.POWDERBLUE);

        populateSections(gridPrimaFaseAndata, gridPrimaFaseRitorno, gridQuarti, gridSemi, gridFinali);

        try {
            HorizontalLayout calendarioPdfButton = buildButtonCalendarioPdf(campionato);
            if (calendarioPdfButton != null) {
                add(calendarioPdfButton);
            }
        } catch (Exception e) {
            log.error("Errore nella creazione del pulsante pdf calendario", e);
        }

        add(buildDetailsPanel("Prima Fase Andata", gridPrimaFaseAndata));
        add(buildDetailsPanel("Prima Fase Ritorno", gridPrimaFaseRitorno));

        if (gridQuarti.getComponentCount() > 0) {
            add(buildDetailsPanel("Quarti", gridQuarti));
        }

        if (gridSemi.getComponentCount() > 0) {
            add(buildDetailsPanel("Semifinali", gridSemi));
        }

        if (gridFinali.getComponentCount() > 0) {
            add(buildDetailsPanel("Finali", gridFinali));
        }
    }

    private VerticalLayout buildSectionLayout(String backgroundColor) {
        VerticalLayout layout = new VerticalLayout();
        layout.getStyle().set(Costants.BORDER, Costants.BORDER_COLOR);
        layout.getStyle().set(Costants.BACKGROUND, backgroundColor);
        return layout;
    }

    private void populateSections(
            VerticalLayout gridPrimaFaseAndata,
            VerticalLayout gridPrimaFaseRitorno,
            VerticalLayout gridQuarti,
            VerticalLayout gridSemi,
            VerticalLayout gridFinali) {

        List<FcGiornata> giornataItems = new ArrayList<>();
        int conta = 1;
        int partite = 4;

        for (FcGiornata giornata : model) {
            giornataItems.add(giornata);

            int idGiornataFc = giornata.getFcGiornataInfo().getIdGiornataFc();
            partite = getNumeroPartitePerGiornata(idGiornataFc);

            if (conta == partite) {
                VerticalLayout giornataLayout = buildGiornataLayout(giornataItems, giornata);
                addLayoutToSection(
                        idGiornataFc,
                        giornataLayout,
                        gridPrimaFaseAndata,
                        gridPrimaFaseRitorno,
                        gridQuarti,
                        gridSemi,
                        gridFinali);

                conta = 1;
                giornataItems = new ArrayList<>();
            } else {
                conta++;
            }
        }
    }

    private int getNumeroPartitePerGiornata(int idGiornataFc) {
        if (idGiornataFc == 17 || idGiornataFc == 18) {
            return 2;
        }
        if (idGiornataFc == 19) {
            return 1;
        }
        return 4;
    }

    private VerticalLayout buildGiornataLayout(List<FcGiornata> giornataItems, FcGiornata lastItem) {
        String dataGiornata = Utils.formatLocalDateTime(
                lastItem.getFcGiornataInfo().getDataGiornata(),
                Costants.DATA_FORMATTED);

        int idGiornataFc = lastItem.getFcGiornataInfo().getIdGiornataFc();
        String descrizione = buildDescrizioneGiornata(lastItem, dataGiornata, idGiornataFc);

        VerticalLayout layout = new VerticalLayout();

        Span title = new Span(descrizione);
        title.getStyle().set(Costants.FONT_SIZE, "14px");

        layout.add(title);
        layout.add(getTableCalendar(giornataItems));

        return layout;
    }

    private String buildDescrizioneGiornata(FcGiornata giornata, String dataGiornata, int idGiornataFc) {
        String descrizioneBase = giornata.getFcGiornataInfo().getDescGiornataFc() + " - " + dataGiornata;

        if (idGiornataFc > 16) {
            return giornata.getFcTipoGiornata().getDescTipoGiornata()
                    + " - "
                    + giornata.getFcGiornataInfo().getDescGiornataFc()
                    + " - "
                    + dataGiornata;
        }

        return descrizioneBase;
    }

    private void addLayoutToSection(
            int idGiornataFc,
            VerticalLayout giornataLayout,
            VerticalLayout gridPrimaFaseAndata,
            VerticalLayout gridPrimaFaseRitorno,
            VerticalLayout gridQuarti,
            VerticalLayout gridSemi,
            VerticalLayout gridFinali) {

        if (idGiornataFc < 8) {
            gridPrimaFaseAndata.add(giornataLayout);
        } else if (idGiornataFc < 15) {
            gridPrimaFaseRitorno.add(giornataLayout);
        } else if (idGiornataFc == 15 || idGiornataFc == 16) {
            gridQuarti.add(giornataLayout);
        } else if (idGiornataFc == 17 || idGiornataFc == 18) {
            gridSemi.add(giornataLayout);
        } else if (idGiornataFc == 19) {
            gridFinali.add(giornataLayout);
        }
    }

    private Details buildDetailsPanel(String summary, VerticalLayout content) {
        Details details = new Details(summary, content);
        details.addThemeVariants(DetailsVariant.REVERSE, DetailsVariant.FILLED);
        details.setEnabled(true);
        details.setOpened(true);
        return details;
    }

    private HorizontalLayout buildButtonCalendarioPdf(FcCampionato campionato) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setSpacing(true);

        try {
            Button stampaPdf = new Button("Calendario Pdf");
            stampaPdf.setIcon(VaadinIcon.DOWNLOAD.create());

            if (jdbcTemplate.getDataSource() != null) {
                Connection connection = jdbcTemplate.getDataSource().getConnection();

                Map<String, Object> parameters = new HashMap<>();
                parameters.put("START", String.valueOf(campionato.getStart()));
                parameters.put("END", String.valueOf(campionato.getEnd()));

                Resource resource = resourceLoader.getResource(REPORT_CALENDARIO);

                FileDownloadWrapper wrapper = new FileDownloadWrapper(
                        Utils.getStreamResource(
                                "Calendario.pdf",
                                connection,
                                parameters,
                                resource.getInputStream()));

                wrapper.wrapComponent(stampaPdf);
                layout.add(wrapper);
            }

        } catch (Exception e) {
            log.error("Errore nella creazione del pdf calendario", e);
        }

        return layout;
    }

    private Grid<FcGiornata> getTableCalendar(List<FcGiornata> items) {
        Grid<FcGiornata> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.setAllRowsVisible(true);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setWidth("500px");

        Column<FcGiornata> attoreCasaColumn = grid.addColumn(g -> g.getFcAttoreByIdAttoreCasa().getDescAttore());
        attoreCasaColumn.setSortable(false);

        Column<FcGiornata> golColumn = grid.addColumn(this::formatGol);
        golColumn.setSortable(false);

        Column<FcGiornata> attoreFuoriColumn = grid.addColumn(g -> g.getFcAttoreByIdAttoreFuori().getDescAttore());
        attoreFuoriColumn.setSortable(false);

        Column<FcGiornata> punteggioColumn = grid.addColumn(this::formatPunteggio);
        punteggioColumn.setSortable(false);

        return grid;
    }

    private String formatGol(FcGiornata giornata) {
        return giornata.getGolCasa() != null
                ? giornata.getGolCasa() + " - " + giornata.getGolFuori()
                : "-";
    }

    private String formatPunteggio(FcGiornata giornata) {
        DecimalFormat formatter = new DecimalFormat(DECIMAL_PATTERN);

        double totaleCasa = giornata.getTotCasa() != null ? giornata.getTotCasa() / Costants.DIVISORE_100 : 0d;
        double totaleFuori = giornata.getTotFuori() != null ? giornata.getTotFuori() / Costants.DIVISORE_100 : 0d;

        return formatter.format(totaleCasa) + " - " + formatter.format(totaleFuori);
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
