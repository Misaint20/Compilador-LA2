import java.io.*;

public class AnalizadorLexico {
    public String ejecutar(File archivo, Elementos elementos) {
        StringBuilder tokensParaConsola = new StringBuilder();
        String rutaBack = archivo.getAbsolutePath().replace(".c", ".back");

        try (FileReader fr = new FileReader(archivo);
                PrintWriter escribir = new PrintWriter(new FileWriter(rutaBack))) {

            int i;
            StringBuilder tokenActual = new StringBuilder();

            while ((i = fr.read()) != -1) {
                char c = (char) i;

                if (Character.isWhitespace(c) || isSymbol(c)) {
                    if (tokenActual.length() > 0) {
                        String t = tokenActual.toString();
                        escribir.println(t);
                        tokensParaConsola.append(t).append("\n");
                        tokenActual.setLength(0);
                    }
                    if (isSymbol(c)) {
                        escribir.println(c);
                        tokensParaConsola.append(c).append("\n");
                    }
                } else {
                    tokenActual.append(c);
                }
            }
            if (tokenActual.length() > 0) {
                String t = tokenActual.toString();
                escribir.println(t);
                tokensParaConsola.append(t).append("\n");
            }

        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
        return tokensParaConsola.toString();
    }

    private boolean isSymbol(char c) {
        return "(){}[];,+-*/%=&!<>#".indexOf(c) != -1;
    }
}