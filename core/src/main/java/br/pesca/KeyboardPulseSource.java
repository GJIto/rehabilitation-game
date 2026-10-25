package br.pesca;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

/** Simulador: cada toque na barra de espaco equivale a uma volta da pedivela. */
public class KeyboardPulseSource implements PulseSource {

    private final CadenceTracker tracker;

    public KeyboardPulseSource(CadenceTracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void update() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) tracker.pulse();
    }

    @Override
    public String nome() {
        return "Teclado (Espaço = 1 volta)";
    }

    @Override
    public void close() { }
}
