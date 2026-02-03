import java.io.*;

public class AnalizadorLexico {
    private int numeroLinea = 1;
    private int nErrores = 0;

    public String ejecutar(File archivo, Elementos elementos) {
        numeroLinea = 1;
        nErrores = 0;
        StringBuilder log = new StringBuilder();
        String rutaBack = archivo.getAbsolutePath().replace(".c", ".back");

        try (FileReader fr = new FileReader(archivo);
                PrintWriter escribir = new PrintWriter(new FileWriter(rutaBack))) {

            escribir.println("--- Analisis Léxico ---");

            int i;
            while ((i = fr.read()) != -1) {
                char c = (char) i;
                char clasif = clasificar(i);
                String tipo = switch (clasif) {
                    case 'l' -> "letra";
                    case 'd' -> "digito";
                    case 's' -> "simbolo";
                    case 'n' -> {
                        int lActual = numeroLinea;
                        numeroLinea++;
                        yield "salto_linea (Línea " + lActual + ")";
                    }
                    case 'b' -> "espacio";
                    case '"' -> {
                        saltarCadena(fr);
                        yield "cadena";
                    }
                    case 'c' -> {
                        saltarChar(fr);
                        yield "caracter";
                    }
                    case 'r' -> "ignorar";
                    default -> "desconocido";
                };

                if (!tipo.equals("ignorar")) {
                    // Formato: Carácter: x Tipo: descripcion (ASCII: n)
                    String salidaFormateada = String.format("Carácter: %s %s (ASCII: %d)",
                            (c == '\n' ? "\\n" : c), tipo, i);

                    // Escribir en archivo .back
                    escribir.println(salidaFormateada);
                    // Mostrar en consola del editor
                    log.append(salidaFormateada).append("\n");

                    if (tipo.equals("desconocido"))
                        nErrores++;
                }
            }

            String resumen = "Compilación completada. Errores: " + nErrores;
            escribir.println("\n" + resumen);
            log.append("\n").append(resumen);

        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        return log.toString();
    }

    private char clasificar(int c) {
        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
            return 'l';
        if (c >= 48 && c <= 57)
            return 'd';
        return switch (c) {
            case 10 -> 'n';
            case 32 -> 'b';
            case 34 -> '"';
            case 39 -> 'c';
            case 13 -> 'r';
            case 33, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59, 60, 61, 62, 63, 64, 91, 92, 93, 94, 95, 96,
                    123, 124, 125, 126 ->
                's';
            default -> 'e';
        };
    }

    private void saltarCadena(FileReader r) throws IOException {
        int c;
        while ((c = r.read()) != -1 && c != 34)
            ;
    }

    private void saltarChar(FileReader r) throws IOException {
        int c = r.read();
        if (c == 92)
            r.read();
        r.read();
    }
}