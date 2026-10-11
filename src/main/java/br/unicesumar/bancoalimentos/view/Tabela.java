package br.unicesumar.bancoalimentos.view;

import java.util.ArrayList;
import java.util.List;

/**
 * Tabela de texto de largura fixa para listagens no console. Valores maiores que a
 * coluna são truncados com {@code ~}, mantendo o alinhamento.
 */
class Tabela {

    private final List<String> titulos = new ArrayList<>();
    private final List<Integer> larguras = new ArrayList<>();
    private final List<Boolean> alinhadasDireita = new ArrayList<>();
    private final List<String[]> linhas = new ArrayList<>();

    /** Coluna de texto, alinhada à esquerda. */
    Tabela coluna(String titulo, int largura) {
        return adicionarColuna(titulo, largura, false);
    }

    /** Coluna numérica, alinhada à direita. */
    Tabela colunaNumerica(String titulo, int largura) {
        return adicionarColuna(titulo, largura, true);
    }

    void adicionarLinha(Object... valores) {
        String[] linha = new String[valores.length];
        for (int i = 0; i < valores.length; i++) {
            linha[i] = String.valueOf(valores[i]);
        }
        linhas.add(linha);
    }

    boolean estaVazia() {
        return linhas.isEmpty();
    }

    void imprimir() {
        System.out.println(formatar(titulos.toArray(new String[0])));
        System.out.println("-".repeat(larguraTotal()));
        for (String[] linha : linhas) {
            System.out.println(formatar(linha));
        }
        if (linhas.isEmpty()) {
            System.out.println("(nenhum registro)");
        }
    }

    private Tabela adicionarColuna(String titulo, int largura, boolean direita) {
        titulos.add(titulo);
        larguras.add(largura);
        alinhadasDireita.add(direita);
        return this;
    }

    private String formatar(String[] valores) {
        StringBuilder texto = new StringBuilder();
        for (int i = 0; i < larguras.size(); i++) {
            int largura = larguras.get(i);
            String valor = i < valores.length ? valores[i] : "";
            if (valor.length() > largura) {
                valor = valor.substring(0, largura - 1) + "~";
            }
            String formato = alinhadasDireita.get(i) ? "%" + largura + "s" : "%-" + largura + "s";
            texto.append(String.format(formato, valor));
            if (i < larguras.size() - 1) {
                texto.append("  ");
            }
        }
        return texto.toString().stripTrailing();
    }

    private int larguraTotal() {
        return larguras.stream().mapToInt(Integer::intValue).sum() + 2 * (larguras.size() - 1);
    }
}
