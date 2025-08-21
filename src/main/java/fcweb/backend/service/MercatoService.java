package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcMercatoDett;

@Service
public class MercatoService {

    private final MercatoRepository mercatoRepository;

    @Autowired
    public MercatoService(MercatoRepository mercatoRepository) {
        this.mercatoRepository = mercatoRepository;
    }

    public List<FcMercatoDett> findAll() {
        List<FcMercatoDett> l = (List<FcMercatoDett>) mercatoRepository.findAll(sortByGiornataInfoAndattoreAsc());
        return l;
    }

    public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDesc(FcAttore attore) {
        List<FcMercatoDett> l = mercatoRepository.findByFcAttoreOrderByFcGiornataInfoDesc(attore);
        return l;
    }

    public List<FcMercatoDett> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
            FcGiornataInfo from, FcGiornataInfo to, FcAttore attore) {
        List<FcMercatoDett> l = mercatoRepository
                .findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqualAndFcAttoreOrderByFcGiornataInfoDescIdDesc(
                        from, to, attore);
        return l;
    }

    public List<FcMercatoDett> findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(FcAttore attore) {
        List<FcMercatoDett> l = mercatoRepository.findByFcAttoreOrderByFcGiornataInfoDescDataCambioDesc(attore);
        return l;
    }

    public FcMercatoDett insertMercatoDett(FcMercatoDett c) {
        FcMercatoDett fcMercatoDett = null;
        try {
            fcMercatoDett = mercatoRepository.save(c);
        } catch (Exception ex) {
            return fcMercatoDett;
        }
        return fcMercatoDett;
    }

    public String deleteMercatoDett(FcMercatoDett c) {
        String id = "";
        try {
            mercatoRepository.delete(c);
            id = "" + c.getId();
        } catch (Exception ex) {
            return "Error delete : " + ex.toString();
        }
        return "FcMercatoDett succesfully delete with id = " + id;
    }

    private Sort sortByGiornataInfoAndattoreAsc() {
        return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcAttore", "id");
    }

}