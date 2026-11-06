package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Beneficiario;
import br.unicesumar.bancoalimentos.service.BeneficiarioService;

import java.util.List;

/** Telas de beneficiários: cadastro (RF04) e listagem. */
public class MenuBeneficiarios extends MenuConsole {

    private final BeneficiarioService beneficiarioService;

    public MenuBeneficiarios(Entrada entrada, BeneficiarioService beneficiarioService) {
        super(entrada);
        this.beneficiarioService = beneficiarioService;
    }

    @Override
    protected String getTitulo() {
        return "BENEFICIARIOS";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Cadastrar beneficiario (RF04)",
                "Listar beneficiarios");
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> cadastrar();
            case 2 -> imprimirBeneficiarios(beneficiarioService.listar());
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }

    private void cadastrar() {
        System.out.println("--- Cadastro de beneficiario ---");
        String nome = entrada.lerTexto("Nome (pessoa, familia ou instituicao)");
        String contato = entrada.lerTextoOpcional("Contato (telefone ou e-mail)");
        Beneficiario beneficiario = beneficiarioService.cadastrar(nome, contato);
        System.out.println("Beneficiario cadastrado com o ID " + beneficiario.getId() + ".");
    }

    /** Tabela de beneficiários; também usada ao registrar uma distribuição. */
    static void imprimirBeneficiarios(List<Beneficiario> beneficiarios) {
        Tabela tabela = new Tabela()
                .colunaNumerica("ID", 4)
                .coluna("Nome", 40)
                .coluna("Contato", 30);
        for (Beneficiario beneficiario : beneficiarios) {
            tabela.adicionarLinha(beneficiario.getId(), beneficiario.getNome(),
                    beneficiario.getContato() == null ? "-" : beneficiario.getContato());
        }
        tabela.imprimir();
    }
}
