package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcAttore;
import fcweb.backend.data.entity.FcGiornataInfo;
import fcweb.backend.data.entity.FcGiornataRis;

@Service
public class GiornataRisService {

    private final GiornataRisRepository giornataRisRepository;

    @Autowired
    public GiornataRisService(GiornataRisRepository giornataRisRepository) {
        this.giornataRisRepository = giornataRisRepository;
    }

    public List<FcGiornataRis> findAll() {
        List<FcGiornataRis> l = (List<FcGiornataRis>) giornataRisRepository.findAll();
        return l;
    }

    public List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoDesc(FcAttore fcAttore) {
        List<FcGiornataRis> l = giornataRisRepository.findByFcAttoreOrderByFcGiornataInfoDesc(fcAttore);
        return l;
    }

    public List<FcGiornataRis> findByFcAttoreOrderByFcGiornataInfoAsc(FcAttore fcAttore) {
        List<FcGiornataRis> l = giornataRisRepository.findByFcAttoreOrderByFcGiornataInfoAsc(fcAttore);
        return l;
    }

    public List<FcGiornataRis> findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqual(FcGiornataInfo start,
            FcGiornataInfo end) {
        List<FcGiornataRis> l = giornataRisRepository
                .findByFcGiornataInfoGreaterThanEqualAndFcGiornataInfoLessThanEqual(start, end);
        return l;
    }

}