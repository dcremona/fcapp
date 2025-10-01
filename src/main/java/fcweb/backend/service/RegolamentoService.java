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

	private Logger LOG = LoggerFactory.getLogger(this.getClass());

	private final RegolamentoRepository regolamentoRepository;

	@Autowired
	public RegolamentoService(RegolamentoRepository regolamentoRepository) {
		this.regolamentoRepository = regolamentoRepository;
	}

	public List<FcRegolamento> findAll() {
		List<FcRegolamento> l = (List<FcRegolamento>) regolamentoRepository.findAll(sortByIdDesc());
		return l;
	}

	private Sort sortByIdDesc() {
		return Sort.by(Sort.Direction.DESC, "id");
	}

	public FcRegolamento insertRegolamento(FcRegolamento r) {

		FcRegolamento fcRegolamento = null;
		try {
			fcRegolamento = regolamentoRepository.save(r);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
		return fcRegolamento;
	}

	public FcRegolamento updateRegolamento(FcRegolamento r) {

		FcRegolamento fcRegolamento = null;
		try {
			fcRegolamento = regolamentoRepository.save(r);
		} catch (Exception ex) {
			LOG.error(ex.getMessage());
		}
		return fcRegolamento;
	}

	public String deleteRegolamento(FcRegolamento r) {
		String id = "";
		try {
			regolamentoRepository.delete(r);
			id = "" + r.getId();
		} catch (Exception ex) {
			return "Error delete Regolamento: " + ex.toString();
		}
		return "Regolamento succesfully delete with id = " + id;
	}

}