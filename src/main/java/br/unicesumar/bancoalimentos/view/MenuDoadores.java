package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.Doador;
import br.unicesumar.bancoalimentos.model.Formatos;
import br.unicesumar.bancoalimentos.model.TipoDoador;
import br.unicesumar.bancoalimentos.service.DoadorService;

import java.util.List;

/** Telas de doadores: cadastro (RF01), listagem e consulta das doações de um doador. */
public class MenuDoadores extends MenuConsole {

    private final DoadorService doadorService;

    public MenuDoadores(Entrada entrada, DoadorService doadorService) {
        super(entrada);
        this.doadorService = doadorService;
    }

    @Override
    protected String getTitulo() {
        return "DOADORES";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Cadastrar doador (RF01)",
                "Listar doadores",
                "Ver alimentos doados por um doador");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> cadastrar();
            case 2 -> listar();
            case 3 -> verAlimentosDoados();
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    private void cadastrar() {
        System.out.println("--- Cadastro de doador ---");
        String nome = entrada.lerTexto("Nome");
        TipoDoador tipo = escolherTipo();
        String contato = entrada.lerTextoOpcional("Contato (telefone ou e-mail)");
        Doador doador = doadorService.cadastrar(nome, tipo, contato);
        System.out.println("Doador cadastrado com o ID " + doador.getId() + ".");
    }

    private TipoDoador escolherTipo() {
        TipoDoador[] tipos = TipoDoador.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println("  " + (i + 1) + " - " + tipos[i].getDescricao());
        }
        return tipos[entrada.lerInteiro("Tipo", 1, tipos.length) - 1];
    }

    private void listar() {
        imprimirDoadores(doadorService.listar());
    }

    /** Tabela de doadores; também usada ao escolher o doador de um alimento. */
    static void imprimirDoadores(List<Doador> doadores) {
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Nome", 34)
                .coluna("Tipo", 13)
                .coluna("Contato", 30);
        for (Doador doador : doadores) {
            tabela.adicionarLinha(doador.getId(), doador.getNome(), doador.getTipo().getDescricao(),
                    doador.getContato() == null ? "-" : doador.getContato());
        }
        tabela.imprimir();
    }

    private void verAlimentosDoados() {
        Doador doador = doadorService.buscarComAlimentos(entrada.lerId("ID do doador"));
        System.out.println("Doador: " + doador.getNome() + " (" + doador.getTipo().getDescricao() + ")");
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Alimento", 30)
                .coluna("Tipo", 13)
                .colunaNumerica("Saldo", 9)
                .coluna("Recebido", 10);
        for (Alimento alimento : doador.listarAlimentos()) {
            tabela.adicionarLinha(alimento.getId(), alimento.getNome(), alimento.getTipo().getDescricao(),
                    Formatos.quantidade(alimento.getQuantidade()), Formatos.data(alimento.getDataRecebimento()));
        }
        tabela.imprimir();
        System.out.println("Total de doacoes registradas: " + doador.listarAlimentos().size());
    }
}
