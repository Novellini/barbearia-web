package br.com.barbearia.web.repository;

import br.com.barbearia.web.model.Agendamento;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Integer> {

    List<Agendamento> findByDataHoraBetweenOrderByDataHora(LocalDateTime inicio, LocalDateTime fim);

    @Query(value = """
            SELECT * FROM agendamento WHERE id_barbeiro = :barbeiro AND status <> 'cancelado'
                AND data_hora < :limite
                AND TIMESTAMPADD(MINUTE, COALESCE(duracao_reservada, 30), data_hora) > :inicio
            """, nativeQuery = true)
    List<Agendamento> buscarReservas(Integer barbeiro, LocalDateTime inicio, LocalDateTime limite);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Agendamento a WHERE a.id = :id")
    Optional<Agendamento> bloquearPorId(Integer id);

    long countByDataHoraBetweenAndStatusNot(LocalDateTime inicio, LocalDateTime fim, String status);

    long countByDataHoraBetweenAndStatus(LocalDateTime inicio, LocalDateTime fim, String status);
}
