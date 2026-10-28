package br.mackenzie;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Registra cada tentativa em CSV: base para mostrar a evolucao do paciente entre sessoes. */
public class SessionLogger {

    /** Fica na pasta do usuario (~/.pesca-no-ritmo/) para nao depender do diretorio de trabalho. */
    private final File arquivo = new File(new File(System.getProperty("user.home"), ".pesca-no-ritmo"), "sessoes_pesca.csv");

    public void registrar(Resultado r) {
        arquivo.getParentFile().mkdirs();
        boolean novo = !arquivo.exists();
        try (PrintWriter w = new PrintWriter(new FileWriter(arquivo, true))) {
            if (novo) {
                w.println("data_hora,fase,capturou,score,pct_zona,cv,tempo_s,rompidas,alvo_rpm,pse");
            }
            w.println(String.format(Locale.ROOT, "%s,%d,%b,%d,%.3f,%.3f,%.1f,%d,%.1f,%d",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    r.fase, r.capturou, r.score, r.pctZona, r.cv, r.tempoLuta, r.rompidas, r.alvoRpm, r.pse));
        } catch (IOException e) {
            System.err.println("Nao foi possivel gravar o CSV: " + e.getMessage());
        }
    }
}
