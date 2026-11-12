package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Formatos;
import br.unicesumar.bancoalimentos.service.RelatorioService;

import java.util.List;

/** Telas de acompanhamento: alertas de vencimento (RF06). */
public class MenuRelatorios extends MenuConsole {

    private final RelatorioService relatorioService;

    public MenuRelatorios(Entrada entrada, RelatorioService relatorioService) {
        super(entrada);
        this.relatorioService = relatorioService;
    }

    @Override
    protected String getTitulo() {
        return "ALERTAS E RELATORIOS";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of("Alertas de vencimento (RF06)");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> exibirAlertasVencimento(relatorioService);
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    /** Alerta do RF06; exibido também automaticamente ao iniciar o sistema. */
    public static void exibirAlertasVencimento(RelatorioService relatorioService) {
        List<Alimento> alertas = relatorioService.listarAlertasVencimento();
        int dias = RelatorioService.DIAS_ALERTA_VENCIMENTO;
        if (alertas.isEmpty()) {
            System.out.println("Nenhum alimento perecivel com saldo vence nos proximos " + dias + " dias.");
            return;
        }
        System.out.println("*** ALERTA DE VENCIMENTO: " + alertas.size()
                + " alimento(s) perecivel(is) vence(m) nos proximos " + dias + " dias. Priorize a distribuicao. ***");
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Alimento", 28)
                .colunaNumerica("Saldo", 9)
                .coluna("Validade", 10)
                .coluna("Situacao", 20)
                .coluna("Doador", 24);
        for (Alimento alimento : alertas) {
            tabela.adicionarLinha(alimento.getId(), alimento.getNome(), Formatos.quantidade(alimento.getQuantidade()),
                    Formatos.data(alimento.consultarValidade().orElse(null)), MenuAlimentos.descreverSituacao(alimento),
                    alimento.getDoador().getNome());
        }
        tabela.imprimir();
    }
}
