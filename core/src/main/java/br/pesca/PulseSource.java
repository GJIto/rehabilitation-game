package br.pesca;

/** Fonte de pulsos de pedalada: simulador por teclado ou Arduino via serial. */
public interface PulseSource {

    /** Chamado uma vez por frame (o teclado usa; a serial roda em thread propria). */
    void update();

    String nome();

    /** Feedback opcional para o Arduino acender LEDs: 'V' = ritmo ok, 'A' = devagar, 'R' = rapido, 'X' = apagar. */
    default void sinal(char c) { }

    void close();
}
