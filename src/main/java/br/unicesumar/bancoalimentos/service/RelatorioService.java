package br.unicesumar.bancoalimentos.service;

import br.unicesumar.bancoalimentos.dao.AlimentoDAO;
import br.unicesumar.bancoalimentos.model.Alimento;
import br.unicesumar.bancoalimentos.model.RelatorioDesperdicio;

import java.time.LocalDate;
import java.util.List;

/** Consultas gerenciais sobre o estoque: alertas de vencimento (RF06) e desperdício (RF07). */
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

    /**
     * Relatório de desperdício (RF07): alimentos vencidos que ainda tinham saldo, ou seja,
     * que não foram distribuídos (no todo ou em parte) antes da validade.
     */
    public RelatorioDesperdicio gerarRelatorioDesperdicio() {
        LocalDate hoje = LocalDate.now();
        return new RelatorioDesperdicio(hoje, alimentoDAO.listarDesperdicio(hoje));
    }
}
