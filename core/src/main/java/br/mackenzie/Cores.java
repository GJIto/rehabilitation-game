package br.mackenzie;

import com.badlogic.gdx.graphics.Color;

/** Paleta limitada. Azul x laranja para zona x alerta (seguro para daltonismo vermelho/verde). */
public final class Cores {
    public static final Color CEU     = new Color(0.55f, 0.80f, 0.95f, 1f);
    public static final Color SOL     = new Color(1.00f, 0.85f, 0.20f, 1f);
    public static final Color AGUA1   = new Color(0.12f, 0.38f, 0.65f, 1f);
    public static final Color AGUA2   = new Color(0.08f, 0.28f, 0.52f, 1f);
    public static final Color AGUA3   = new Color(0.05f, 0.20f, 0.40f, 1f);
    public static final Color BARCO   = new Color(0.55f, 0.33f, 0.15f, 1f);
    public static final Color PELE    = new Color(0.95f, 0.78f, 0.62f, 1f);
    public static final Color ROUPA   = new Color(0.90f, 0.25f, 0.30f, 1f);
    public static final Color ZONA    = new Color(0.20f, 0.55f, 0.95f, 1f);
    public static final Color ALERTA  = new Color(1.00f, 0.55f, 0.10f, 1f);
    public static final Color CINZA   = new Color(0.30f, 0.33f, 0.38f, 1f);
    public static final Color BRANCO  = new Color(1f, 1f, 1f, 1f);
    public static final Color PRETO   = new Color(0.05f, 0.05f, 0.08f, 1f);
    public static final Color VEU     = new Color(0f, 0f, 0f, 0.65f);
    public static final Color MENU_BG = new Color(0.07f, 0.22f, 0.40f, 1f);

    public static final Color[] PEIXE = {
        new Color(0.75f, 0.85f, 0.25f, 1f),
        new Color(0.65f, 0.40f, 0.85f, 1f),
        new Color(0.35f, 0.40f, 0.55f, 1f),
    };

    private Cores() { }
}
