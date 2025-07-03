package fcweb.ui.views.seriea;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

import common.util.Utils;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcExpStat;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.AlboService;
import fcweb.backend.service.AttoreService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;;

@PageTitle("Albo")
@Route(value = "albo", layout = MainLayout.class)
@RolesAllowed("USER")
public class AlboView extends VerticalLayout {

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AlboService alboController;

	@Autowired
	private AttoreService attoreController;

	@Autowired
	private AccessoService accessoController;

	public AlboView() {
		log.info("AlboView()");
	}

	@PostConstruct
	void init() {
		log.debug("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());

		initLayout();
	}

	private void initLayout() {

		List<FcExpStat> items = alboController.findAll();
		this.add(getGrid(items));

		List<FcExpStat> modelCrosstab = getModelCrosstab(items);
		modelCrosstab.sort((p1, p2) -> p2.getScudetto().compareToIgnoreCase(p1.getScudetto()));
		this.add(getGrid2(modelCrosstab));

	}

	private Grid<FcExpStat> getGrid(List<FcExpStat> items) {

		Grid<FcExpStat> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);
		grid.setAllRowsVisible(true);

		Column<FcExpStat> campionatoColumn = grid.addColumn(s -> s.getAnno() + " " + s.getCampionato());
		campionatoColumn.setSortable(false);
		campionatoColumn.setResizable(false);
		campionatoColumn.setHeader("Campionato");
		campionatoColumn.setWidth("150px");

		Column<FcExpStat> scudettoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);

