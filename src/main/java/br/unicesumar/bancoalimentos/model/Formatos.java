package br.unicesumar.bancoalimentos.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Formatos de exibição compartilhados pelas mensagens do domínio e pela camada de console:
 * datas no padrão brasileiro ({@code dd/MM/aaaa}) e quantidades com duas casas decimais
 * e vírgula, como no restante do sistema.
 */
public final class Formatos {

    /** Localidade usada na formatação de números (vírgula decimal). */
    public static final Locale LOCALE_BRASIL = Locale.forLanguageTag("pt-BR");

    /**
     * Padrão de data aceito na digitação e usado na exibição. A validação estrita rejeita
     * datas inexistentes (ex.: 31/02) em vez de ajustá-las silenciosamente.
     */
    public static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Formatos() {
    }

    /** Data no formato {@code dd/MM/aaaa}; {@code "-"} quando ausente. */
    public static String data(LocalDate data) {
        return data == null ? "-" : data.format(FORMATO_DATA);
    }

    /** Quantidade com duas casas decimais e vírgula (ex.: {@code 12,50}). */
    public static String quantidade(double valor) {
        return String.format(LOCALE_BRASIL, "%.2f", valor);
    }
}
