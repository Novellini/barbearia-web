package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Servico;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ServicoRepository extends JpaRepository<Servico, Integer> {

    List<Servico> findAllByOrderByNome();

    List<Servico> findByAtivoTrueOrderByNome();

    @Query("SELECT s FROM Servico s WHERE LOWER(s.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
         + "OR LOWER(s.descricao) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY s.nome")
    List<Servico> buscar(String termo);
}
