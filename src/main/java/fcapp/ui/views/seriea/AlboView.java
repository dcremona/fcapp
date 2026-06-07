package fcapp.ui.views.seriea;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import fcapp.backend.data.entity.FcAttore;
import fcapp.backend.data.entity.FcExpStat;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.AlboService;
import fcapp.backend.service.AttoreService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle("Albo")
@Route(value = "albo", layout = MainLayout.class)
@RolesAllowed("USER")
public class AlboView extends VerticalLayout {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final transient AlboService alboService;
    private final transient AttoreService attoreService;
    private final transient AccessoService accessoService;

    public AlboView(
            AlboService alboService,
            AttoreService attoreService,
            AccessoService accessoService) {

        this.alboService = alboService;
        this.attoreService = attoreService;
        this.accessoService = accessoService;

        log.info("AlboView()");
    }

    @PostConstruct
    void init() {
        log.debug("init");

        if (!Utils.isValidVaadinSession()) {
            return;
        }

        accessoService.insertAccesso(getClass().getName());
        initLayout();
    }

    private void initLayout() {
        List<FcExpStat> items = alboService.findAll();
        add(buildHistoryGrid(items));

        List<FcExpStat> crosstab = getModelCrosstab(items);
        crosstab.sort((left, right) -> right.getScudetto().compareToIgnoreCase(left.getScudetto()));
        add(buildSummaryGrid(crosstab));
    }

    private Grid<FcExpStat> buildHistoryGrid(List<FcExpStat> items) {
        Grid<FcExpStat> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);

