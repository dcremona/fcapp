package fcweb.backend.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import fcweb.backend.data.entity.FcAttore;

public interface AttoreRepository extends JpaRepository<FcAttore, Long>,
		JpaSpecificationExecutor<FcAttore>{

	FcAttore findByUsername(String username);

	public List<FcAttore> findByActive(boolean active);
}
