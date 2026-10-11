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
import java.util.List;

/** Telas do estoque de alimentos: registro de entrada (RF02) e listagem. */
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
                "Listar todos os alimentos");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> registrarEntrada();
            case 2 -> listarTodos();
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
