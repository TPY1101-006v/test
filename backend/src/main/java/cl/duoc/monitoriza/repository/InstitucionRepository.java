package cl.duoc.monitoriza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.monitoriza.model.Institucion;

@Repository
public interface InstitucionRepository extends JpaRepository<Institucion, Long>{

}
