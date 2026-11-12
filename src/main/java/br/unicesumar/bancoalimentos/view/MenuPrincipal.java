package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.service.AlimentoService;
import br.unicesumar.bancoalimentos.service.BeneficiarioService;
import br.unicesumar.bancoalimentos.service.DistribuicaoService;
import br.unicesumar.bancoalimentos.service.DoadorService;
import br.unicesumar.bancoalimentos.service.RelatorioService;

import java.util.List;

/** Menu principal: dá acesso aos menus de cada área do sistema. */
public class MenuPrincipal extends MenuConsole {

    private final MenuDoadores menuDoadores;
    private final MenuAlimentos menuAlimentos;
    private final MenuBeneficiarios menuBeneficiarios;
    private final MenuDistribuicoes menuDistribuicoes;
    private final MenuRelatorios menuRelatorios;

    public MenuPrincipal(Entrada entrada) {
        super(entrada);
        DoadorService doadorService = new DoadorService();
        AlimentoService alimentoService = new AlimentoService();
        BeneficiarioService beneficiarioService = new BeneficiarioService();
        DistribuicaoService distribuicaoService = new DistribuicaoService();
        this.menuDoadores = new MenuDoadores(entrada, doadorService);
        this.menuAlimentos = new MenuAlimentos(entrada, alimentoService, doadorService);
        this.menuBeneficiarios = new MenuBeneficiarios(entrada, beneficiarioService);
        this.menuDistribuicoes = new MenuDistribuicoes(entrada, distribuicaoService, alimentoService, beneficiarioService);
        this.menuRelatorios = new MenuRelatorios(entrada, new RelatorioService());
    }

    @Override
    protected String getTitulo() {
        return "MENU PRINCIPAL";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Doadores",
                "Alimentos e estoque",
                "Beneficiarios",
                "Distribuicoes",
                "Alertas e relatorios");
    }

    @Override
    protected String getRotuloSaida() {
        return "Sair";
    }

    @Override
    protected void executarOpcao(int opcao) {
        switch (opcao) {
            case 1 -> menuDoadores.executar();
            case 2 -> menuAlimentos.executar();
            case 3 -> menuBeneficiarios.executar();
            case 4 -> menuDistribuicoes.executar();
            case 5 -> menuRelatorios.executar();
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }
}
