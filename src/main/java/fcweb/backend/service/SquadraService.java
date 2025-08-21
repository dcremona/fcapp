package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcSquadra;

@Service
public class SquadraService {

    private final SquadraRepository squadraRepository;

    @Autowired
    public SquadraService(SquadraRepository squadraRepository) {
        this.squadraRepository = squadraRepository;
    }

    public List<FcSquadra> findAll() {
        List<FcSquadra> l = (List<FcSquadra>) squadraRepository.findAll(sortByIdSquadra());
        return l;
    }

    public FcSquadra findByNomeSquadra(String nomeSquadra) {
        FcSquadra fcSquadra = squadraRepository.findByNomeSquadra(nomeSquadra);
        return fcSquadra;
    }

    public FcSquadra findByIdSquadra(int idSquadra) {
        FcSquadra fcSquadra = squadraRepository.findByIdSquadra(idSquadra);
        return fcSquadra;
    }

    private Sort sortByIdSquadra() {
        return Sort.by(Sort.Direction.ASC, "idSquadra");
    }

    public FcSquadra updateSquadra(FcSquadra c) {
        FcSquadra Squadra = null;
        try {
            Squadra = squadraRepository.save(c);
        } catch (Exception ex) {
            return Squadra;
        }
        return Squadra;
    }

    public String deleteSquadra(FcSquadra c) {
        String id = "";
        try {
            squadraRepository.delete(c);
            id = "" + c.getIdSquadra();
        } catch (Exception ex) {
            return "Error delete : " + ex.toString();
        }
        return "Squadra succesfully delete with id = " + id;
    }

}