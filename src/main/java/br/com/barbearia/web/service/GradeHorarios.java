package br.com.barbearia.web.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class GradeHorarios {
    private static final LocalTime ABERTURA = LocalTime.of(8, 0);
    private static final LocalTime FECHAMENTO = LocalTime.of(20, 0);

    private GradeHorarios() {}

    public record Reserva(LocalDateTime inicio, int minutos) {}

    public static boolean cabe(LocalDateTime inicio, int minutos, LocalDateTime agora, List<Reserva> reservas) {
        LocalTime hora = inicio.toLocalTime();
        if (minutos <= 0 || inicio.isBefore(agora) || hora.isBefore(ABERTURA)
                || hora.getMinute() % 30 != 0 || hora.getSecond() != 0 || hora.getNano() != 0) {
            return false;
        }
        LocalDateTime fim = inicio.plusMinutes(minutos);
        if (fim.isAfter(inicio.toLocalDate().atTime(FECHAMENTO))) return false;
        for (Reserva reserva : reservas) {
            LocalDateTime fimReserva = reserva.inicio().plusMinutes(reserva.minutos());
            if (inicio.isBefore(fimReserva) && reserva.inicio().isBefore(fim)) return false;
        }
        return true;
    }

    public static List<String> livres(LocalDate data, int minutos, List<Reserva> reservas) {
        List<String> horarios = new ArrayList<>();
        LocalDateTime agora = LocalDateTime.now();
        for (LocalTime hora = ABERTURA; hora.isBefore(FECHAMENTO); hora = hora.plusMinutes(30)) {
            if (cabe(data.atTime(hora), minutos, agora, reservas)) horarios.add(hora.toString());
        }
        return horarios;
    }
}
