package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Barbeiro;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BarbeiroRepository extends JpaRepository<Barbeiro, Integer> {

    List<Barbeiro> findAllByOrderByNome();

    List<Barbeiro> findByAtivoTrueOrderByNome();

    @Query("SELECT b FROM Barbeiro b WHERE LOWER(b.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
         + "OR b.telefone LIKE CONCAT('%', :termo, '%') "
         + "OR LOWER(b.especialidade) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY b.nome")
    List<Barbeiro> buscar(String termo);
}
