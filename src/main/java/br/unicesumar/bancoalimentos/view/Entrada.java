package br.unicesumar.bancoalimentos.view;

import br.unicesumar.bancoalimentos.model.Formatos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * Leitura de dados digitados pelo operador, com validação de formato.
 *
 * <p>Toda leitura repete a pergunta até receber um valor válido, de modo que uma digitação
 * errada nunca derruba o sistema. Regras de negócio (ex.: quantidade maior que o saldo)
 * não são verificadas aqui, e sim no domínio e na camada de serviço.</p>
 */
public class Entrada {

    private final BufferedReader leitor;

    public Entrada(InputStream origem) {
        this.leitor = new BufferedReader(new InputStreamReader(origem));
    }

    /** Texto obrigatório. */
    public String lerTexto(String rotulo) {
        while (true) {
            String valor = lerLinha(rotulo + ": ");
            if (!valor.isEmpty()) {
                return valor;
            }
            System.out.println("Valor obrigatorio.");
        }
    }

    /** Texto opcional; vazio retorna {@code ""}. */
    public String lerTextoOpcional(String rotulo) {
        return lerLinha(rotulo + " (opcional): ");
    }

    /** Texto que pode ser mantido: Enter conserva o valor atual. */
    public String lerTextoOuManter(String rotulo, String atual) {
        String valor = lerLinha(rotulo + " [" + (atual == null ? "" : atual) + "]: ");
        return valor.isEmpty() ? atual : valor;
    }

    /** Número inteiro entre {@code minimo} e {@code maximo}, inclusive. */
    public int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            String valor = lerLinha(rotulo + ": ");
            try {
                int numero = Integer.parseInt(valor);
                if (numero >= minimo && numero <= maximo) {
                    return numero;
                }
            } catch (NumberFormatException e) {
                // tratado abaixo, junto com o valor fora do intervalo
            }
            System.out.println("Digite um numero inteiro entre " + minimo + " e " + maximo + ".");
        }
    }

    /** Identificador (inteiro positivo). */
    public int lerId(String rotulo) {
        return lerInteiro(rotulo, 1, Integer.MAX_VALUE);
    }

    /** Quantidade maior que zero; aceita vírgula ou ponto decimal. */
    public double lerQuantidade(String rotulo) {
        while (true) {
            Double numero = converterDecimal(lerLinha(rotulo + ": "));
            if (numero != null && numero > 0) {
                return numero;
            }
            System.out.println("Digite uma quantidade maior que zero (ex.: 10 ou 2,5).");
        }
    }

    /** Quantidade maior ou igual a zero; Enter conserva o valor atual. */
    public double lerQuantidadeOuManter(String rotulo, double atual) {
        while (true) {
            String valor = lerLinha(rotulo + " [" + Formatos.quantidade(atual) + "]: ");
            if (valor.isEmpty()) {
                return atual;
            }
            Double numero = converterDecimal(valor);
            if (numero != null && numero >= 0) {
                return numero;
            }
            System.out.println("Digite uma quantidade maior ou igual a zero (ex.: 10 ou 2,5).");
        }
    }

    /** Data no formato dd/mm/aaaa; se {@code padrao} não for nulo, Enter o seleciona. */
    public LocalDate lerData(String rotulo, LocalDate padrao) {
        String sufixo = padrao == null ? "" : " [Enter = " + Formatos.data(padrao) + "]";
        while (true) {
            String valor = lerLinha(rotulo + " (dd/mm/aaaa)" + sufixo + ": ");
            if (valor.isEmpty() && padrao != null) {
                return padrao;
            }
            try {
                return LocalDate.parse(valor, Formatos.FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data invalida. Use o formato dd/mm/aaaa (ex.: 25/12/2026).");
            }
        }
    }

    /** Data que pode ser mantida: Enter conserva o valor atual. */
    public LocalDate lerDataOuManter(String rotulo, LocalDate atual) {
        while (true) {
            String valor = lerLinha(rotulo + " (dd/mm/aaaa) [" + Formatos.data(atual) + "]: ");
            if (valor.isEmpty()) {
                return atual;
            }
            try {
                return LocalDate.parse(valor, Formatos.FORMATO_DATA);
            } catch (DateTimeParseException e) {
                System.out.println("Data invalida. Use o formato dd/mm/aaaa (ex.: 25/12/2026).");
            }
        }
    }

    /** Pergunta de sim ou não. */
    public boolean confirmar(String pergunta) {
        while (true) {
            String valor = lerLinha(pergunta + " (s/n): ").toLowerCase();
            if (valor.equals("s") || valor.equals("sim")) {
                return true;
            }
            if (valor.equals("n") || valor.equals("nao")) {
                return false;
            }
            System.out.println("Responda s ou n.");
        }
    }

    /** Aguarda o operador antes de voltar ao menu. */
    public void pausar() {
        lerLinha("Pressione Enter para continuar...");
    }

    private Double converterDecimal(String valor) {
        try {
            double numero = Double.parseDouble(valor.replace(',', '.'));
            return Double.isFinite(numero) ? numero : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String lerLinha(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        try {
            String linha = leitor.readLine();
            if (linha == null) {
                throw new EntradaEncerradaException();
            }
            return linha.trim();
        } catch (IOException e) {
            throw new EntradaEncerradaException();
        }
    }
}
