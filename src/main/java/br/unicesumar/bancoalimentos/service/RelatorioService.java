package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.model.Alimento;

import java.time.LocalDate;
import java.util.List;

/** Consultas gerenciais sobre o estoque: alertas de vencimento (RF06). */
public class RelatorioService {

    /**
     * Antecedência do alerta de vencimento (RF06): são alertados os perecíveis com saldo
     * que vencem de hoje até este número de dias à frente.
     */
    public static final int DIAS_ALERTA_VENCIMENTO = 3;

    private final AlimentoDAO alimentoDAO;

    public RelatorioService() {
        this(new AlimentoDAO());
    }

    public RelatorioService(AlimentoDAO alimentoDAO) {
        this.alimentoDAO = alimentoDAO;
    }

    /**
     * Alimentos com saldo cuja validade cai entre hoje e hoje + {@link #DIAS_ALERTA_VENCIMENTO}
     * (RF06), dos que vencem antes para os que vencem depois. Itens sem validade (não
     * perecíveis) nunca aparecem; itens já vencidos pertencem ao relatório de desperdício.
     */
    public List<Alimento> listarAlertasVencimento() {
        LocalDate hoje = LocalDate.now();
        return alimentoDAO.listarComValidadeEntre(hoje, hoje.plusDays(DIAS_ALERTA_VENCIMENTO));
    }
}
