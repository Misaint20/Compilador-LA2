import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.*;

public class AnalizadorSintactico {

    private int errores = 0;
    private StringBuilder reporte = new StringBuilder();
    private String funcionActual = "Global";
    private List<Variable> variables = new ArrayList<>();

    static class Variable {
        String tipo;
        String nombre;
        String funcion;

        Variable(String tipo, String nombre, String funcion) {
            this.tipo = tipo;
            this.nombre = nombre;
            this.funcion = funcion;
        }
    }

    public String analizarEstructuras(File archivoC) {
        reporte.setLength(0);
        errores = 0;
        funcionActual = "Global";
        variables.clear();
        try {
            List<String> lineas = Files.readAllLines(archivoC.toPath());
            reporte.append("--- RECONOCIMIENTO Y VALIDACIÓN ---\n\n");
            validarEstructuraGlobal(lineas);
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

        if (errores > 0)
            reporte.append("\n\n>>> TOTAL DE ERRORES: ").append(errores);
        else {
            reporte.append("\n\n>>> ANÁLISIS EXITOSO: Código y estructuras sintácticamente correctas.");
            reporte.append("\n\n--- TABLA DE VARIABLES ---\n");
            reporte.append(String.format("%-15s | %-15s | %-15s\n", "FUNCION", "TIPO", "NOMBRE"));
            reporte.append("--------------------------------------------------\n");
            for (Variable v : variables) {
                reporte.append(String.format("%-15s | %-15s | %-15s\n", v.funcion, v.tipo, v.nombre));
            }
        }

        return reporte.toString();
    }

    public void validarEstructuraGlobal(List<String> lineas) {
        boolean enFuncion = false;
        List<String> bloqueActual = new ArrayList<>();
        Stack<Integer> llaves = new Stack<>();
        int nl = 0;

        for (String lineaOriginal : lineas) {
            nl++;
            String linea = lineaOriginal.trim();
            if (linea.isEmpty() || linea.startsWith("//"))
                continue;

            if (linea.startsWith("#")) {
                if (!linea.matches("^#include\\s*[<\"].+[>\"]$") && !linea.matches("^#define\\s+\\w+.*")) {
                    registrarError(nl, "Directiva preprocesador inválida: " + linea);
                } else {
                    reporte.append("L").append(nl).append(": Directiva correcta -> ").append(linea).append("\n");
                }
                continue;
            }

            if (!enFuncion) {
                if (linea.matches("^(int|void|float|char|double)\\s+[a-zA-Z_]\\w*\\s*\\(.*\\)\\s*\\{?\\s*$")) {
                    enFuncion = true;
                    Matcher mFunc = Pattern.compile("^(?:int|void|float|char|double)\\s+([a-zA-Z_]\\w*)").matcher(linea);
                    if (mFunc.find()) {
                        funcionActual = mFunc.group(1);
                    }
                    if (linea.endsWith("{")) {
                        llaves.push(nl);
                    }
                    reporte.append("L").append(nl).append(": Declaración de función encontrada\n");
                    validarArgumentosFuncion(linea, nl);
                } else if (linea.equals("{")) {
                    llaves.push(nl);
                    enFuncion = true;
                } else if (esDeclaracionVariable(linea)) {
                    validarDeclaracionVariable(linea, nl);
                } else {
                    registrarError(nl, "Sentencia global inválida o ámbito incorrecto: " + linea);
                }
            } else {
                bloqueActual.add(linea + " //-L:" + nl); 
                if (linea.contains("{")) {
                    llaves.push(nl);
                }
                if (linea.contains("}")) {
                    if (llaves.isEmpty())
                        registrarError(nl, "'}' sin apertura previa.");
                    else
                        llaves.pop();

                    if (llaves.isEmpty()) {
                        enFuncion = false;
                        validarBloqueSentencias(bloqueActual);
                        bloqueActual.clear();
                        funcionActual = "Global";
                    }
                }
            }
        }
        if (!llaves.isEmpty()) {
            registrarError(nl, "Falta cerrar " + llaves.size() + " llave(s) '}' en el código.");
        }
    }

    private void validarArgumentosFuncion(String linea, int nl) {
        Matcher m = Pattern.compile("\\((.*?)\\)").matcher(linea);
        if (m.find()) {
            String args = m.group(1).trim();
            if (!args.isEmpty() && !args.equals("void")) {
                String[] params = args.split(",");
                for (String param : params) {
                    String pTrim = param.trim();
                    if (!pTrim.matches("^(int|float|char|double|void)\\s+[a-zA-Z_]\\w*(?:\\s*\\[\\s*\\])?$")) {
                        registrarError(nl, "Argumento de función inválido: " + pTrim);
                    } else {
                        Matcher mParam = Pattern.compile("^(int|float|char|double|void)\\s+([a-zA-Z_]\\w*)").matcher(pTrim);
                        if (mParam.find()) {
                            variables.add(new Variable(mParam.group(1), mParam.group(2), funcionActual));
                        }
                    }
                }
            }
        }
    }

    public void validarBloqueSentencias(List<String> sentencias) {
        for (String sent : sentencias) {
            String[] parts = sent.split("//-L:");
            String linea = parts[0].trim();
            int nl = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

            if (linea.isEmpty())
                continue;

            String lnEvaluada = linea;
            if (lnEvaluada.startsWith("}"))
                lnEvaluada = lnEvaluada.substring(1).trim();
            if (lnEvaluada.endsWith("{"))
                lnEvaluada = lnEvaluada.substring(0, lnEvaluada.length() - 1).trim();
            if (lnEvaluada.endsWith("}"))
                lnEvaluada = lnEvaluada.substring(0, lnEvaluada.length() - 1).trim();

            if (lnEvaluada.isEmpty())
                continue;

            if (lnEvaluada.startsWith("if") || lnEvaluada.startsWith("while") || lnEvaluada.startsWith("for")
                    || lnEvaluada.startsWith("switch")) {
                validarEstructurasControl(lnEvaluada, nl);
            } else if (lnEvaluada.startsWith("else") || lnEvaluada.startsWith("do")) {
                reporte.append("L").append(nl).append(": Estructura de control encontrada -> ").append(lnEvaluada)
                        .append("\n");
            } else if (esDeclaracionVariable(lnEvaluada)) {
                validarDeclaracionVariable(lnEvaluada, nl);
            } else {
                validarSentenciaSimple(lnEvaluada, nl);
            }
        }
    }

    private void validarSentenciaSimple(String linea, int nl) {
        if (!linea.endsWith(";")) {
            registrarError(nl, "Falta ';' al final de la instrucción: " + linea);
            return;
        }

        if (linea.matches("^[a-zA-Z_]\\w*\\s*\\(.*\\)\\s*;$")) {
            reporte.append("L").append(nl).append(": Llamada a función válida\n");
        } else if (linea.matches("^[a-zA-Z_]\\w*\\s*(?:\\+|-|\\*|/|%|)=.+;$")
                || linea.matches("^[a-zA-Z_]\\w*(?:\\+\\+|--|\\+=\\d+|-=\\d+);$")) {
            reporte.append("L").append(nl).append(": Asignación / Operación válida\n");
            
            // Validar la expresión al lado derecho de la asignación
            String operacionStr = linea.replaceAll("^[a-zA-Z_]\\w*\\s*(?:\\+|-|\\*|/|%|)=", "").replaceAll(";$","").trim();
            if (!operacionStr.isEmpty() && !operacionStr.matches("^(?:\\+\\+|--|\\+=\\d+|-=\\d+)$")) {
                if (validarExpresion(operacionStr)) {
                    reporte.append("L").append(nl).append(": -> Expresión de asignación correcta según diagrama\n");
                } else {
                    registrarError(nl, "Expresión mal formada en asignación/operación: " + operacionStr);
                }
            }
        } else if (linea.matches("^return(\\s+.*)?;$") || linea.equals("break;") || linea.equals("continue;")) {
            reporte.append("L").append(nl).append(": Sentencia de control (return/break/continue) válida\n");
            if (linea.startsWith("return ")) {
                String expReturn = linea.substring(7).replaceAll(";$","").trim();
                if (!expReturn.isEmpty() && !validarExpresion(expReturn)) {
                    registrarError(nl, "Expresión mal formada en return: " + expReturn);
                }
            }
        } else {
            // Evaluacion de expresiones independientes
            String probExp = linea.substring(0, linea.length()-1).trim();
            if (validarExpresion(probExp)) {
                reporte.append("L").append(nl).append(": Expresión independiente válida\n");
            } else {
                registrarError(nl, "Sentencia no reconocida o estructura mal formada: " + linea);
            }
        }
    }

    public void validarEstructurasControl(String linea, int nl) {
        String estructura = linea.replaceAll("\\(.*", "").trim();
        reporte.append("L").append(nl).append(": Estructura de control encontrada -> ").append(estructura).append("\n");

        if (!linea.contains("(")) {
            registrarError(nl, "A la estructura de control le falta '(' de apertura.");
        } else if (!linea.contains(")")) {
            registrarError(nl, "A la estructura de control le falta ')' de cierre.");
        }
    }

    public void validarDeclaracionVariable(String linea, int nl) {
        if (!linea.endsWith(";")) {
            registrarError(nl, "Declaración falta ';': " + linea);
            return;
        }

        String clean = linea.substring(0, linea.length() - 1).trim();
        String varPattern = "[a-zA-Z_]\\w*(?:\\s*=\\s*[^,;]+)?";
        String declPattern = "^(int|float|char|double|short|long)\\s+" + varPattern + "(?:\\s*,\\s*" + varPattern
                + ")*$";

        if (!clean.matches(declPattern)) {
            registrarError(nl,
                    "Declaración de variable mal formada (tipo, identificador, o asignación inicial): " + clean);
        } else {
            reporte.append("L").append(nl).append(": Declaración de variable correcta\n");
            Matcher md = Pattern.compile("^(int|float|char|double|short|long)\\s+(.*)").matcher(clean);
            if (md.find()) {
                String tipo = md.group(1);
                String varsStr = md.group(2);
                String[] vars = varsStr.split(",");
                for (String v : vars) {
                    String nombre = v.split("=")[0].trim().replaceAll("\\[.*\\]", "");
                    variables.add(new Variable(tipo, nombre, funcionActual));
                }
            }
        }
    }

    private boolean esDeclaracionVariable(String linea) {
        return linea.matches("^(int|float|char|double|short|long)\\s+[a-zA-Z_].*");
    }

    private void registrarError(int linea, String mensaje) {
        reporte.append("Error [L: ").append(linea).append("]: ").append(mensaje).append("\n");
        errores++;
    }

    private Elementos tablaElementos = new Elementos();

    public boolean validarExpresion(String expresion) {
        if (expresion == null || expresion.trim().isEmpty()) {
            return false;
        }
        
        String expr = expresion.trim();
        
        expr = expr.replaceAll("'.'|'\\\\[a-zA-Z0-9]'", " OPERANDO ");
        
        expr = expr.replaceAll("\\b\\d+\\.\\d+\\b", " OPERANDO "); 
        expr = expr.replaceAll("\\b\\d+\\b", " OPERANDO "); 
        
        expr = expr.replaceAll("\\b(true|false)\\b", " OPERANDO ");
        
        expr = expr.replaceAll("\\b[a-zA-Z_]\\w*\\s*\\([^()]*\\)", " OPERANDO ");
        
        expr = expr.replaceAll("\\b[a-zA-Z_]\\w*\\b", " OPERANDO ");
        
        List<String> opDes = new ArrayList<>(tablaElementos.Operadores);
        opDes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        List<String> opUnariosDes = new ArrayList<>(tablaElementos.OperadoresUnarios);
        opUnariosDes.sort((a, b) -> Integer.compare(b.length(), a.length()));

        boolean huboCambios = true;
        while (huboCambios) {
            huboCambios = false;
            String previo = expr;
            
            expr = expr.replaceAll("\\(\\s*OPERANDO\\s*\\)", " OPERANDO ");
            
            for (String opU : opUnariosDes) {
                String rx = Pattern.quote(opU);
                if (opU.equals("sizeof")) rx = "\\bsizeof\\b";
                expr = expr.replaceAll(rx + "\\s*OPERANDO", " OPERANDO ");
            }
            
            for (String opU : opUnariosDes) {
                String rx = Pattern.quote(opU);
                if (opU.equals("sizeof")) continue; 
                expr = expr.replaceAll("OPERANDO\\s*" + rx, " OPERANDO ");
            }
            
            for (String op : opDes) {
                String rx = Pattern.quote(op);
                expr = expr.replaceAll("OPERANDO\\s*" + rx + "\\s*OPERANDO", " OPERANDO ");
            }
            
            if (!expr.equals(previo)) {
                huboCambios = true;
            }
        }
        
        return expr.trim().equals("OPERANDO"); 
    }
}