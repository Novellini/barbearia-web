package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Agendamento;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByDataHoraBetweenOrderByDataHora(LocalDateTime inicio, LocalDateTime fim);

    List<Agendamento> findByBarbeiroIdAndDataHoraBetween(Integer idBarbeiro, LocalDateTime inicio, LocalDateTime fim);

    boolean existsByBarbeiroIdAndDataHora(Integer idBarbeiro, LocalDateTime dataHora);

    long countByDataHoraBetweenAndStatusNot(LocalDateTime inicio, LocalDateTime fim, String status);

    long countByDataHoraBetweenAndStatus(LocalDateTime inicio, LocalDateTime fim, String status);
}
