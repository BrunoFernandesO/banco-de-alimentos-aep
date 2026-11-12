package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Formatos;
import br.unicesumar.bancoalimentos.model.ItemDesperdicio;
import br.unicesumar.bancoalimentos.model.RelatorioDesperdicio;
import br.unicesumar.bancoalimentos.service.RelatorioService;

import java.util.List;

/** Telas de acompanhamento: alertas de vencimento (RF06) e relatório de desperdício (RF07). */
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
        return List.of(
                "Alertas de vencimento (RF06)",
                "Relatorio de desperdicio (RF07)");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> exibirAlertasVencimento(relatorioService);
            case 2 -> exibirRelatorioDesperdicio();
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    private void exibirRelatorioDesperdicio() {
        RelatorioDesperdicio relatorio = relatorioService.gerarRelatorioDesperdicio();
        System.out.println("RELATORIO DE DESPERDICIO - alimentos vencidos nao distribuidos ("
                + Formatos.data(relatorio.dataReferencia()) + ")");
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Alimento", 28)
                .coluna("Venceu em", 10)
                .colunaNumerica("Perdido", 9)
                .colunaNumerica("Distribuido", 11)
                .coluna("Situacao", 22)
                .coluna("Doador", 22);
        for (ItemDesperdicio item : relatorio.itens()) {
            Alimento alimento = item.alimento();
            tabela.adicionarLinha(alimento.getId(), alimento.getNome(),
                    Formatos.data(alimento.consultarValidade().orElse(null)),
                    Formatos.quantidade(item.quantidadePerdida()), Formatos.quantidade(item.quantidadeDistribuida()),
                    item.nuncaDistribuido() ? "nunca distribuido" : "distribuido em parte",
                    alimento.getDoador().getNome());
        }
        tabela.imprimir();
        System.out.println();
        System.out.println("Itens vencidos com saldo ......: " + relatorio.itens().size());
        System.out.println("Itens nunca distribuidos ......: " + relatorio.contarNuncaDistribuidos());
        System.out.println("Quantidade total perdida ......: " + Formatos.quantidade(relatorio.quantidadeTotalPerdida())
                + " (soma das quantidades; unidade conforme cada item)");
        System.out.println("Distribuido antes de vencer ...: " + Formatos.quantidade(relatorio.quantidadeTotalDistribuida()));
        System.out.println("Aproveitamento desses itens ...: "
                + String.format(Formatos.LOCALE_BRASIL, "%.1f%%", relatorio.percentualAproveitado()));
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
