package cl.duoc.monitoriza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import cl.duoc.monitoriza.model.Alerta;
import java.util.List;

@Repository
public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    // Esto nos traerá las alertas más recientes primero para el historial
    List<Alerta> findTop15ByOrderByIdDesc();
}