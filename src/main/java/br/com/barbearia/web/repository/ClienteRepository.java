package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Cliente;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    List<Cliente> findAllByOrderByNome();

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.nome) LIKE LOWER(CONCAT('%', :termo, '%')) "
         + "OR c.telefone LIKE CONCAT('%', :termo, '%') "
         + "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :termo, '%')) ORDER BY c.nome")
    List<Cliente> buscar(String termo);
}
