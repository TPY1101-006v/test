package cl.duoc.monitoriza.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.monitoriza.model.Medicion;

@Repository
public interface MedicionRepository extends JpaRepository<Medicion, Long>{

    List<Medicion> findTop20ByOrderByFechaHoraDesc();

}
