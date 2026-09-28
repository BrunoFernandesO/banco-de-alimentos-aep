package br.unicesumar.bancoalimentos.dao.conexao;

import br.unicesumar.bancoalimentos.dao.PersistenciaException;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Parâmetros de conexão com o banco de dados, lidos do arquivo {@code db.properties}.
 *
 * <p>O arquivo fica na raiz do projeto (diretório de onde o sistema é executado) e
 * <b>não é versionado</b>, pois contém a senha do MySQL de cada integrante. O modelo
 * versionado é {@code db.properties.example}.</p>
 *
 * <p>Chaves: {@code db.url} e {@code db.usuario} (obrigatórias) e {@code db.senha}
 * (opcional; ausente equivale a senha vazia).</p>
 */
public final class ConfiguracaoBanco {

    /** Nome do arquivo de configuração, procurado no diretório de execução. */
    public static final String NOME_ARQUIVO = "db.properties";

    static final String CHAVE_URL = "db.url";
    static final String CHAVE_USUARIO = "db.usuario";
    static final String CHAVE_SENHA = "db.senha";

    private final String url;
    private final String usuario;
    private final String senha;

    private ConfiguracaoBanco(String url, String usuario, String senha) {
        this.url = url;
        this.usuario = usuario;
        this.senha = senha;
    }

    /** Lê {@code db.properties} do diretório de execução. */
    public static ConfiguracaoBanco carregar() {
        return carregar(Path.of(NOME_ARQUIVO));
    }

    /** Lê a configuração do arquivo informado. */
    public static ConfiguracaoBanco carregar(Path arquivo) {
        if (!Files.isRegularFile(arquivo)) {
            throw new PersistenciaException("Arquivo de configuracao nao encontrado: "
                    + arquivo.toAbsolutePath() + System.lineSeparator()
                    + "Copie 'db.properties.example' para 'db.properties' (na raiz do projeto) "
                    + "e informe o usuario e a senha do seu MySQL.");
        }
        Properties propriedades = new Properties();
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        } catch (IOException e) {
            throw new PersistenciaException("Nao foi possivel ler o arquivo " + arquivo.toAbsolutePath(), e);
        }
        return dePropriedades(propriedades);
    }

    /** Monta a configuração a partir de propriedades já carregadas, validando as chaves. */
    static ConfiguracaoBanco dePropriedades(Properties propriedades) {
        String url = valorObrigatorio(propriedades, CHAVE_URL);
        if (!url.startsWith("jdbc:")) {
            throw new PersistenciaException("Valor invalido para " + CHAVE_URL + " em " + NOME_ARQUIVO
                    + ": deve comecar com 'jdbc:' (ex.: jdbc:mysql://localhost:3306/banco_alimentos).");
        }
        String usuario = valorObrigatorio(propriedades, CHAVE_USUARIO);
        String senha = propriedades.getProperty(CHAVE_SENHA, "");
        return new ConfiguracaoBanco(url, usuario, senha);
    }

    private static String valorObrigatorio(Properties propriedades, String chave) {
        String valor = propriedades.getProperty(chave);
        if (valor == null || valor.isBlank()) {
            throw new PersistenciaException("Propriedade obrigatoria ausente em " + NOME_ARQUIVO + ": " + chave);
        }
        return valor.trim();
    }

    public String getUrl() {
        return url;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }

    /** Descrição para mensagens e logs; nunca inclui a senha. */
    @Override
    public String toString() {
        return usuario + "@" + url;
    }
}
