package fcapp.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fcapp.backend.data.entity.FcProperties;

@Service
public class ProprietaService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	private final ProprietaRepository proprietaRepository;

	public ProprietaService(ProprietaRepository proprietaRepository) {
		this.proprietaRepository = proprietaRepository;
	}

	public List<FcProperties> findAll() {
		return (List<FcProperties>) proprietaRepository.findAll();
	}

	public FcProperties findByKey(String key) {
		return proprietaRepository.findByKey(key);
	}

	public FcProperties save(FcProperties proprieta) {
		FcProperties fcProperties = null;
		try {
			fcProperties = proprietaRepository.save(proprieta);
		} catch (Exception ex) {
			log.error(ex.getMessage());
		}
		return fcProperties;
	}

	public void delete(FcProperties proprieta) {
        try {
			proprietaRepository.delete(proprieta);
        } catch (Exception ex) {
        	log.error(ex.getMessage());
		}
	}

}