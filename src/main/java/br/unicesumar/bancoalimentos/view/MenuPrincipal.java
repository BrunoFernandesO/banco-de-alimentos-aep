package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.service.AlimentoService;
import br.unicesumar.bancoalimentos.service.DoadorService;

import java.util.List;

/** Menu principal: dá acesso aos menus de cada área do sistema. */
public class MenuPrincipal extends MenuConsole {

    private final MenuDoadores menuDoadores;
    private final MenuAlimentos menuAlimentos;

    public MenuPrincipal(Entrada entrada) {
        super(entrada);
        DoadorService doadorService = new DoadorService();
        AlimentoService alimentoService = new AlimentoService();
        this.menuDoadores = new MenuDoadores(entrada, doadorService);
        this.menuAlimentos = new MenuAlimentos(entrada, alimentoService, doadorService);
    }

    @Override
    protected String getTitulo() {
        return "MENU PRINCIPAL";
    }

    @Override
    protected List<String> getOpcoes() {
        return List.of(
                "Doadores",
                "Alimentos e estoque");
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
            default -> throw new IllegalStateException("Opcao nao tratada: " + opcao);
        }
    }
}
