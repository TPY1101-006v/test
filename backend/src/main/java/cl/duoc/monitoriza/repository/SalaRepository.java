package cl.duoc.monitoriza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.monitoriza.model.Sala;

@Repository
public interface SalaRepository extends JpaRepository<Sala, Long>{

}
