import javax.swing.*;
import java.io.*;

public class ManejadorArchivos {
    
    public static File abrir(JFrame padre, JTextArea editor) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Abrir archivo fuente (.c)");
        if (fc.showOpenDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                editor.read(br, null);
                return f;
            } catch (IOException e) {
                JOptionPane.showMessageDialog(padre, "Error al leer el archivo");
            }
        }
        return null;
    }

    public static File guardarComo(JFrame padre, JTextArea editor) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Guardar como...");
        if (fc.showSaveDialog(padre) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            // Asegurar que tenga extensión .c si no la tiene
            if (!f.getName().endsWith(".c")) {
                f = new File(f.getAbsolutePath() + ".c");
            }
            guardarExistente(f, editor);
            return f;
        }
        return null;
    }

    public static void guardarExistente(File f, JTextArea editor) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
            editor.write(bw);
        } catch (IOException e) {
            System.err.println("Error crítico al guardar: " + e.getMessage());
        }
    }
}