        Column<FcExpStat> campionatoColumn = grid.addColumn(s -> s.getAnno() + " " + s.getCampionato());
        campionatoColumn.setSortable(false);
        campionatoColumn.setResizable(false);
        campionatoColumn.setHeader("Campionato");
        campionatoColumn.setWidth("150px");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getScudetto(), "badge success", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Scudetto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP2(), "badge pill", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Finalista");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP3(), null, false)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("3 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP4(), null, false)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("4 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP5(), null, false)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("5 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP6(), null, false)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("6 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP7(), null, false)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("7 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getP8(), "badge error", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("8 Posto");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getWinClasPt(), "badge success", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Clas Punti");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getWinClasReg(), "badge success", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Clas Regolare");

        grid.addColumn(new ComponentRenderer<>(s ->
                buildHighlightedPlayerCell(s.getWinClasTvsT(), "badge success", true)))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Clas TvsT");

        grid.addColumn(new ComponentRenderer<>(AlboView::buildTripleteCell))
                .setSortable(false)
                .setResizable(false)
                .setHeader("Triplete");

        grid.addColumn(new ComponentRenderer<>(AlboView::buildQuaDripleteCell))
		        .setSortable(false)
		        .setResizable(false)
		        .setHeader("Quadriplete");

        return grid;
    }

    private Grid<FcExpStat> buildSummaryGrid(List<FcExpStat> items) {
        Grid<FcExpStat> grid = new Grid<>();
        grid.setItems(items);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addThemeVariants(
                GridVariant.LUMO_NO_BORDER,
                GridVariant.LUMO_NO_ROW_BORDERS,
                GridVariant.LUMO_ROW_STRIPES);
        grid.setAllRowsVisible(true);

        Column<FcExpStat> squadraColumn = grid.addColumn(FcExpStat::getAnno);
        squadraColumn.setSortable(true);
        squadraColumn.setHeader(Costants.SQUADRA);

        addNumericColumn(grid, FcExpStat::getScudetto, "Scudetto");
        addNumericColumn(grid, FcExpStat::getP2, "Finalista");
        addNumericColumn(grid, FcExpStat::getP3, "3 Posto");
        addNumericColumn(grid, FcExpStat::getP4, "4 Posto");
        addNumericColumn(grid, FcExpStat::getP5, "5 Posto");
        addNumericColumn(grid, FcExpStat::getP6, "6 Posto");
        addNumericColumn(grid, FcExpStat::getP7, "7 Posto");
        addNumericColumn(grid, FcExpStat::getP8, "8 Posto");
        addNumericColumn(grid, FcExpStat::getWinClasPt, "Clas Punti");
        addNumericColumn(grid, FcExpStat::getWinClasReg, "Clas Regolare");
        addNumericColumn(grid, FcExpStat::getWinClasTvsT, "Clas TvsT");

        return grid;
    }

    private void addNumericColumn(
            Grid<FcExpStat> grid,
            java.util.function.Function<FcExpStat, String> getter,
            String header) {

        Column<FcExpStat> column = grid.addColumn(new ComponentRenderer<>(item -> buildNumericCell(getter.apply(item))));
        column.setSortable(true);
        column.setComparator(Comparator.comparing(getter));
        column.setHeader(header);
    }

    private HorizontalLayout buildNumericCell(String value) {
        HorizontalLayout cellLayout = new HorizontalLayout();

        if (StringUtils.isNotEmpty(value)) {
            cellLayout.add(new Span(String.valueOf(Integer.parseInt(value))));
        }

        return cellLayout;
    }

    private HorizontalLayout buildHighlightedPlayerCell(String value, String badgeTheme, boolean badgeForSelected) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);

        FcAttore currentAttore = getSessionAttribute("ATTORE", FcAttore.class);
        Span label = new Span(value);

        boolean isCurrentUser = currentAttore != null && currentAttore.getDescAttore().equals(value);
        if (isCurrentUser) {
            cellLayout.getStyle().set("color", Costants.GRAY);
            if (badgeForSelected && badgeTheme != null) {
                label.getElement().getThemeList().add(badgeTheme);
            }
        } else {
            label.getStyle().set("fontSize", "smaller");
        }

        cellLayout.add(label);
        return cellLayout;
    }

    private List<FcExpStat> getModelCrosstab(List<FcExpStat> all) {
        List<FcExpStat> result = new ArrayList<>();
        List<FcAttore> squadre = attoreService.findAll();

        for (FcAttore attore : squadre) {
            String squadra = attore.getDescAttore();

            int countScudetto = 0;
            int countP2 = 0;
            int countP3 = 0;
            int countP4 = 0;
            int countP5 = 0;
            int countP6 = 0;
            int countP7 = 0;
            int countP8 = 0;
            int countWinClasPt = 0;
            int countWinClasReg = 0;
            int countWinClasTvsT = 0;

            for (FcExpStat bean : all) {
                if (squadra.equals(bean.getScudetto())) {
                    countScudetto++;
                }
                if (squadra.equals(bean.getP2())) {
                    countP2++;
                }
                if (squadra.equals(bean.getP3())) {
                    countP3++;
                }
                if (squadra.equals(bean.getP4())) {
                    countP4++;
                }
                if (squadra.equals(bean.getP5())) {
                    countP5++;
                }
                if (squadra.equals(bean.getP6())) {
                    countP6++;
                }
                if (squadra.equals(bean.getP7())) {
                    countP7++;
                }
                if (squadra.equals(bean.getP8())) {
                    countP8++;
                }
                if (squadra.equals(bean.getWinClasPt())) {
                    countWinClasPt++;
                }
                if (squadra.equals(bean.getWinClasReg())) {
                    countWinClasReg++;
                }
                if (squadra.equals(bean.getWinClasTvsT())) {
                    countWinClasTvsT++;
                }
            }

            FcExpStat summary = new FcExpStat();
            summary.setAnno(squadra);
            summary.setScudetto(formatCount(countScudetto));
            summary.setP2(formatCount(countP2));
            summary.setP3(formatCount(countP3));
            summary.setP4(formatCount(countP4));
            summary.setP5(formatCount(countP5));
            summary.setP6(formatCount(countP6));
            summary.setP7(formatCount(countP7));
            summary.setP8(formatCount(countP8));
            summary.setWinClasPt(formatCount(countWinClasPt));
            summary.setWinClasReg(formatCount(countWinClasReg));
            summary.setWinClasTvsT(formatCount(countWinClasTvsT));

            result.add(summary);
        }

        return result;
    }

    private String formatCount(int count) {
        return count < 10 ? "0" + count : String.valueOf(count);
    }

    private static HorizontalLayout buildTripleteCell(FcExpStat stat) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);

        Span label = null;
        if (Objects.equals(stat.getScudetto(), stat.getWinClasPt())
                && Objects.equals(stat.getScudetto(), stat.getWinClasReg())) {
            label = new Span(stat.getScudetto());
            label.getStyle().set("fontSize", "smaller");
            cellLayout.add(label);
        }

        FcAttore currentAttore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
        if (currentAttore != null
                && Objects.equals(currentAttore.getDescAttore(), stat.getScudetto())
                && Objects.equals(currentAttore.getDescAttore(), stat.getWinClasPt())
                && Objects.equals(currentAttore.getDescAttore(), stat.getWinClasReg())
                && label != null) {
            label.getElement().getThemeList().add("badge contrast pill");
            cellLayout.getStyle().set("color", Costants.GRAY);
        }

        return cellLayout;
    }

    private static HorizontalLayout buildQuaDripleteCell(FcExpStat stat) {
        HorizontalLayout cellLayout = new HorizontalLayout();
        cellLayout.setMargin(false);
        cellLayout.setPadding(false);
        cellLayout.setSpacing(false);
        cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);

        Span label = null;
        if (Objects.equals(stat.getScudetto(), stat.getWinClasPt())
        		&& Objects.equals(stat.getScudetto(), stat.getWinClasReg())) {
            label = new Span(stat.getScudetto());
            label.getStyle().set("fontSize", "smaller");
            cellLayout.add(label);
        }

        FcAttore currentAttore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
        if (currentAttore != null
                && Objects.equals(currentAttore.getDescAttore(), stat.getScudetto())
                && Objects.equals(currentAttore.getDescAttore(), stat.getWinClasPt())
                && Objects.equals(currentAttore.getDescAttore(), stat.getWinClasReg())
                && Objects.equals(currentAttore.getDescAttore(), stat.getWinClasTvsT())
                && label != null) {
            label.getElement().getThemeList().add("badge contrast pill");
            cellLayout.getStyle().set("color", Costants.GRAY);
        }

        return cellLayout;
    }

    @SuppressWarnings("unchecked")
    private <T> T getSessionAttribute(String key, Class<T> type) {
        Object value = VaadinSession.getCurrent().getAttribute(key);
        return value == null ? null : (T) value;
    }
}
