package br.pesca;

/**
 * Regras do jogo, sem nenhuma dependencia de libGDX (facil de testar).
 * Recebe a cadencia (RPM) a cada frame e atualiza distancia do peixe, tensao da linha e metricas.
 */
public class LutaPesca {

    public enum Estado { AQUECIMENTO, LUTA, CAPTURADO, ESCAPOU, ROMPEU }

    public enum Sinal { OK, DEVAGAR, RAPIDO }

    /** Acima de alvo x este fator a zona nunca sobe (limite de seguranca para o paciente). */
    public static final float LIMITE_SEGURANCA = 1.35f;
    public static final float TEMPO_AQUECIMENTO = 10f;
    public static final int MAX_ROMPIDAS = 3;

    public final Fase fase;
    public final float alvo;

    public Estado estado = Estado.AQUECIMENTO;
    public Sinal sinal = Sinal.DEVAGAR;

    public float progressoAquec;       // 0..1
    public float distancia;            // 0 = capturado, 1 = fugiu
    public float tensao = 0.5f;        // 0..1 (ideal ~0,5; >= 1 rompe)
    public float zonaMin, zonaMax;
    public float tempoLuta, tempoZona;
    public float rompeuHa = 99f;       // segundos desde a ultima linha rompida (efeito visual)
    public int rompidas;

    private float fatorZona = 1f;
    private int n;
    private double media, m2;          // Welford: media e variancia online da cadencia

    public LutaPesca(Fase fase, float alvoRpm) {
        this.fase = fase;
        this.alvo = alvoRpm;
        this.distancia = fase.distInicial;
        this.zonaMin = alvo * 0.6f;
        this.zonaMax = alvo * LIMITE_SEGURANCA;
    }

    public boolean terminou() {
        return estado == Estado.CAPTURADO || estado == Estado.ESCAPOU || estado == Estado.ROMPEU;
    }

    public void update(float cad, float dt) {
        if (terminou()) return;

        if (estado == Estado.AQUECIMENTO) {
            zonaMin = alvo * 0.6f;
            zonaMax = alvo * LIMITE_SEGURANCA;
            sinal = classificar(cad);
            progressoAquec += Math.min(cad / alvo, 1.2f) * dt / TEMPO_AQUECIMENTO;
            if (progressoAquec >= 1f) estado = Estado.LUTA;
            return;
        }

        tempoLuta += dt;
        rompeuHa += dt;

        fatorZona += (fase.fatorAlvo(tempoLuta) - fatorZona) * Math.min(1f, dt * 1.5f);
        float centro = alvo * fatorZona;
        zonaMin = centro * (1f - fase.zonaMeia);
        zonaMax = Math.min(centro * (1f + fase.zonaMeia), alvo * LIMITE_SEGURANCA);

        amostrar(cad);
        sinal = classificar(cad);

        switch (sinal) {
            case OK:
                distancia -= fase.velRecolher * dt;
                tempoZona += dt;
                tensao += (0.5f - tensao) * Math.min(1f, dt * 3f);
                break;
            case RAPIDO:
                tensao += (cad - zonaMax) / alvo * 0.9f * dt;
                break;
            case DEVAGAR:
                float deficit = Math.max(0f, Math.min(1f, (zonaMin - cad) / zonaMin));
                distancia += fase.velFuga * dt * (0.4f + 0.6f * deficit);
                tensao += (0.1f - tensao) * Math.min(1f, dt * 1.5f);
                break;
        }

        if (tensao >= 1f) {
            rompidas++;
            rompeuHa = 0f;
            tensao = 0.5f;
            distancia = Math.min(1f, distancia + 0.12f);
            if (rompidas >= MAX_ROMPIDAS) {
                estado = Estado.ROMPEU;
                return;
            }
        }
        if (distancia <= 0f) {
            distancia = 0f;
            estado = Estado.CAPTURADO;
        } else if (distancia >= 1f || tempoLuta >= fase.tempoMax) {
            estado = Estado.ESCAPOU;
        }
    }

    private Sinal classificar(float cad) {
        if (cad > zonaMax) return Sinal.RAPIDO;
        if (cad < zonaMin) return Sinal.DEVAGAR;
        return Sinal.OK;
    }

    private void amostrar(float x) {
        n++;
        double d = x - media;
        media += d / n;
        m2 += d * (x - media);
    }

    public Resultado resultado() {
        float pctZona = tempoLuta > 0f ? tempoZona / tempoLuta : 0f;
        float cv = 1f;
        if (n > 1 && media > 0.0) {
            cv = (float) (Math.sqrt(m2 / (n - 1)) / media);
        }
        boolean capturou = estado == Estado.CAPTURADO;
        float consistencia = clamp01(1f - cv);
        float velocidade = clamp01(1f - tempoLuta / fase.tempoMax);
        float s = 600f * pctZona + 300f * consistencia + 100f * velocidade;
        if (!capturou) s *= 0.5f;
        return new Resultado(fase.numero, capturou, Math.round(s), pctZona, cv, tempoLuta, rompidas, alvo);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
