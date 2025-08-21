package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcExpStat;

@Service
public class ExpStatService {

    private final ExpStatRepository expStatRepository;

    @Autowired
    public ExpStatService(ExpStatRepository expStatRepository) {
        this.expStatRepository = expStatRepository;
    }

    public List<FcExpStat> findAll() {
        List<FcExpStat> l = (List<FcExpStat>) expStatRepository.findAll(sortByIdAsc());
        return l;
    }

    private Sort sortByIdAsc() {
        return Sort.by(Sort.Direction.ASC, "id");
    }

    public FcExpStat updateExpStat(FcExpStat expStat) {
        FcExpStat fcExpStat = null;
        try {
            fcExpStat = expStatRepository.save(expStat);
        } catch (Exception ex) {
        }
        return fcExpStat;
    }

    public String deleteExpStat(FcExpStat expStat) {
        String id = "";
        try {
            expStatRepository.delete(expStat);
            id = "" + expStat.getId();
        } catch (Exception ex) {
            return "Error delete FcExpStat: " + ex.toString();
        }
        return "expStat succesfully delete with id = " + id;
    }

}