package fcweb.ui.views.admin;

import java.io.File;
import java.sql.SQLException;
import java.util.Properties;

import org.hibernate.engine.jdbc.BlobProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.field.provider.ComboBoxProvider;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.crudui.layout.impl.HorizontalSplitCrudLayout;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import common.util.Utils;
import fcweb.backend.data.entity.FcCampionato;
import fcweb.backend.data.entity.FcGiocatore;
import fcweb.backend.data.entity.FcRuolo;
import fcweb.backend.data.entity.FcSquadra;
import fcweb.backend.service.AccessoService;
import fcweb.backend.service.GiocatoreService;
import fcweb.backend.service.RuoloService;
import fcweb.backend.service.SquadraService;
import fcweb.ui.views.MainLayout;
import fcweb.utils.Costants;
import fcweb.utils.CustomMessageDialog;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle(Costants.GIOCATORE)
@Route(value = Costants.GIOCATORE, layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiocatoreView extends VerticalLayout{

	private static final long serialVersionUID = 1L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GiocatoreService giocatoreController;

	@Autowired
	private SquadraService squadraController;

	@Autowired
	private RuoloService ruoloController;

	@Autowired
	public Environment env;

	@Autowired
	private AccessoService accessoController;

	private ComboBox<FcRuolo> ruoloFilter = new ComboBox<>();
	private ComboBox<FcSquadra> squadraFilter = new ComboBox<>();

	public FcGiocatoreView() {
		log.info("FcGiocatoreView()");
	}

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoController.insertAccesso(this.getClass().getName());
		initLayout();
	}

	private void initLayout() {

		this.setMargin(true);
		this.setSpacing(true);
		this.setSizeFull();

		GridCrud<FcGiocatore> crud = new GridCrud<>(FcGiocatore.class,new HorizontalSplitCrudLayout());

		DefaultCrudFormFactory<FcGiocatore> formFactory = new DefaultCrudFormFactory<>(FcGiocatore.class);
		crud.setCrudFormFactory(formFactory);
		formFactory.setUseBeanValidation(false);

		formFactory.setVisibleProperties(CrudOperation.READ, "idGiocatore", "cognGiocatore", "quotazione", "nomeImg", "fcSquadra", "fcRuolo", "flagAttivo", "quotazione");
		formFactory.setVisibleProperties(CrudOperation.ADD, "idGiocatore", "cognGiocatore", "nomeImg", "fcSquadra", "fcRuolo", "flagAttivo", "quotazione");
		formFactory.setVisibleProperties(CrudOperation.UPDATE, "cognGiocatore", "quotazione", "nomeImg", "fcSquadra", "fcRuolo", "flagAttivo", "quotazione");
		formFactory.setVisibleProperties(CrudOperation.DELETE, "idGiocatore", "cognGiocatore");

		crud.getGrid().removeAllColumns();

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		if ("1".equals(campionato.getType())) {
			Column<FcGiocatore> giocatreColumn = crud.getGrid().addColumn(new ComponentRenderer<>(g -> {
				HorizontalLayout cellLayout = new HorizontalLayout();
				cellLayout.setSizeFull();
				if (g != null && g.getNomeImg() != null) {
					try {
						Image img = Utils.getImage(g.getNomeImg(), g.getImg().getBinaryStream());
						cellLayout.add(img);
					} catch (SQLException e) {
						e.printStackTrace();
					}

					Image imgOnline = new Image(Costants.HTTP_URL_IMG + g.getNomeImg(),g.getNomeImg());
					cellLayout.add(imgOnline);

					Button updateImg = new Button("Salva");
					updateImg.setIcon(VaadinIcon.DATABASE.create());
					updateImg.addClickListener(event -> {
						try {
							Properties p = (Properties) VaadinSession.getCurrent().getAttribute("PROPERTIES");
							String basePathData = (String) p.get("PATH_TMP");
							log.info("basePathData " + basePathData);

							File f = new File(basePathData);
							if (!f.exists()) {
								CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Impossibile trovare il percorso specificato " + basePathData);
								return;
							}

							String newImg = g.getNomeImg();
							log.info("newImg " + newImg);
							log.info("httpUrlImg " + Costants.HTTP_URL_IMG);
							String imgPath = basePathData;

							boolean flag = Utils.downloadFile(Costants.HTTP_URL_IMG + newImg, imgPath + newImg);
							log.info("bResult 1 " + flag);
							flag = Utils.buildFileSmall(imgPath + newImg, imgPath + "small-" + newImg);
							log.info("bResult 2 " + flag);

							g.setImg(BlobProxy.generateProxy(Utils.getImage(imgPath + newImg)));
							g.setImgSmall(BlobProxy.generateProxy(Utils.getImage(imgPath + "small-" + newImg)));

							log.info("SAVE GIOCATORE ");
							giocatoreController.updateGiocatore(g);

							CustomMessageDialog.showMessageInfo(CustomMessageDialog.MSG_OK);
						} catch (Exception e) {
							CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, e.getMessage());
						}
					});

					cellLayout.add(updateImg);
				}
				return cellLayout;
			}));
			giocatreColumn.setWidth("350px");
		}

		crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : "" + g.getIdGiocatore())).setHeader("Id");
		crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : g.getFcRuolo().getIdRuolo())).setHeader(Costants.RUOLO);

		Column<FcGiocatore> giocatoreColumn = crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : "" + g.getCognGiocatore())).setHeader(Costants.GIOCATORE);
		giocatoreColumn.setSortable(false);
		giocatoreColumn.setAutoWidth(true);

		Column<FcGiocatore> squadraColumn = crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : g.getFcSquadra().getNomeSquadra())).setHeader(Costants.SQUADRA);
		squadraColumn.setSortable(false);
		squadraColumn.setAutoWidth(true);

		crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : "" + g.getQuotazione())).setHeader("Quotazione");

		crud.getGrid().setColumnReorderingAllowed(true);

		formFactory.setFieldProvider("fcSquadra", new ComboBoxProvider<>("fcSquadra",squadraController.findAll(),new TextRenderer<>(FcSquadra::getNomeSquadra),FcSquadra::getNomeSquadra));
		formFactory.setFieldProvider("fcRuolo", new ComboBoxProvider<>("fcRuolo",ruoloController.findAll(),new TextRenderer<>(FcRuolo::getDescRuolo),FcRuolo::getDescRuolo));

		crud.setRowCountCaption("%d Giocatore(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		ruoloFilter.setPlaceholder(Costants.RUOLO);
		ruoloFilter.setItems(ruoloController.findAll());
		ruoloFilter.setItemLabelGenerator(FcRuolo::getIdRuolo);
		ruoloFilter.setClearButtonVisible(true);
		ruoloFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(ruoloFilter);

		squadraFilter.setPlaceholder(Costants.SQUADRA);
		squadraFilter.setItems(squadraController.findAll());
		squadraFilter.setItemLabelGenerator(FcSquadra::getNomeSquadra);
		squadraFilter.setClearButtonVisible(true);
		squadraFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(squadraFilter);

		Button clearFilters = new Button("clear");
		clearFilters.addClickListener(event -> {
			ruoloFilter.clear();
			squadraFilter.clear();
		});
		crud.getCrudLayout().addFilterComponent(clearFilters);

		crud.setFindAllOperation(() -> giocatoreController.findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(ruoloFilter.getValue(), squadraFilter.getValue()));
		crud.setAddOperation(g -> giocatoreController.updateGiocatore(g));
		crud.setUpdateOperation(g -> giocatoreController.updateGiocatore(g));
		crud.setDeleteOperation(g -> giocatoreController.deleteGiocatore(g));

		add(crud);

	}
}