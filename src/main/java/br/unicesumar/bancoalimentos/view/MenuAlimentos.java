package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.AlimentoNaoPerecivel;
import br.unicesumar.bancoalimentos.model.AlimentoPerecivel;
import br.unicesumar.bancoalimentos.model.Doador;
import br.unicesumar.bancoalimentos.model.Formatos;
import br.unicesumar.bancoalimentos.model.TipoAlimento;
import br.unicesumar.bancoalimentos.service.AlimentoService;
import br.unicesumar.bancoalimentos.service.DoadorService;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Telas do estoque de alimentos: registro de entrada (RF02), estoque por prioridade de
 * distribuição (RF03) e as demais operações do CRUD de Alimento.
 */
public class MenuAlimentos extends MenuConsole {

    private final AlimentoService alimentoService;
    private final DoadorService doadorService;

    public MenuAlimentos(Entrada entrada, AlimentoService alimentoService, DoadorService doadorService) {
        super(entrada);
        this.alimentoService = alimentoService;
        this.doadorService = doadorService;
    }

    @Override
    protected String getTitulo() {
        return "ALIMENTOS E ESTOQUE";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Registrar entrada de alimento (RF02)",
                "Listar todos os alimentos",
                "Estoque por prioridade de distribuicao (RF03)",
                "Consultar alimento por ID",
                "Alterar alimento",
                "Excluir alimento");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> registrarEntrada();
            case 2 -> listarTodos();
            case 3 -> listarPorPrioridade();
            case 4 -> consultar();
            case 5 -> alterar();
            case 6 -> excluir();
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    private void registrarEntrada() {
        System.out.println("--- Entrada de alimento no estoque ---");
        List<Doador> doadores = doadorService.listar();
        if (doadores.isEmpty()) {
            System.out.println("Nenhum doador cadastrado. Cadastre o doador antes de registrar a doacao.");
            return;
        }
        MenuDoadores.imprimirDoadores(doadores);
        Doador doador = doadorService.buscarPorId(entrada.lerId("ID do doador"));

        String nome = entrada.lerTexto("Nome do alimento (inclua a unidade, ex.: Arroz 5kg)");
        TipoAlimento tipo = escolherTipo();
        double quantidade = entrada.lerQuantidade("Quantidade");
        LocalDate dataRecebimento = entrada.lerData("Data de recebimento", LocalDate.now());

        // Construção do objeto conforme o tipo escolhido pelo operador.
        Alimento alimento = switch (tipo) {
            case PERECIVEL -> new AlimentoPerecivel(nome, quantidade, dataRecebimento,
                    entrada.lerData("Data de validade", null), doador);
            case NAO_PERECIVEL -> new AlimentoNaoPerecivel(nome, quantidade, dataRecebimento, doador);
        };
        alimentoService.cadastrar(alimento);
        System.out.println("Alimento registrado com o ID " + alimento.getId() + ".");
    }

