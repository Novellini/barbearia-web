package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Pagamento;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagamentoRepository extends JpaRepository<Pagamento, Integer> {

    Optional<Pagamento> findByAgendamentoId(Integer idAgendamento);
}
