// Máscara de telefone: (11) 99999-0000
document.querySelectorAll('[data-mascara=telefone]').forEach(campo => {
    campo.addEventListener('input', () => {
        const d = campo.value.replace(/\D/g, '').slice(0, 11);
        let v = d;
        if (d.length > 0) v = '(' + d.slice(0, 2);
        if (d.length >= 2) v += ') ' + d.slice(2, 7);
        if (d.length >= 7) v += '-' + d.slice(7);
        campo.value = v;
    });
});

// Novo agendamento: resumo ao vivo e horários livres do barbeiro
const form = document.getElementById('form-agendamento');
if (form) {
    const cliente = document.getElementById('clienteId');
    const barbeiro = document.getElementById('barbeiroId');
    const data = document.getElementById('data');
    const horario = document.getElementById('horario');
    const servicos = form.querySelectorAll('input[name=servicoIds]');
    const moeda = v => v.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    const texto = (sel) => sel.value ? sel.options[sel.selectedIndex].text : '-';

    function atualizarResumo() {
        document.getElementById('res-cliente').textContent = cliente.value ? texto(cliente).split(' — ')[0] : '-';
        document.getElementById('res-barbeiro').textContent = texto(barbeiro);
        const marcados = [...servicos].filter(s => s.checked);
        document.getElementById('res-servicos').textContent = marcados.map(s => s.dataset.nome).join(' + ') || '-';
        const total = marcados.reduce((t, s) => t + parseFloat(s.dataset.preco), 0);
        const duracao = marcados.reduce((t, s) => t + parseInt(s.dataset.duracao), 0);
        document.getElementById('res-total').textContent = moeda(total);
        document.getElementById('res-duracao').textContent = duracao ? duracao + ' min' : '-';
        document.getElementById('res-data').textContent = data.value ? data.value.split('-').reverse().join('/') : '-';
        document.getElementById('res-horario').textContent = horario.value || '-';
    }

    async function carregarHorarios() {
        const anterior = horario.value;
        horario.innerHTML = '';
        if (!barbeiro.value || !data.value) {
            horario.add(new Option(barbeiro.value ? 'Informe a data' : 'Selecione o barbeiro', ''));
            atualizarResumo();
            return;
        }
        const url = form.dataset.horarios + '?barbeiroId=' + barbeiro.value + '&data=' + data.value;
        const livres = await (await fetch(url)).json();
        horario.add(new Option(livres.length ? 'Selecionar horário' : 'Sem horários livres nesta data', ''));
        livres.forEach(h => horario.add(new Option(h, h, false, h === anterior)));
        atualizarResumo();
    }

    [cliente, horario].forEach(c => c.addEventListener('change', atualizarResumo));
    servicos.forEach(s => s.addEventListener('change', atualizarResumo));
    [barbeiro, data].forEach(c => c.addEventListener('change', carregarHorarios));
    atualizarResumo();
}
