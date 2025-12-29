package fcweb.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcProperties;

@Service
public class ProprietaService{

	private final ProprietaRepository proprietaRepository;

	@Autowired
	public ProprietaService(ProprietaRepository proprietaRepository) {
		this.proprietaRepository = proprietaRepository;
	}

	public List<FcProperties> findAll() {
		return (List<FcProperties>) proprietaRepository.findAll();
	}

	public FcProperties findByKey(String key) {
		return proprietaRepository.findByKey(key);
	}

	public FcProperties updateProprieta(FcProperties proprieta) {
		FcProperties fcProperties = null;
		try {
			fcProperties = proprietaRepository.save(proprieta);
		} catch (Exception ignored) {
		}
		return fcProperties;
	}

	public void deleteProprieta(FcProperties proprieta) {
        try {
			proprietaRepository.delete(proprieta);
        } catch (Exception ignored) {
		}
	}

}