    private TipoAlimento escolherTipo() {
        TipoAlimento[] tipos = TipoAlimento.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("  " + (i + 1) + " - " + tipos[i].getDescricao());
        }
        return tipos[entrada.lerInteiro("Tipo", 1, tipos.length) - 1];
    }

    private void listarTodos() {
        imprimirAlimentos(alimentoService.listar());
    }

    private void listarPorPrioridade() {
        System.out.println("Estoque disponivel (saldo > 0, nao vencido), em ordem de distribuicao:");
        imprimirPrioridades(alimentoService.listarPorPrioridade());
    }

    /**
     * Tabela do RF03, na ordem recebida. Também usada para escolher o alimento de uma
     * distribuição.
     */
    static void imprimirPrioridades(List<Alimento> alimentos) {
        Tabela tabela = new Tabela()
                .colunaNumerica("#", 3)
                .colunaNumerica("ID", 4)
                .coluna("Alimento", 28)
                .coluna("Tipo", 13)
                .colunaNumerica("Saldo", 9)
                .coluna("Validade", 10)
                .coluna("Situacao", 20)
                .colunaNumerica("Prioridade", 10);
        int posicao = 1;
        for (Alimento alimento : alimentos) {
            tabela.adicionarLinha(posicao++, alimento.getId(), alimento.getNome(), alimento.getTipo().getDescricao(),
                    Formatos.quantidade(alimento.getQuantidade()),
                    Formatos.data(alimento.consultarValidade().orElse(null)),
                    descreverSituacao(alimento),
                    String.format(Formatos.LOCALE_BRASIL, "%.1f", alimento.calcularPrioridade()));
        }
        tabela.imprimir();
    }

    /** Prazo de validade (itens com validade) ou tempo em estoque (itens sem validade). */
    static String descreverSituacao(Alimento alimento) {
        return alimento.consultarValidade()
                .map(validade -> {
                    long dias = ChronoUnit.DAYS.between(LocalDate.now(), validade);
                    if (dias < 0) {
                        return "VENCIDO ha " + (-dias) + " dia(s)";
                    }
                    return dias == 0 ? "vence hoje" : "vence em " + dias + " dia(s)";
                })
                .orElse(alimento.diasEmEstoque() + " dia(s) em estoque");
    }

    private void consultar() {
        imprimirDetalhes(alimentoService.buscarPorId(entrada.lerId("ID do alimento")));
    }

    private void imprimirDetalhes(Alimento alimento) {
        System.out.println("ID ............: " + alimento.getId());
        System.out.println("Nome ..........: " + alimento.getNome());
        System.out.println("Tipo ..........: " + alimento.getTipo().getDescricao());
        System.out.println("Doador ........: " + alimento.getDoador().getNome());
        System.out.println("Saldo .........: " + Formatos.quantidade(alimento.getQuantidade()));
        System.out.println("Recebido em ...: " + Formatos.data(alimento.getDataRecebimento()));
        System.out.println("Validade ......: " + Formatos.data(alimento.consultarValidade().orElse(null)));
        System.out.println("Situacao ......: " + descreverSituacao(alimento));
        System.out.println("Prioridade ....: " + String.format(Formatos.LOCALE_BRASIL, "%.1f", alimento.calcularPrioridade()));
    }

    private void alterar() {
        Alimento alimento = alimentoService.buscarPorId(entrada.lerId("ID do alimento"));
        imprimirDetalhes(alimento);
        System.out.println("Informe os novos valores (Enter mantem o valor atual). Tipo e doador nao podem ser alterados.");
        alimento.setNome(entrada.lerTextoOuManter("Nome", alimento.getNome()));
        alimento.setQuantidade(entrada.lerQuantidadeOuManter("Saldo em estoque", alimento.getQuantidade()));
        LocalDate dataRecebimento = entrada.lerDataOuManter("Data de recebimento", alimento.getDataRecebimento());
        // Campo exibido apenas para o tipo que o possui (decisao de tela, nao de regra de negocio).
        if (alimento instanceof AlimentoPerecivel perecivel) {
            LocalDate dataValidade = entrada.lerDataOuManter("Data de validade", perecivel.getDataValidade());
            perecivel.alterarDatas(dataRecebimento, dataValidade);
        } else {
            alimento.setDataRecebimento(dataRecebimento);
        }
        alimentoService.atualizar(alimento);
        System.out.println("Alimento " + alimento.getId() + " atualizado.");
    }

    private void excluir() {
        Alimento alimento = alimentoService.buscarPorId(entrada.lerId("ID do alimento"));
        imprimirDetalhes(alimento);
        if (entrada.confirmar("Confirma a exclusao deste alimento?")) {
            alimentoService.excluir(alimento.getId());
            System.out.println("Alimento " + alimento.getId() + " excluido.");
        } else {
            System.out.println("Exclusao cancelada.");
        }
    }

    /** Tabela com os dados cadastrais dos alimentos. */
    static void imprimirAlimentos(List<Alimento> alimentos) {
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Alimento", 28)
                .coluna("Tipo", 13)
                .colunaNumerica("Saldo", 9)
                .coluna("Recebido", 10)
                .coluna("Validade", 10)
                .coluna("Doador", 22);
        for (Alimento alimento : alimentos) {
            tabela.adicionarLinha(alimento.getId(), alimento.getNome(), alimento.getTipo().getDescricao(),
                    Formatos.quantidade(alimento.getQuantidade()), Formatos.data(alimento.getDataRecebimento()),
                    Formatos.data(alimento.consultarValidade().orElse(null)), alimento.getDoador().getNome());
        }
        tabela.imprimir();
    }
}
