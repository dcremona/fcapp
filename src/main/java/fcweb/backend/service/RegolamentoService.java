package fcweb.backend.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import fcweb.backend.data.entity.FcRegolamento;

@Service
public class RegolamentoService{

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	private final RegolamentoRepository regolamentoRepository;

	@Autowired
	public RegolamentoService(RegolamentoRepository regolamentoRepository) {
		this.regolamentoRepository = regolamentoRepository;
	}

	public List<FcRegolamento> findAll() {
		return (List<FcRegolamento>) regolamentoRepository.findAll(sortByIdDesc());
	}

	private Sort sortByIdDesc() {
		return Sort.by(Sort.Direction.DESC, "id");
	}

	public void insertUpdateRegolamento(FcRegolamento r) {

        try {
            regolamentoRepository.save(r);
        } catch (Exception ex) {
			log.error(ex.getMessage());
		}
	}

}