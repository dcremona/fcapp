package fcapp.ui.views.admin;

import java.io.File;
import java.io.Serial;
import java.sql.SQLException;

import com.vaadin.flow.component.checkbox.Checkbox;
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

import fcapp.backend.data.entity.FcCampionato;
import fcapp.backend.data.entity.FcGiocatore;
import fcapp.backend.data.entity.FcRuolo;
import fcapp.backend.data.entity.FcSquadra;
import fcapp.backend.service.AccessoService;
import fcapp.backend.service.GiocatoreService;
import fcapp.backend.service.RuoloService;
import fcapp.backend.service.SquadraService;
import fcapp.ui.views.MainLayout;
import fcapp.utils.Costants;
import fcapp.utils.CustomMessageDialog;
import fcapp.utils.Utils;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;

@PageTitle(Costants.GIOCATORE)
@Route(value = Costants.GIOCATORE, layout = MainLayout.class)
@RolesAllowed("ADMIN")
public class FcGiocatoreView extends VerticalLayout{

	@Serial
    private static final long serialVersionUID = 1L;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	public Environment env;

	private final GiocatoreService giocatoreService;
	private final SquadraService squadraService;
	private final RuoloService ruoloService;
	private final AccessoService accessoService;

	private final ComboBox<FcRuolo> ruoloFilter = new ComboBox<>();
	private final ComboBox<FcSquadra> squadraFilter = new ComboBox<>();

	public FcGiocatoreView(GiocatoreService giocatoreService,SquadraService squadraService,RuoloService ruoloService,AccessoService accessoService) {
		log.info("FcGiocatoreView()");
		this.giocatoreService = giocatoreService;
		this.squadraService = squadraService;
		this.ruoloService = ruoloService;
		this.accessoService = accessoService;
	}

	@PostConstruct
	void init() {
		log.info("init");
		if (!Utils.isValidVaadinSession()) {
			return;
		}
		accessoService.insertAccesso(this.getClass().getName());
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
						log.error(e.getMessage());
					}

					Image imgOnline = new Image(Costants.HTTP_URL_IMG + g.getNomeImg(),g.getNomeImg());
					cellLayout.add(imgOnline);

					Button updateImg = new Button("Salva");
					updateImg.setIcon(VaadinIcon.DATABASE.create());
					updateImg.addClickListener(event -> {
						try {
							String basePathData = env.getProperty("PATH_TMP");
                            log.info("basePathData {}", basePathData);

                            assert basePathData != null;
                            File f = new File(basePathData);
							if (!f.exists()) {
								CustomMessageDialog.showMessageErrorDetails(CustomMessageDialog.MSG_ERROR_GENERIC, "Impossibile trovare il percorso specificato " + basePathData);
								return;
							}

							String newImg = g.getNomeImg();
                            log.info("newImg {}", newImg);
							log.info("httpUrlImg " + Costants.HTTP_URL_IMG);

                            boolean flag = Utils.downloadFile(Costants.HTTP_URL_IMG + newImg, basePathData + newImg);
                            log.info("bResult 1 {}", flag);
							flag = Utils.buildFileSmall(basePathData + newImg, basePathData + "small-" + newImg);
                            log.info("bResult 2 {}", flag);

							g.setImg(BlobProxy.generateProxy(Utils.getImage(basePathData + newImg)));
							g.setImgSmall(BlobProxy.generateProxy(Utils.getImage(basePathData + "small-" + newImg)));

							log.info("SAVE GIOCATORE ");
							giocatoreService.save(g);

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

		Column<FcGiocatore> giocatoreColumn = crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : g.getCognGiocatore())).setHeader(Costants.GIOCATORE);
		giocatoreColumn.setSortable(false);
		giocatoreColumn.setAutoWidth(true);

		Column<FcGiocatore> squadraColumn = crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : g.getFcSquadra().getNomeSquadra())).setHeader(Costants.SQUADRA);
		squadraColumn.setSortable(false);
		squadraColumn.setAutoWidth(true);

		crud.getGrid().addColumn(new TextRenderer<>(g -> g == null ? "" : "" + g.getQuotazione())).setHeader("Quotazione");

		crud.getGrid().addColumn(new ComponentRenderer<>(g -> {
			Checkbox check = new Checkbox();
			check.setValue(g.isFlagAttivo());
			check.setEnabled(false);
			return check;
		})).setHeader("Attivo");

		crud.getGrid().setColumnReorderingAllowed(true);

		formFactory.setFieldProvider("fcSquadra", new ComboBoxProvider<>("fcSquadra",squadraService.findAll(),new TextRenderer<>(FcSquadra::getNomeSquadra),FcSquadra::getNomeSquadra));
		formFactory.setFieldProvider("fcRuolo", new ComboBoxProvider<>("fcRuolo",ruoloService.findAll(),new TextRenderer<>(FcRuolo::getDescRuolo),FcRuolo::getDescRuolo));

		crud.setRowCountCaption("%d Giocatore(s) found");
		crud.setClickRowToUpdate(true);
		crud.setUpdateOperationVisible(true);

		ruoloFilter.setPlaceholder(Costants.RUOLO);
		ruoloFilter.setItems(ruoloService.findAll());
		ruoloFilter.setItemLabelGenerator(FcRuolo::getIdRuolo);
		ruoloFilter.setClearButtonVisible(true);
		ruoloFilter.addValueChangeListener(e -> crud.refreshGrid());
		crud.getCrudLayout().addFilterComponent(ruoloFilter);

		squadraFilter.setPlaceholder(Costants.SQUADRA);
		squadraFilter.setItems(squadraService.findAll());
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

		crud.setFindAllOperation(() -> giocatoreService.findByFcRuoloAndFcSquadraOrderByQuotazioneDesc(ruoloFilter.getValue(), squadraFilter.getValue()));
		crud.setAddOperation(giocatoreService::save);
		crud.setUpdateOperation(giocatoreService::save);
		crud.setDeleteOperation(giocatoreService::delete);

		add(crud);

	}
}