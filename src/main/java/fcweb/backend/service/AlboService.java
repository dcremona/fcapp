package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpStat;

@Service
public class AlboService {

    private final AlboRepository alboRepository;

    @Autowired
    public AlboService(AlboRepository alboRepository) {
        this.alboRepository = alboRepository;
    }

    public List<FcExpStat> findAll() {
        List<FcExpStat> l = (List<FcExpStat>) alboRepository.findAll(sortByIdAsc());
        return l;
    }

    private Sort sortByIdAsc() {
        return Sort.by(Sort.Direction.DESC, "id");
    }

}