			Span lblAttore = new Span(s.getScudetto());
			if (att.getDescAttore().equals(s.getScudetto())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge success");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		scudettoColumn.setSortable(false);
		scudettoColumn.setResizable(false);
		scudettoColumn.setHeader("Scudetto");

		Column<FcExpStat> p2Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			Span lblAttore = new Span(s.getP2());
			if (att.getDescAttore().equals(s.getP2())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge pill");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p2Column.setSortable(false);
		p2Column.setResizable(false);
		p2Column.setHeader("Finalista");

		Column<FcExpStat> p3Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if (att.getDescAttore().equals(s.getP3())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			}
			Span lblAttore = new Span(s.getP3());
			lblAttore.getStyle().set("fontSize", "smaller");
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p3Column.setSortable(false);
		p3Column.setResizable(false);
		p3Column.setHeader("3 Posto");

		Column<FcExpStat> p4Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if (att.getDescAttore().equals(s.getP4())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			}
			Span lblAttore = new Span(s.getP4());
			lblAttore.getStyle().set("fontSize", "smaller");
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p4Column.setSortable(false);
		p4Column.setResizable(false);
		p4Column.setHeader("4 Posto");

		Column<FcExpStat> p5Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if (att.getDescAttore().equals(s.getP5())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			}
			Span lblAttore = new Span(s.getP5());
			lblAttore.getStyle().set("fontSize", "smaller");
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p5Column.setSortable(false);
		p5Column.setResizable(false);
		p5Column.setHeader("5 Posto");

		Column<FcExpStat> p6Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if (att.getDescAttore().equals(s.getP6())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			}
			Span lblAttore = new Span(s.getP6());
			lblAttore.getStyle().set("fontSize", "smaller");
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p6Column.setSortable(false);
		p6Column.setResizable(false);
		p6Column.setHeader("6 Posto");

		Column<FcExpStat> p7Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			if (att.getDescAttore().equals(s.getP7())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
			}
			Span lblAttore = new Span(s.getP7());
			lblAttore.getStyle().set("fontSize", "smaller");
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p7Column.setSortable(false);
		p7Column.setResizable(false);
		p7Column.setHeader("7 Posto");

		Column<FcExpStat> p8Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			Span lblAttore = new Span(s.getP8());
			if (att.getDescAttore().equals(s.getP8())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge error");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		p8Column.setSortable(false);
		p8Column.setResizable(false);
		p8Column.setHeader("8 Posto");

		Column<FcExpStat> winClasPtColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			Span lblAttore = new Span(s.getWinClasPt());
			if (att.getDescAttore().equals(s.getWinClasPt())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge success");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		winClasPtColumn.setSortable(false);
		winClasPtColumn.setResizable(false);
		winClasPtColumn.setHeader("Clas Punti");

		Column<FcExpStat> winClasRegColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			Span lblAttore = new Span(s.getWinClasReg());
			if (att.getDescAttore().equals(s.getWinClasReg())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge success");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		winClasRegColumn.setSortable(false);
		winClasRegColumn.setResizable(false);
		winClasRegColumn.setHeader("Clas Regolare");

		Column<FcExpStat> winTvsTColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			Span lblAttore = new Span(s.getWinClasTvsT());
			if (att.getDescAttore().equals(s.getWinClasTvsT())) {
				cellLayout.getStyle().set("color", Costants.GRAY);
				lblAttore.getElement().getThemeList().add("badge success");
			} else {
				lblAttore.getStyle().set("fontSize", "smaller");
			}
			cellLayout.add(lblAttore);
			return cellLayout;
		}));
		winTvsTColumn.setSortable(false);
		winTvsTColumn.setResizable(false);
		winTvsTColumn.setHeader("Clas TvsT");

		Column<FcExpStat> tripleteColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			cellLayout.setMargin(false);
			cellLayout.setPadding(false);
			cellLayout.setSpacing(false);
			Span lblAttore = null;
			if (s.getScudetto().equals(s.getWinClasPt()) && s.getScudetto().equals(s.getWinClasReg())) {
				lblAttore = new Span(s.getScudetto());
				lblAttore.getStyle().set("fontSize", "smaller");
				cellLayout.add(lblAttore);
			}

			cellLayout.getStyle().set("color", Costants.LIGHT_GRAY);
			FcAttore att = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");
			if (att.getDescAttore().equals(s.getScudetto()) && att.getDescAttore().equals(s.getWinClasPt())
					&& att.getDescAttore().equals(s.getWinClasReg())) {
				lblAttore.getElement().getThemeList().add("badge contrast pill");
				cellLayout.getStyle().set("color", Costants.GRAY);
			}

			return cellLayout;
		}));
		tripleteColumn.setSortable(false);
		tripleteColumn.setResizable(false);
		tripleteColumn.setHeader("Triplete");

		return grid;
	}

	private Grid<FcExpStat> getGrid2(List<FcExpStat> items) {

		Grid<FcExpStat> grid = new Grid<>();
		grid.setItems(items);
		grid.setSelectionMode(Grid.SelectionMode.SINGLE);
		grid.addThemeVariants(GridVariant.LUMO_NO_BORDER, GridVariant.LUMO_NO_ROW_BORDERS,
				GridVariant.LUMO_ROW_STRIPES);
		grid.setAllRowsVisible(true);

		Column<FcExpStat> annoColumn = grid.addColumn(s -> s.getAnno());
		annoColumn.setSortable(true);
		annoColumn.setHeader("Squadra");

		Column<FcExpStat> scudettoColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getScudetto())) {
				Span lbl = new Span("" + Integer.parseInt(s.getScudetto()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		scudettoColumn.setSortable(true);
		scudettoColumn.setComparator((p1, p2) -> p1.getScudetto().compareTo(p2.getScudetto()));
		scudettoColumn.setHeader("Scudetto");

		Column<FcExpStat> p2Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP2())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP2()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p2Column.setSortable(true);
		p2Column.setComparator((p1, p2) -> p1.getP2().compareTo(p2.getP2()));
		p2Column.setHeader("Finalista");

		Column<FcExpStat> p3Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP3())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP3()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p3Column.setSortable(true);
		p3Column.setComparator((p1, p2) -> p1.getP3().compareTo(p2.getP3()));
		p3Column.setHeader("3 Posto");

		Column<FcExpStat> p4Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP4())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP4()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p4Column.setSortable(true);
		p4Column.setComparator((p1, p2) -> p1.getP4().compareTo(p2.getP4()));
		p4Column.setHeader("4 Posto");

		Column<FcExpStat> p5Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP5())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP5()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p5Column.setSortable(true);
		p5Column.setComparator((p1, p2) -> p1.getP5().compareTo(p2.getP5()));
		p5Column.setHeader("5 Posto");

		Column<FcExpStat> p6Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP6())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP6()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p6Column.setSortable(true);
		p6Column.setComparator((p1, p2) -> p1.getP6().compareTo(p2.getP6()));
		p6Column.setHeader("6 Posto");

		Column<FcExpStat> p7Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP7())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP7()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p7Column.setSortable(true);
		p7Column.setComparator((p1, p2) -> p1.getP7().compareTo(p2.getP7()));
		p7Column.setHeader("7 Posto");

		Column<FcExpStat> p8Column = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getP8())) {
				Span lbl = new Span("" + Integer.parseInt(s.getP8()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		p8Column.setSortable(true);
		p8Column.setComparator((p1, p2) -> p1.getP8().compareTo(p2.getP8()));
		p8Column.setHeader("8 Posto");

		Column<FcExpStat> winClasPtColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getWinClasPt())) {
				Span lbl = new Span("" + Integer.parseInt(s.getWinClasPt()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		winClasPtColumn.setSortable(true);
		winClasPtColumn.setComparator((p1, p2) -> p1.getWinClasPt().compareTo(p2.getWinClasPt()));
		winClasPtColumn.setHeader("Clas Punti");

		Column<FcExpStat> winClasRegColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getWinClasReg())) {
				Span lbl = new Span("" + Integer.parseInt(s.getWinClasReg()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		winClasRegColumn.setSortable(true);
		winClasRegColumn.setComparator((p1, p2) -> p1.getWinClasReg().compareTo(p2.getWinClasReg()));
		winClasRegColumn.setHeader("Clas Regolare");

		Column<FcExpStat> winClasTvsTColumn = grid.addColumn(new ComponentRenderer<>(s -> {
			HorizontalLayout cellLayout = new HorizontalLayout();
			if (s != null && !StringUtils.isEmpty(s.getWinClasTvsT())) {
				Span lbl = new Span("" + Integer.parseInt(s.getWinClasTvsT()));
				cellLayout.add(lbl);
			}
			return cellLayout;
		}));
		winClasTvsTColumn.setSortable(true);
		winClasTvsTColumn.setComparator((p1, p2) -> p1.getWinClasTvsT().compareTo(p2.getWinClasTvsT()));
		winClasTvsTColumn.setHeader("Clas TvsT");

		return grid;

	}

	private List<FcExpStat> getModelCrosstab(List<FcExpStat> all) {

		List<FcExpStat> beans = new ArrayList<>();

		List<FcAttore> squadre = attoreController.findAll();

		for (FcAttore attore : squadre) {

			String squadra = attore.getDescAttore();

			int count_scudetto = 0;
			int count_p2 = 0;
			int count_p3 = 0;
			int count_p4 = 0;
			int count_p5 = 0;
			int count_p6 = 0;
			int count_p7 = 0;
			int count_p8 = 0;
			int count_win_clas_pt = 0;
			int count_win_clas_reg = 0;
			int count_win_clas_tvst = 0;

			for (FcExpStat bean : all) {

				if (bean.getScudetto().equals(squadra)) {
					count_scudetto++;
				}
				if (bean.getP2().equals(squadra)) {
					count_p2++;
				}
				if (bean.getP3().equals(squadra)) {
					count_p3++;
				}
				if (bean.getP4().equals(squadra)) {
					count_p4++;
				}
				if (bean.getP5().equals(squadra)) {
					count_p5++;
				}
				if (bean.getP6().equals(squadra)) {
					count_p6++;
				}
				if (bean.getP7().equals(squadra)) {
					count_p7++;
				}
				if (bean.getP8().equals(squadra)) {
					count_p8++;
				}
				if (bean.getWinClasPt().equals(squadra)) {
					count_win_clas_pt++;
				}
				if (bean.getWinClasReg().equals(squadra)) {
					count_win_clas_reg++;
				}
				if (bean.getWinClasTvsT().equals(squadra)) {
					count_win_clas_tvst++;
				}
			}

			FcExpStat b = new FcExpStat();
			b.setAnno(squadra);
			b.setScudetto(count_scudetto < 10 ? "0" + count_scudetto : "" + count_scudetto);
			b.setP2(count_p2 < 10 ? "0" + count_p2 : "" + count_p2);
			b.setP3(count_p3 < 10 ? "0" + count_p3 : "" + count_p3);
			b.setP4(count_p4 < 10 ? "0" + count_p4 : "" + count_p4);
			b.setP5(count_p5 < 10 ? "0" + count_p5 : "" + count_p5);
			b.setP6(count_p6 < 10 ? "0" + count_p6 : "" + count_p6);
			b.setP7(count_p7 < 10 ? "0" + count_p7 : "" + count_p7);
			b.setP8(count_p8 < 10 ? "0" + count_p8 : "" + count_p8);

			b.setWinClasPt(count_win_clas_pt < 10 ? "0" + count_win_clas_pt : "" + count_win_clas_pt);
			b.setWinClasReg(count_win_clas_reg < 10 ? "0" + count_win_clas_reg : "" + count_win_clas_reg);
			b.setWinClasTvsT(count_win_clas_tvst < 10 ? "0" + count_win_clas_tvst : "" + count_win_clas_tvst);

			beans.add(b);
		}

		return beans;
	}

}