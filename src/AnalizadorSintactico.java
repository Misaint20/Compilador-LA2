import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class AnalizadorSintactico {

    public String analizarEstructuras(File archivoC) {
        StringBuilder reporte = new StringBuilder();
        int errores = 0;

        try {
            List<String> lineas = Files.readAllLines(archivoC.toPath());
            String contenido = String.join("\n", lineas);
            Stack<Integer> pLla = new Stack<>();
            Stack<Integer> pPar = new Stack<>();

            reporte.append("--- RECONOCIMIENTO Y VALIDACIÓN ---\n\n");

            Pattern pFunc = Pattern.compile("\\b(int|void|float|char)\\b\\s+(\\w+)\\s*\\((.*?)\\)\\s*\\{");
            Matcher mFunc = pFunc.matcher(contenido);

            while (mFunc.find()) {
                String tipo = mFunc.group(1);
                String nombre = mFunc.group(2); 
                String argumentosRaw = mFunc.group(3);

                reporte.append(tipo).append("\nfuncion\n(\n");

                // Validacion de argumentos dentro de parentesis
                if (!argumentosRaw.trim().isEmpty()) {
                    String[] args = argumentosRaw.split(",");
                    for (int j = 0; j < args.length; j++) {
                        String arg = args[j].trim();
                        if (arg.matches("(int|float|char|void|double)\\s+\\w+")) {
                            String[] partes = arg.split("\\s+");
                            reporte.append(partes[0]).append("\nidentificador");
                            if (j < args.length - 1)
                                reporte.append("\n,");
                            reporte.append("\n");
                        } else {
                            reporte.append("!!! ERROR EN ARGUMENTOS: ").append(arg).append("\n");
                            errores++;
                        }
                    }
                }
                reporte.append(")\n{\n...\n}\n");
            }

            for (int i = 0; i < lineas.size(); i++) {
                String lineaOriginal = lineas.get(i);
                String linea = lineaOriginal.trim();
                int nL = i + 1;

                if (linea.isEmpty() || linea.startsWith("#") || linea.startsWith("//"))
                    continue;

                boolean esEstructura = false;
                if (linea.startsWith("if")) {
                    reporte.append("L").append(nL).append(": [if] ");
                    esEstructura = true;
                } else if (linea.startsWith("else")) {
                    reporte.append("L").append(nL).append(": [else] ");
                    esEstructura = true;
                } else if (linea.startsWith("for")) {
                    reporte.append("L").append(nL).append(": [for] ");
                    esEstructura = true;
                } else if (linea.startsWith("while") && !linea.endsWith(";")) {
                    reporte.append("L").append(nL).append(": [while] ");
                    esEstructura = true;
                } else if (linea.startsWith("do")) {
                    reporte.append("L").append(nL).append(": [do-while] ");
                    esEstructura = true;
                } else if (linea.startsWith("switch")) {
                    reporte.append("L").append(nL).append(": [switch] ");
                    esEstructura = true;
                }

                if (linea.startsWith("if") || (linea.startsWith("while") && !linea.endsWith(";"))
                        || linea.startsWith("for") || linea.startsWith("switch")) {
                    if (!linea.contains("(") || !linea.contains(")")) {
                        reporte.append(" <--- ERROR: Falta ( )");
                        errores++;
                    }
                }

                if (necesitaPuntoYComa(linea)) {
                    reporte.append("\nError [L: ").append(nL).append(", C: ").append(lineaOriginal.length())
                            .append("]: Falta ';' al final.");
                    errores++;
                }

                for (char c : lineaOriginal.toCharArray()) {
                    if (c == '(')
                        pPar.push(nL);
                    else if (c == ')') {
                        if (pPar.isEmpty()) {
                            reporte.append("\nError [L: ").append(nL).append("]: ')' sin apertura.");
                            errores++;
                        } else
                            pPar.pop();
                    } else if (c == '{')
                        pLla.push(nL);
                    else if (c == '}') {
                        if (pLla.isEmpty()) {
                            reporte.append("\nError [L: ").append(nL).append("]: '}' sin apertura.");
                            errores++;
                        } else
                            pLla.pop();
                    }
                }
                if (esEstructura && !linea.contains("Error"))
                    reporte.append("\n");
            }

            while (!pPar.isEmpty()) {
                reporte.append("\nError: Paréntesis abierto en L").append(pPar.pop()).append(" no cerró.");
                errores++;
            }
            while (!pLla.isEmpty()) {
                reporte.append("\nError: Llave '{' abierta en L").append(pLla.pop()).append(" no cerró.");
                errores++;
            }

        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

        if (errores > 0)
            reporte.append("\n\n>>> TOTAL DE ERRORES: ").append(errores);
        else
            reporte.append("\n\n>>> ANÁLISIS EXITOSO: Estructuras correctas.");

        return reporte.toString();
    }

    private boolean necesitaPuntoYComa(String l) {
        return !(l.isEmpty() || l.endsWith(";") || l.endsWith("{") || l.endsWith("}") ||
                l.startsWith("if") || l.startsWith("for") || l.startsWith("while") ||
                l.startsWith("switch") || l.startsWith("else") || l.equals("do") || l.startsWith("#"));
    }
}