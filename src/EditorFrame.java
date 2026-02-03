import javax.swing.*;
import java.awt.*;
import java.io.File;

public class EditorFrame extends JFrame {
    private JTextArea txtEditor = new JTextArea();
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

        setupEstilo();
        setupMenus();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(txtEditor), new JScrollPane(txtConsola));
        split.setDividerLocation(450);
        add(split);
    }

    private void setupEstilo() {
        txtEditor.setBackground(new Color(30, 30, 30));
        txtEditor.setForeground(new Color(220, 220, 220));
        txtEditor.setCaretColor(Color.WHITE);
        txtEditor.setFont(new Font("Consolas", Font.PLAIN, 14));
        txtConsola.setBackground(new Color(20, 20, 20));
        txtConsola.setForeground(new Color(150, 255, 150));
        txtConsola.setEditable(false);
    }

    private void setupMenus() {
        JMenuBar mb = new JMenuBar();

        // --- MENÚ ARCHIVO ---
        JMenu mArchivo = new JMenu("Archivo");
        agregarItem(mArchivo, "Nuevo", e -> {
            txtEditor.setText("");
            archivoActual = null;
            setTitle("Nuevo Archivo");
        });
        agregarItem(mArchivo, "Abrir", e -> {
            archivoActual = ManejadorArchivos.abrir(this, txtEditor);
            if (archivoActual != null)
                setTitle("Editando: " + archivoActual.getName());
        });
        agregarItem(mArchivo, "Guardar", e -> guardar());
        agregarItem(mArchivo, "Guardar como...", e -> {
            File f = ManejadorArchivos.guardarComo(this, txtEditor);
            if (f != null) {
                archivoActual = f;
                setTitle("Guardado: " + archivoActual.getName());
            }
        });
        mArchivo.addSeparator();
        agregarItem(mArchivo, "Salir", e -> System.exit(0));

        JMenu mCompilar = new JMenu("Compilar");
        agregarItem(mCompilar, "Análisis Léxico", e -> {
            if (validarArchivo())
                txtConsola.setText("");
            txtConsola.setForeground(new Color(150, 255, 150));
            txtConsola.setText(lexico.ejecutar(archivoActual, elementos));
        });
        agregarItem(mCompilar, "Análisis Sintáctico", e -> {
            if (validarArchivo())
                txtConsola.setText("");
            String resultado = sintactico.analizarEstructuras(archivoActual);

            if (resultado.contains("Error [")) {
                txtConsola.setForeground(Color.RED);
            } else {
                txtConsola.setForeground(new Color(150, 255, 150));
            }

            txtConsola.setText(resultado);
        });
        agregarItem(mCompilar, "Traducir Reservadas", e -> {
            txtConsola.setForeground(new Color(150, 255, 150));
            traducir();
        });

        mb.add(mArchivo);
        mb.add(mCompilar);
        setJMenuBar(mb);
    }

    private void agregarItem(JMenu menu, String nombre, java.awt.event.ActionListener accion) {
        JMenuItem item = new JMenuItem(nombre);
        item.addActionListener(accion);
        menu.add(item);
    }

    private void traducir() {
        txtConsola.setText("--- Traducción de Reservadas ---\n");
        String[] lineas = txtEditor.getText().split("\n");
        boolean encontroAlguna = false;

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            StringBuilder halladasEnLinea = new StringBuilder();
            boolean hayEnEstaLinea = false;

            // Buscamos cada palabra del diccionario en la línea actual
            for (String palabraClave : elementos.PalabrasReservadas.keySet()) {
                if (linea.matches(".*\\b" + palabraClave + "\\b.*")) {
                    halladasEnLinea.append(palabraClave).append(" → ")
                            .append(elementos.PalabrasReservadas.get(palabraClave)).append("  ");
                    hayEnEstaLinea = true;
                    encontroAlguna = true;
                }
            }

            if (hayEnEstaLinea) {
                txtConsola.append("Línea " + (i + 1) + ": " + halladasEnLinea.toString() + "\n");
            }
        }

        if (!encontroAlguna) {
            txtConsola.append("No se encontraron palabras reservadas en el código.\n");
        } else {
            txtConsola.append("\nTraducción completada.\n");
        }
    }

    private void guardar() {
        if (archivoActual == null)
            archivoActual = ManejadorArchivos.guardarComo(this, txtEditor);
        else
            ManejadorArchivos.guardarExistente(archivoActual, txtEditor);
        if (archivoActual != null)
            setTitle("Guardado: " + archivoActual.getName());
    }

    private boolean validarArchivo() {
        if (archivoActual == null) {
            JOptionPane.showMessageDialog(this, "Debe guardar el archivo (.c) antes de procesar.");
            return false;
        }
        ManejadorArchivos.guardarExistente(archivoActual, txtEditor);
        return true;
    }
}