package cl.duoc.monitoriza.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cl.duoc.monitoriza.model.Nodo;

@Repository
public interface NodoRepository extends JpaRepository<Nodo, Long>{

}
