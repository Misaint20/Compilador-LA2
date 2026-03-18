import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;

public class Elementos {
    public final Map<String, String> PalabrasReservadas = new LinkedHashMap<>();

    public final List<String> Operadores = Arrays.asList(
            "/", "*", "%", "+", "-", ">", "<", ">=", "<=", "==", "!=", "&&", "||", "=", "+=", "-=", "*=", "/=", "%=", "&", "|", "^", "<<", ">>"
    );

    public final List<String> OperadoresUnarios = Arrays.asList(
            "++", "--", "-", "!", "~", "*", "&", "sizeof"
    );

    public final List<String> TiposOperando = Arrays.asList(
            "Identificador", "Invocacion de Funcion", "Caracter", "Entero", "Flotante", "Boleano"
    );

    public Elementos() {
        // --- Tipos de Datos y Modificadores ---
        PalabrasReservadas.put("int", "entero");
        PalabrasReservadas.put("float", "flotante");
        PalabrasReservadas.put("double", "doble precisión");
        PalabrasReservadas.put("char", "carácter");
        PalabrasReservadas.put("void", "vacío");
        PalabrasReservadas.put("short", "corto");
        PalabrasReservadas.put("long", "largo");
        PalabrasReservadas.put("signed", "con signo");
        PalabrasReservadas.put("unsigned", "sin signo");
        PalabrasReservadas.put("bool", "booleano (_Bool)");
        PalabrasReservadas.put("complex", "complejo");
        PalabrasReservadas.put("imaginary", "imaginario");

        // --- Control de Flujo ---
        PalabrasReservadas.put("if", "si (condicional)");
        PalabrasReservadas.put("else", "si no");
        PalabrasReservadas.put("switch", "seleccionar / conmutar");
        PalabrasReservadas.put("case", "caso");
        PalabrasReservadas.put("default", "por defecto");
        PalabrasReservadas.put("for", "para (bucle)");
        PalabrasReservadas.put("while", "mientras (bucle)");
        PalabrasReservadas.put("do", "hacer");
        PalabrasReservadas.put("break", "romper / salir");
        PalabrasReservadas.put("continue", "continuar");
        PalabrasReservadas.put("goto", "ir a");
        PalabrasReservadas.put("return", "devolver / retornar");

        // --- Almacenamiento y Calificadores ---
        PalabrasReservadas.put("auto", "automático");
        PalabrasReservadas.put("const", "constante");
        PalabrasReservadas.put("extern", "externo");
        PalabrasReservadas.put("register", "registro");
        PalabrasReservadas.put("static", "estático");
        PalabrasReservadas.put("volatile", "volátil");
        PalabrasReservadas.put("restrict", "restringido (C99)");
        PalabrasReservadas.put("inline", "en línea (C99)");

        // --- Estructuras y Definiciones ---
        PalabrasReservadas.put("struct", "estructura");
        PalabrasReservadas.put("union", "unión");
        PalabrasReservadas.put("enum", "enumeración");
        PalabrasReservadas.put("typedef", "definir tipo");
        PalabrasReservadas.put("sizeof", "tamaño de");

        // --- Palabras de C11 y Avanzadas ---
        PalabrasReservadas.put("_Alignas", "alinear como");
        PalabrasReservadas.put("_Alignof", "alineación de");
        PalabrasReservadas.put("_Atomic", "atómico");
        PalabrasReservadas.put("_Generic", "genérico");
        PalabrasReservadas.put("_Noreturn", "sin retorno");
        PalabrasReservadas.put("_Static_assert", "aserción estática");
        PalabrasReservadas.put("_Thread_local", "local al hilo");
    }
}