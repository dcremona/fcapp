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
public class AccessoService {

    private Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final AccessoRepository accessoRepository;

    @Autowired
    public AccessoService(AccessoRepository accessoRepository) {
        this.accessoRepository = accessoRepository;
    }

    public List<FcAccesso> findAll() {
        List<FcAccesso> l = (List<FcAccesso>) accessoRepository.findAll(sortByIdDesc());
        return l;
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
        LOG.info("now : " + now + " attore " + attore.getDescAttore() + " note " + note);
        return fcAccesso;
    }

    public FcAccesso updateAccesso(FcAccesso accesso) {
        FcAccesso fcAccesso = null;
        try {
            fcAccesso = accessoRepository.save(accesso);
        } catch (Exception ex) {
        }
        return fcAccesso;
    }

    public String deleteAccesso(FcAccesso accesso) {
        String id = "";
        try {
            accessoRepository.delete(accesso);
            id = "" + accesso.getId();
        } catch (Exception ex) {
            return "Error delete accesso: " + ex.toString();
        }
        return "accesso succesfully delete with id = " + id;
    }

}