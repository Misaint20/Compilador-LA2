import java.io.*;

public class Lexico {
    private int numeroLinea = 1;
    private int nErrores = 0;
    private StringBuilder logSalida = new StringBuilder();

    public String analizar(File archivoFuente) {
        logSalida.setLength(0);
        numeroLinea = 1;
        nErrores = 0;

        File archivoBack = new File(archivoFuente.getAbsolutePath() + ".back");

        try (FileReader leer = new FileReader(archivoFuente);
                PrintWriter escribir = new PrintWriter(new FileWriter(archivoBack))) {

            int iCaracter;
            while ((iCaracter = leer.read()) != -1) {
                char tipo = clasificar(iCaracter);
                String descripcion = "";

                switch (tipo) {
                    case 'l' -> {
                        descripcion = "letra";
                        escribir.println("letra");
                    }
                    case 'd' -> {
                        descripcion = "digito";
                        escribir.println("digito");
                    }
                    case 's' -> {
                        descripcion = "simbolo";
                        escribir.println("simbolo");
                    }
                    case 'n' -> {
                        descripcion = "salto_linea";
                        numeroLinea++;
                        escribir.println("salto_linea");
                    }
                    case '"' -> {
                        descripcion = "cadena";
                        manejarCadena(leer, escribir);
                    }
                    case 'c' -> {
                        descripcion = "caracter";
                        manejarConstanteCaracter(leer, escribir);
                    }
                    case 'b' -> {
                        descripcion = "espacio";
                        escribir.println("espacio");
                    }
                    case 'r' -> {
                        continue;
                    } // Retorno de carro ignora
                    default -> registrarError(iCaracter);
                }

                if (tipo != 'r' && tipo != 'e') {
                    logSalida.append(
                            String.format("Carácter: %c %s (ASCII: %d)\n", (char) iCaracter, descripcion, iCaracter));
                }
            }
            logSalida.append("\nCompilación completada. Errores: ").append(nErrores);
            logSalida.append("\nArchivo backup creado: ").append(archivoBack.getName());

        } catch (IOException e) {
            logSalida.append("Error de E/S: ").append(e.getMessage());
        }
        return logSalida.toString();
    }

    private char clasificar(int c) {
        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122))
            return 'l';
        if (c >= 48 && c <= 57)
            return 'd';
        return switch (c) {
            case 10 -> 'n';
            case 13 -> 'r';
            case 32 -> 'b';
            case 34 -> '"';
            case 39 -> 'c';
            case 33, 35, 36, 37, 38, 40, 41, 42, 43, 44, 45, 46, 47, 58, 59,
                    60, 61, 62, 63, 64, 91, 92, 93, 94, 95, 96, 123, 124, 125, 126 ->
                's';
            default -> 'e';
        };
    }

    private void manejarCadena(FileReader reader, PrintWriter writer) throws IOException {
        int c;
        while ((c = reader.read()) != -1 && c != 34) {
            if (c == 10)
                numeroLinea++;
            if (c == 92)
                reader.read(); // Salta el escape
        }
        if (c == -1)
            registrarError(-2);
        writer.println("cadena");
    }

    private void manejarConstanteCaracter(FileReader reader, PrintWriter writer) throws IOException {
        int c = reader.read();
        if (c == 92) { // Manejo de '\n', '\t', etc.
            c = reader.read();
            // Simplemente validamos que sea un escape válido
            if ("nrt0'\"\\".indexOf(c) == -1)
                registrarError(39);
        }
        c = reader.read(); // Debería ser la comilla de cierre '
        if (c != 39)
            registrarError(39);
        writer.println("caracter");
    }

    private void registrarError(int code) {
        logSalida.append("Error léxico ").append(code).append(", linea ").append(numeroLinea).append("\n");
        nErrores++;
    }
}