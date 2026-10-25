package br.pesca;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Le linhas "P,<intervalo_ms>" enviadas pelo Arduino (115200 baud). Cada linha = 1 volta da pedivela.
 * A leitura roda em thread propria para nao travar o render.
 */
public class SerialPulseSource implements PulseSource {

    private final SerialPort porta;
    private final CadenceTracker tracker;
    private volatile boolean rodando = true;

    public SerialPulseSource(SerialPort porta, CadenceTracker tracker) {
        this.porta = porta;
        this.tracker = tracker;
    }

    /** @return false se a porta nao pode ser aberta. */
    public boolean abrir() {
        porta.setBaudRate(115200);
        porta.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        if (!porta.openPort()) return false;
        Thread t = new Thread(this::ler, "serial-leitor");
        t.setDaemon(true);
        t.start();
        return true;
    }

    private void ler() {
        StringBuilder linha = new StringBuilder();
        try (InputStream in = porta.getInputStream()) {
            while (rodando) {
                int b = in.read();
                if (b < 0) break;
                if (b == '\n') {
                    processar(linha.toString().trim());
                    linha.setLength(0);
                } else if (linha.length() < 64) {
                    linha.append((char) b);
                }
            }
        } catch (IOException e) {
            // porta fechada ou cabo removido: a thread simplesmente termina
        }
    }

    private void processar(String l) {
        if (l.startsWith("P")) tracker.pulse();
    }

    @Override
    public void update() { }

    @Override
    public String nome() {
        return "Arduino (" + porta.getSystemPortName() + ")";
    }

    @Override
    public void sinal(char c) {
        try {
            OutputStream out = porta.getOutputStream();
            out.write(c);
            out.flush();
        } catch (IOException e) {
            // feedback e opcional: ignora falhas
        }
    }

    @Override
    public void close() {
        rodando = false;
        porta.closePort();
    }
}
