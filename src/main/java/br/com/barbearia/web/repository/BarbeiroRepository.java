package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Barbeiro;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BarbeiroRepository extends JpaRepository<Barbeiro, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Barbeiro b WHERE b.id = :id")
    Optional<Barbeiro> bloquearParaAgendar(Integer id);

    List<Barbeiro> findAllByOrderByNome();

    List<Barbeiro> findByAtivoTrueOrderByNome();

    @Query("SELECT b FROM Barbeiro b WHERE LOWER(b.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
         + "OR b.telefone LIKE CONCAT('%', :termo, '%') "
         + "OR LOWER(b.especialidade) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY b.nome")
    List<Barbeiro> buscar(String termo);
}
