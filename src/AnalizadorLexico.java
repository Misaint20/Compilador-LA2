import java.io.*;
import java.util.*;

public class AnalizadorLexico {
    public String ejecutar(File archivo, Elementos elementos) {
        StringBuilder tokensParaConsola = new StringBuilder();
        String rutaBack = archivo.getAbsolutePath().replace(".c", ".back");
        List<String> tokens = new ArrayList<>();

        try (FileReader fr = new FileReader(archivo)) {
            int i;
            StringBuilder tokenActual = new StringBuilder();

            while ((i = fr.read()) != -1) {
                char c = (char) i;

                if (Character.isWhitespace(c) || isSymbol(c)) {
                    if (tokenActual.length() > 0) {
                        tokens.add(tokenActual.toString());
                        tokenActual.setLength(0);
                    }
                    if (isSymbol(c)) {
                        tokens.add(String.valueOf(c));
                    }
                } else {
                    tokenActual.append(c);
                }
            }
            if (tokenActual.length() > 0) {
                tokens.add(tokenActual.toString());
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

        try (PrintWriter escribir = new PrintWriter(new FileWriter(rutaBack))) {
            for (int j = 0; j < tokens.size(); j++) {
                String t = tokens.get(j);
                escribir.println(t);

                String clasificacion = "";

                if (t.length() == 1 && isSymbol(t.charAt(0))) {
                    clasificacion = "Símbolo";
                } else if (esTipoDato(t)) {
                    clasificacion = "Tipo de dato";
                } else if (esEstructuraControl(t)) {
                    clasificacion = "Estructura de control / cíclica";
                } else if (elementos.PalabrasReservadas.containsKey(t)) {
                    clasificacion = "Palabra reservada (" + elementos.PalabrasReservadas.get(t) + ")";
                } else if (t.matches("^[a-zA-Z_]\\w*$")) {
                    if (j + 1 < tokens.size() && tokens.get(j + 1).equals("(")) {
                        clasificacion = "Declaración/Llamada de función";
                    } else {
                        clasificacion = "Identificador";
                    }
                } else if (t.matches("-?\\d+") || t.matches("-?\\d+\\.\\d+")) {
                    clasificacion = "Número";
                } else {
                    clasificacion = "Desconocido/Literal";
                }

                tokensParaConsola.append(t).append(" -> ").append(clasificacion).append("\n");
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }

        return tokensParaConsola.toString();
    }

    private boolean isSymbol(char c) {
        return "(){}[];,+-*/%=&!<>#".indexOf(c) != -1;
    }

    private boolean esTipoDato(String t) {
        return Arrays.asList("int", "float", "double", "char", "void", "short", "long", "bool").contains(t);
    }

    private boolean esEstructuraControl(String t) {
        return Arrays.asList("if", "else", "switch", "case", "for", "while", "do", "break", "continue", "return")
                .contains(t);
    }
}