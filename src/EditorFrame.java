import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

public class EditorFrame extends JFrame {
    private JTextPane txtEditor = new JTextPane();
    private JTextArea txtConsola = new JTextArea();
    private File archivoActual = null;
    private AnalizadorLexico lexico = new AnalizadorLexico();
    private AnalizadorSintactico sintactico = new AnalizadorSintactico();
    private Elementos elementos = new Elementos();

    public EditorFrame() {
        setTitle("Compilador C");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setupUI();
        setupMenus();
    }

    private void setupUI() {
        txtEditor.setBackground(new Color(35, 35, 35));
        txtEditor.setForeground(Color.WHITE);
        txtEditor.setFont(new Font("Consolas", Font.PLAIN, 16));
        txtEditor.setCaretColor(Color.WHITE);

        txtConsola.setBackground(new Color(20, 20, 20));
        txtConsola.setForeground(new Color(150, 255, 150));
        txtConsola.setEditable(false);
        txtConsola.setFont(new Font("Monospaced", Font.PLAIN, 14));
        txtConsola.setBorder(new EmptyBorder(10, 20, 10, 10));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(txtEditor), new JScrollPane(txtConsola));
        split.setDividerLocation(450);
        add(split);
    }

    private void setupMenus() {
        JMenuBar mb = new JMenuBar();
        JMenu mArc = new JMenu("Archivo");
        agregarItem(mArc, "Nuevo", e -> { txtEditor.setText(""); archivoActual = null; });
        agregarItem(mArc, "Abrir", e -> { archivoActual = ManejadorArchivos.abrir(this, txtEditor); });
        agregarItem(mArc, "Guardar", e -> guardar());
        agregarItem(mArc, "Guardar como", e -> { archivoActual = ManejadorArchivos.guardarComo(this, txtEditor); });
        mArc.addSeparator();
        agregarItem(mArc, "Salir", e -> System.exit(0));

        JMenu mComp = new JMenu("Compilar");
        agregarItem(mComp, "Análisis Léxico", e -> {
            if (val()) {
                txtConsola.setText("");
                txtConsola.setForeground(new Color(150, 255, 150));
                txtConsola.setText(lexico.ejecutar(archivoActual, elementos));
            }
        });

        agregarItem(mComp, "Análisis Sintáctico", e -> {
            if (val()) {
                txtConsola.setText("");
                String res = sintactico.analizarEstructuras(archivoActual);
                if (res.contains("Error")) txtConsola.setForeground(Color.RED);
                else txtConsola.setForeground(new Color(150, 255, 150));
                txtConsola.setText(res);
            }
        });

        agregarItem(mComp, "Traducir", e -> traducir());

        mb.add(mArc); mb.add(mComp);
        setJMenuBar(mb);
    }

    private void traducir() {
        if (txtEditor.getText().isEmpty()) return;
        txtConsola.setText("");
        txtConsola.setForeground(new Color(150, 255, 150));
        String[] lineas = txtEditor.getText().split("\n");
        for (int i = 0; i < lineas.length; i++) {
            for (String k : elementos.PalabrasReservadas.keySet()) {
                if (lineas[i].contains(k)) {
                    txtConsola.append("L" + (i + 1) + ": " + k + " -> " + elementos.PalabrasReservadas.get(k) + "\n");
                }
            }
        }
    }

    private void guardar() {
        if (archivoActual == null) archivoActual = ManejadorArchivos.guardarComo(this, txtEditor);
        else ManejadorArchivos.guardarExistente(archivoActual, txtEditor);
    }

    private boolean val() {
        if (archivoActual == null) {
            JOptionPane.showMessageDialog(this, "Debe guardar el archivo primero.");
            return false;
        }
        ManejadorArchivos.guardarExistente(archivoActual, txtEditor);
        return true;
    }

    private void agregarItem(JMenu m, String n, java.awt.event.ActionListener a) {
        JMenuItem i = new JMenuItem(n); i.addActionListener(a); m.add(i);
    }
}