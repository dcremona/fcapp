package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcMercatoDettInfo;

@Service
public class MercatoInfoService {

    private final MercatoInfoRepository mercatoInfoRepository;

    @Autowired
    public MercatoInfoService(MercatoInfoRepository mercatoInfoRepository) {
        this.mercatoInfoRepository = mercatoInfoRepository;
    }

    public List<FcMercatoDettInfo> findAll() {
        List<FcMercatoDettInfo> l = (List<FcMercatoDettInfo>) mercatoInfoRepository
                .findAll(sortByGiornataInfoAndattoreAsc());
        return l;
    }

    private Sort sortByGiornataInfoAndattoreAsc() {
        return Sort.by(Sort.Direction.ASC, "fcGiornataInfo", "fcAttore");
    }

    public List<FcMercatoDettInfo> findByFcAttoreOrderByFcGiornataInfoAsc(FcAttore fcAttore) {
        List<FcMercatoDettInfo> l = mercatoInfoRepository.findByFcAttoreOrderByFcGiornataInfoAsc(fcAttore);

        return l;
    }

    public FcMercatoDettInfo insertMercatoDettInfo(FcMercatoDettInfo mercatoInfo) {
        FcMercatoDettInfo fcMercatoDettInfo = null;
        try {
            fcMercatoDettInfo = mercatoInfoRepository.save(mercatoInfo);
        } catch (Exception ex) {
            return fcMercatoDettInfo;
        }
        return fcMercatoDettInfo;
    }
}