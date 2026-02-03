import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.Stack;

public class AnalizadorSintactico {

    public String analizarEstructuras(File archivoC) {
        StringBuilder reporte = new StringBuilder();
        String rutaSyntax = archivoC.getAbsolutePath().replace(".c", ".syntax");
        int errores = 0;

        reporte.append("--- REPORTE SINTÁCTICO INTEGRAL ---\n");

        try (PrintWriter escribir = new PrintWriter(new FileWriter(rutaSyntax))) {
            List<String> lineas = Files.readAllLines(archivoC.toPath());
            Stack<SymbolPos> llaves = new Stack<>();
            Stack<SymbolPos> parentesis = new Stack<>();

            for (int i = 0; i < lineas.size(); i++) {
                String lineaOriginal = lineas.get(i);
                String lineaTrim = lineaOriginal.trim();
                int numL = i + 1;

                if (lineaTrim.isEmpty() || lineaTrim.startsWith("#") || lineaTrim.startsWith("//"))
                    continue;

                // IF, WHILE, FOR
                if (lineaTrim.startsWith("if") || lineaTrim.startsWith("while") || lineaTrim.startsWith("for")) {
                    if (!lineaTrim.contains("(") || !lineaTrim.contains(")")) {
                        reporte.append(String.format("Error [L: %d, C: %d]: Estructura '%s' requiere paréntesis ( ).\n",
                                numL, lineaOriginal.indexOf(lineaTrim.substring(0, 2)) + 1, lineaTrim.split(" ")[0]));
                        errores++;
                    }
                }

                // SWITCH
                if (lineaTrim.startsWith("switch")) {
                    int col = lineaOriginal.indexOf("switch") + 1;
                    if (!lineaTrim.contains("(") || !lineaTrim.contains(")")) {
                        reporte.append("Error [L: ").append(numL).append(", C: ").append(col)
                                .append("]: 'switch' requiere (variable).\n");
                        errores++;
                    }
                    if (!buscarEnProximasLineas(lineas, i, "{")) {
                        reporte.append("Error [L: ").append(numL).append(", C: ").append(col)
                                .append("]: 'switch' requiere apertura de llave '{'.\n");
                        errores++;
                    }
                }

                // DO-WHILE
                if (lineaTrim.startsWith("do")) {
                    if (!validarDoWhile(lineas, i)) {
                        reporte.append("Error [L: ").append(numL).append(", C: ")
                                .append(lineaOriginal.indexOf("do") + 1).append("]: 'do' sin 'while(...);'.\n");
                        errores++;
                    }
                }

                // ELSE (Verificar que no tenga condición pegada como else(x))
                if (lineaTrim.startsWith("else") && lineaTrim.contains("(") && !lineaTrim.contains("if")) {
                    reporte.append("Error [L: ").append(numL).append(", C: ").append(lineaOriginal.indexOf("else") + 1)
                            .append("]: 'else' no lleva condición (use 'else if').\n");
                    errores++;
                }

                // --- VALIDACIÓN DE PUNTO Y COMA ---
                if (necesitaPuntoYComa(lineaTrim)) {
                    reporte.append("Error [L: ").append(numL).append(", C: ").append(lineaOriginal.length())
                            .append("]: Falta ';' al final de la instrucción.\n");
                    errores++;
                }

                // --- BALANCE DE SÍMBOLOS CON COLUMNA ---
                char[] caracteres = lineaOriginal.toCharArray();
                for (int c = 0; c < caracteres.length; c++) {
                    int colActual = c + 1;
                    if (caracteres[c] == '(')
                        parentesis.push(new SymbolPos(numL, colActual));
                    else if (caracteres[c] == ')') {
                        if (parentesis.isEmpty()) {
                            reporte.append("Error [L: ").append(numL).append(", C: ").append(colActual)
                                    .append("]: ')' de cierre sin apertura.\n");
                            errores++;
                        } else
                            parentesis.pop();
                    } else if (caracteres[c] == '{')
                        llaves.push(new SymbolPos(numL, colActual));
                    else if (caracteres[c] == '}') {
                        if (llaves.isEmpty()) {
                            reporte.append("Error [L: ").append(numL).append(", C: ").append(colActual)
                                    .append("]: '}' de cierre sin apertura.\n");
                            errores++;
                        } else
                            llaves.pop();
                    }
                }
            }

            // Validaciones finales de la pila
            while (!parentesis.isEmpty()) {
                SymbolPos p = parentesis.pop();
                reporte.append("Error [L: ").append(p.l).append(", C: ").append(p.c)
                        .append("]: Paréntesis nunca se cerró.\n");
                errores++;
            }
            while (!llaves.isEmpty()) {
                SymbolPos p = llaves.pop();
                reporte.append("Error [L: ").append(p.l).append(", C: ").append(p.c)
                        .append("]: Llave '{' nunca se cerró.\n");
                errores++;
            }

            reporte.append("\n>>> Análisis finalizado. Errores encontrados: ").append(errores);
            escribir.println(reporte.toString());

        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        return reporte.toString();
    }

    // Clase interna para guardar posición
    private static class SymbolPos {
        int l, c;

        SymbolPos(int l, int c) {
            this.l = l;
            this.c = c;
        }
    }

    private boolean necesitaPuntoYComa(String l) {
        return !(l.isEmpty() || l.endsWith(";") || l.endsWith("{") || l.endsWith("}") ||
                l.startsWith("if") || l.startsWith("for") || l.startsWith("while") ||
                l.startsWith("switch") || l.startsWith("else") || l.equals("do") || l.startsWith("#"));
    }

    private boolean buscarEnProximasLineas(List<String> lineas, int inicio, String texto) {
        for (int i = inicio; i < Math.min(inicio + 5, lineas.size()); i++) {
            if (lineas.get(i).contains(texto))
                return true;
        }
        return false;
    }

    private boolean validarDoWhile(List<String> lineas, int inicio) {
        for (int i = inicio; i < lineas.size(); i++) {
            String l = lineas.get(i).trim();
            if (l.startsWith("while"))
                return l.endsWith(";");
        }
        return false;
    }
}