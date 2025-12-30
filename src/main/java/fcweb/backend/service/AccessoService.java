package fcweb.backend.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.vaadin.flow.server.VaadinSession;

import fcweb.backend.data.entity.FcAccesso;
import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcCampionato;

@Service
public class AccessoService{

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final AccessoRepository accessoRepository;

	@Autowired
	public AccessoService(AccessoRepository accessoRepository) {
		this.accessoRepository = accessoRepository;
	}

	public List<FcAccesso> findAll() {
		return (List<FcAccesso>) accessoRepository.findAll(sortByIdDesc());
	}

	private Sort sortByIdDesc() {
		return Sort.by(Sort.Direction.DESC, "id");
	}

	public FcAccesso insertAccesso(String note) {

		FcCampionato campionato = (FcCampionato) VaadinSession.getCurrent().getAttribute("CAMPIONATO");
		FcAttore attore = (FcAttore) VaadinSession.getCurrent().getAttribute("ATTORE");

		LocalDateTime now = LocalDateTime.now();

		FcAccesso a = new FcAccesso();
		a.setFcAttore(attore);
		a.setData(now);
		a.setNote(note);
		a.setFcCampionato(campionato);

		FcAccesso fcAccesso = null;
		try {
			fcAccesso = accessoRepository.save(a);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
        LOG.info("now : {} attore {} note {}", now, attore.getDescAttore(), note);
		return fcAccesso;
	}

	public FcAccesso updateAccesso(FcAccesso accesso) {
		FcAccesso fcAccesso = null;
		try {
			fcAccesso = accessoRepository.save(accesso);
		} catch (Exception ignored) {
		}
		return fcAccesso;
	}

	public void deleteAccesso(FcAccesso accesso) {
        try {
			accessoRepository.delete(accesso);
        } catch (Exception ignored) {

		}
	}

}