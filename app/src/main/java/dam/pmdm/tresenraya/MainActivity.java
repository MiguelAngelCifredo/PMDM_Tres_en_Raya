package dam.pmdm.tresenraya;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.gridlayout.widget.GridLayout;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    // Constantes para los estados de las celdas
    static final int BLANCO = 0;
    static final int FICHA1 = 1;
    static final int FICHA2 = 2;

    // Variables de estado del juego
    static int[][] casillero = new int[3][3];
    static boolean esPrimeraParte = true;
    static boolean esTurno1 = true;
    static boolean estaEnJuego = false;
    static boolean esPrimerClic = true;
    static int filDesde, colDesde;
    static int contInicio = 0;
    static String nombre1 = "1", nombre2 = "2";
    static int ganador = 0;

    TextView lblInfo;
    GridLayout tablero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        initComponents();
        actualizarTablero();
    }

    private void initComponents() {
        lblInfo = findViewById(R.id.lblInfo);
        tablero = findViewById(R.id.tablero);

        // Inicializamos los botones usando sus índices en el GridLayout
        for (int i = 0; i < tablero.getChildCount(); i++) {
            View v = tablero.getChildAt(i);
            v.setTag(new int[]{i / 3, i % 3}); // Guardamos fila y columna en el TAG
            v.setOnClickListener(this::clickOnCasilla);
        }
    }

    private void actualizarTablero() {
        for (int i = 0; i < tablero.getChildCount(); i++) {
            int f = i / 3;
            int c = i % 3;
            View v = tablero.getChildAt(i);

            // 1. Asignar imagen según el valor en la matriz
            int fichaDrawable = R.drawable.ficha_0;
            if (casillero[f][c] == FICHA1) fichaDrawable = R.drawable.ficha_1;
            else if (casillero[f][c] == FICHA2) fichaDrawable = R.drawable.ficha_2;

            v.setForeground(ResourcesCompat.getDrawable(getResources(), fichaDrawable, null));

            // 2. Feedback visual de selección (Alpha al 50% si la ficha está levantada)
            if (!esPrimerClic && f == filDesde && c == colDesde) v.setAlpha(0.5f);
            else v.setAlpha(1.0f);
        }
        mostrarQuienJuega();
    }

    private void clickOnCasilla(View v) {
        if (!estaEnJuego) return;

        int[] pos = (int[]) v.getTag();
        int f = pos[0], c = pos[1];
        int turnoActual = esTurno1 ? FICHA1 : FICHA2;

        if (esPrimeraParte) {
            if (casillero[f][c] == BLANCO) {
                casillero[f][c] = turnoActual;
                esTurno1 = !esTurno1;
                if (++contInicio == 6) esPrimeraParte = false;
            }
        } else {
            if (esPrimerClic) {
                if (casillero[f][c] == turnoActual) {
                    filDesde = f; colDesde = c;
                    esPrimerClic = false;
                }
            } else {
                boolean esAdyacente = Math.abs(f - filDesde) <= 1 && Math.abs(c - colDesde) <= 1;
                if (casillero[f][c] == BLANCO && esAdyacente) {
                    casillero[filDesde][colDesde] = BLANCO;
                    casillero[f][c] = turnoActual;
                    esTurno1 = !esTurno1;
                    esPrimerClic = true;
                } else {
                    esPrimerClic = true; // Si falla o toca la misma, deseleccionamos
                }
            }
        }
        detectarFinalJuego();
        actualizarTablero(); // Refrescamos toda la interfaz tras el cambio
    }

    private void detectarFinalJuego() {
        ganador = 0;
        for (int i = 0; i < 3; i++) {
            // Filas
            if (casillero[i][0] != BLANCO && casillero[i][0] == casillero[i][1] && casillero[i][0] == casillero[i][2])
                ganador = casillero[i][0];
            // Columnas
            if (casillero[0][i] != BLANCO && casillero[0][i] == casillero[1][i] && casillero[0][i] == casillero[2][i])
                ganador = casillero[0][i];
        }
        // Diagonales
        if (casillero[1][1] != BLANCO) {
            if (casillero[0][0] == casillero[1][1] && casillero[0][0] == casillero[2][2]) ganador = casillero[1][1];
            if (casillero[0][2] == casillero[1][1] && casillero[0][2] == casillero[2][0]) ganador = casillero[1][1];
        }
        if (ganador != 0) estaEnJuego = false;
    }

    private void mostrarQuienJuega() {
        if (estaEnJuego) {
            lblInfo.setTextColor(getResources().getColor(esTurno1 ? R.color.jugador1 : R.color.jugador2, null));
            lblInfo.setText(getString(R.string.turno) + (esTurno1 ? nombre1 : nombre2));
        } else if (ganador != 0) {
            lblInfo.setTextColor(getResources().getColor(R.color.info, null));
            lblInfo.setText(getString(R.string.gana) + (ganador == 1 ? nombre1 : nombre2));
        }
    }

    public void btnEmpezar(View v) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.nueva_partida)
                .setMessage(R.string.quieres_una_nueva_partida)
                .setNegativeButton(R.string.no, null)
                .setPositiveButton(R.string.si, (dialog, which) -> nuevaPartida())
                .show();
    }

    private void nuevaPartida() {
        esPrimeraParte = true; esTurno1 = true; estaEnJuego = true;
        contInicio = 0; esPrimerClic = true; ganador = 0;
        for (int f = 0; f < 3; f++)
            for (int c = 0; c < 3; c++) casillero[f][c] = BLANCO;
        actualizarTablero();
    }

    public void btnNombres(View view) {
        final View layout_nombres = getLayoutInflater().inflate(R.layout.preguntar_nombres, null);
        EditText txt1 = layout_nombres.findViewById(R.id.txtNombre1);
        EditText txt2 = layout_nombres.findViewById(R.id.txtNombre2);
        txt1.setText(nombre1); txt2.setText(nombre2);

        new AlertDialog.Builder(this)
                .setTitle(R.string.nombre_jugadores)
                .setView(layout_nombres)
                .setPositiveButton(R.string.guardar, (d, w) -> {
                    nombre1 = txt1.getText().toString().isEmpty() ? "1" : txt1.getText().toString();
                    nombre2 = txt2.getText().toString().isEmpty() ? "2" : txt2.getText().toString();
                    actualizarTablero();
                }).show();
    }
}