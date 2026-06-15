package cl.duoc.monitoriza.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.monitoriza.model.Informe;

@Repository
public interface InformeRepository extends JpaRepository<Informe, Long>{
    Optional<Informe> findByFecha(LocalDate fecha);
    List<Informe> findAllByOrderByFechaDesc();
    boolean existsByFecha(LocalDate fecha);
}
