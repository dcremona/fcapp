package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpRosea;

@Service
public class ExpRoseAService {

    private final ExpRoseARepository expRoseARepository;

    @Autowired
    public ExpRoseAService(ExpRoseARepository expRoseARepository) {
        this.expRoseARepository = expRoseARepository;
    }

    public List<FcExpRosea> findAll() {
        List<FcExpRosea> l = (List<FcExpRosea>) expRoseARepository.findAll(sortByIdAsc());
        return l;
    }

    private Sort sortByIdAsc() {
        return Sort.by(Sort.Direction.ASC, "id");
    }
}