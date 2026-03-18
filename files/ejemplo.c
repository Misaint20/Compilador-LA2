#include <stdio.h>

int a;

float suma(float a, float b){ 
    return a + b;
}

int main() {
    int num1, num2, suma;
    char b, c;
    printf("Introduce el primer numero: ");
    scanf("%d", &num1);
    printf("Introduce el segundo numero: ");
    scanf("%d", &num2);

    if (num1 % 2 == 0) {
        printf("%d es par.\n", num1);
    } else {
        printf("%d es impar.\n", num1);
    }

    b + c;

    if (num2 % 2 == 0) {
        printf("%d es par.\n", num2);
    } else {
        printf("%d es impar.\n", num2);
    }
    
    suma = num1 + num2;
    printf("La suma es: %d\n", suma);
    return 0;
}
