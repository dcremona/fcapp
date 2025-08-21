package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcProperties;

@Service
public class ProprietaService {

    private final ProprietaRepository proprietaRepository;

    @Autowired
    public ProprietaService(ProprietaRepository proprietaRepository) {
        this.proprietaRepository = proprietaRepository;
    }

    public List<FcProperties> findAll() {
        List<FcProperties> l = (List<FcProperties>) proprietaRepository.findAll();
        return l;
    }

    public FcProperties findByKey(String key) {
        FcProperties l = proprietaRepository.findByKey(key);
        return l;
    }

    public FcProperties updateProprieta(FcProperties proprieta) {
        FcProperties fcProperties = null;
        try {
            fcProperties = proprietaRepository.save(proprieta);
        } catch (Exception ex) {

        }
        return fcProperties;
    }

    public String deleteProprieta(FcProperties proprieta) {
        String id = "";
        try {
            proprietaRepository.delete(proprieta);
            id = "" + proprieta.getKey();
        } catch (Exception ex) {
            return "Error delete proprieta: " + ex.toString();
        }
        return "proprieta succesfully delete with id = " + id;
    }

}