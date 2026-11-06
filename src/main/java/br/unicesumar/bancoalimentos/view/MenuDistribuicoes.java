package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.model.Distribuicao;
import br.unicesumar.bancoalimentos.model.Formatos;
import br.unicesumar.bancoalimentos.service.AlimentoService;
import br.unicesumar.bancoalimentos.service.BeneficiarioService;
import br.unicesumar.bancoalimentos.service.DistribuicaoService;

import java.util.List;

/**
 * Telas de distribuição: registro com baixa de estoque (RF05) e histórico de entregas.
 * O alimento é escolhido a partir da lista por prioridade (RF03), para que os itens mais
 * próximos do vencimento sejam oferecidos primeiro.
 */
public class MenuDistribuicoes extends MenuConsole {

    private final DistribuicaoService distribuicaoService;
    private final AlimentoService alimentoService;
    private final BeneficiarioService beneficiarioService;

    public MenuDistribuicoes(Entrada entrada, DistribuicaoService distribuicaoService,
                             AlimentoService alimentoService, BeneficiarioService beneficiarioService) {
        super(entrada);
        this.distribuicaoService = distribuicaoService;
        this.alimentoService = alimentoService;
        this.beneficiarioService = beneficiarioService;
    }

    @Override
    protected String getTitulo() {
        return "DISTRIBUICOES";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Registrar distribuicao (RF05)",
                "Historico de distribuicoes");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> registrar();
            case 2 -> listarHistorico();
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    private void registrar() {
        System.out.println("--- Registro de distribuicao ---");
        List<Alimento> disponiveis = alimentoService.listarPorPrioridade();
        if (disponiveis.isEmpty()) {
            System.out.println("Nao ha alimentos disponiveis para distribuicao (com saldo e dentro da validade).");
            return;
        }
        List<Beneficiario> beneficiarios = beneficiarioService.listar();
        if (beneficiarios.isEmpty()) {
            System.out.println("Nenhum beneficiario cadastrado. Cadastre o beneficiario antes de registrar a entrega.");
            return;
        }

        System.out.println("Estoque disponivel em ordem de prioridade (a posicao 1 deve sair primeiro):");
        MenuAlimentos.imprimirPrioridades(disponiveis);
        int posicao = entrada.lerInteiro("Posicao (#) do alimento na lista", 1, disponiveis.size());
        Alimento alimento = disponiveis.get(posicao - 1);
        System.out.println("Selecionado: " + alimento.getNome() + " (saldo " + Formatos.quantidade(alimento.getQuantidade()) + ")");

        MenuBeneficiarios.imprimirBeneficiarios(beneficiarios);
        int beneficiarioId = entrada.lerId("ID do beneficiario");
        double quantidade = entrada.lerQuantidade("Quantidade a distribuir");

        Distribuicao distribuicao = distribuicaoService.registrar(alimento.getId(), beneficiarioId, quantidade);
        System.out.println("Distribuicao " + distribuicao.getId() + " registrada: "
                + Formatos.quantidade(distribuicao.getQuantidade()) + " de '" + distribuicao.getAlimento().getNome()
                + "' para '" + distribuicao.getBeneficiario().getNome() + "'.");
        System.out.println("Saldo restante em estoque: " + Formatos.quantidade(distribuicao.getAlimento().getQuantidade()) + ".");
    }

    private void listarHistorico() {
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Data", 10)
                .coluna("Alimento", 28)
                .colunaNumerica("Quantidade", 10)
                .coluna("Beneficiario", 34);
        for (Distribuicao distribuicao : distribuicaoService.listar()) {
            tabela.adicionarLinha(distribuicao.getId(), Formatos.data(distribuicao.getData()),
                    distribuicao.getAlimento().getNome(), Formatos.quantidade(distribuicao.getQuantidade()),
                    distribuicao.getBeneficiario().getNome());
        }
        tabela.imprimir();
    }
}
