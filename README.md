
# Bookstore - Optimización del Almacenamiento de Datos

Este proyecto optimiza el almacenamiento de datos del inventario de la librería **"Libros y Más"** mediante la **Codificación Huffman** y **Compresión Aritmética** para reducir los costos de almacenamiento y transmisión de datos sin pérdida de información.

## Funcionalidades

### Clases principales:

- **HuffmanEncoder**: Implementa el algoritmo de Codificación Huffman para comprimir los campos de texto (Nombre del libro). Utiliza una cola de prioridad para construir el árbol de Huffman y asignar códigos binarios a cada carácter basado en su frecuencia.
- **ArithmeticCompressor**: Implementa el algoritmo de Compresión Aritmética para el campo **Nombre del libro**. Utiliza un rango de 0 a 65,535 para la compresión de los datos, manejando símbolos basados en su probabilidad.
  
### Operaciones soportadas:

- **Codificación Huffman**: Comprime los campos de texto del inventario (Nombre del libro) generando un archivo comprimido en formato binario.
- **Compresión Aritmética**: Comprime el campo **Nombre del libro** utilizando un rango de valores numéricos para reducir aún más el tamaño del archivo.

## Estructura del Proyecto

```
Bookstore/
│
├── src/
│   ├── Main.java                  # Archivo principal que contiene el punto de entrada del programa
│   ├── HuffmanEncoder.java        # Implementación de la Codificación Huffman
│   └── ArithmeticCompressor.java  # Implementación de la Compresión Aritmética
│
├── input/
│   ├── inventory.csv              # Archivo CSV con el inventario de la librería
│   └── search.csv                 # Archivo CSV con las búsquedas de libros
│
└── output/
    └── result.txt                 # Archivo con los resultados de las compresiones
```

## Ejemplo de uso

1. **Archivo de entrada (CSV)**:
   ```csv
   INSERT; {"isbn":"1234567890","name":"Cien Anos de Soledad","author":"Gabriel Garcia Marquez","category":"Ficcion","price":"20.00","quantity":"10"}
   INSERT; {"isbn":"0987654321","name":"El Principito","author":"Antoine de Saint-Exupery","category":"Ficcion","price":"15.00","quantity":"5"}
   DELETE; {"isbn":"1234567890"}
   SEARCH; {"name":"Don Quijote de la Mancha"}
   ```

2. **Archivo de salida (TXT)**:
   ```txt
   {"isbn":"1122334455","name":"Don Quijote de la Mancha","author":"Miguel de Cervantes","category":"Clasicos","price":"25.00","quantity":"7","namesize":"48","namesizehuffman":"93","namesizearithmetic":"12"}
   {"isbn":"0987654321","name":"El Principito","author":"Antoine de Saint-Exupery","category":"Ficcion","price":"18.00","quantity":"5","namesize":"26","namesizehuffman":"44","namesizearithmetic":"6"}
   Equal: 0
   Decompress: 0
   Huffman: 2
   Arithmetic: 0
   ```

## Respuestas a preguntas

1. **¿Qué algoritmo es mejor para comprimir estos datos y por qué?**
   El algoritmo de **Compresión Aritmética** es generalmente más eficiente para los textos más cortos, ya que utiliza un rango numérico y no depende de la estructura del texto como Huffman. Sin embargo, **Codificación Huffman** puede ser más eficiente cuando los textos contienen muchas repeticiones de ciertos caracteres.

2. **Si el mejor algoritmo fue A, ¿qué se debería considerar para que B fuera mejor?**
   Si el algoritmo de **Compresión Aritmética** fue el mejor, para que **Codificación Huffman** sea más eficiente, sería necesario que el texto tuviera más variabilidad en la frecuencia de los caracteres. Además, una implementación optimizada de la cola de prioridad puede mejorar el rendimiento.

3. **¿Cómo se vio afectado el programa del Lab01 con estos nuevos requerimientos, vale la pena la compresión de datos?**
   La inclusión de compresión ha aumentado la complejidad del código y las operaciones de inserción y búsqueda, pero los beneficios en reducción de costos de almacenamiento y transmisión hacen que la compresión sea útil, especialmente para una librería con una gran cantidad de sucursales.

4. **¿Qué recomendaciones harías para mejorar los algoritmos vistos en clase?**
   - **Codificación Huffman**: Optimizar el uso de la cola de prioridad para manejar mejor los nodos con igual probabilidad y mejorar la gestión de memoria.
   - **Compresión Aritmética**: Utilizar técnicas para manejar rangos más amplios y mejorar la precisión del cálculo, especialmente en textos más largos.

## Ejecución local

### Pasos:

1. Clona el repositorio:
   ```bash
   git clone https://github.com/danielxiquin/Bookstore.git
   ```

2. Abre el proyecto en **Visual Studio Code** o tu entorno preferido para **Java**.

3. Asegúrate de tener **Java 11** o superior instalado.

4. Compila y ejecuta el código con los siguientes comandos:
   ```bash
   javac Main.java
   java Main
   ```

## Recomendaciones

1. **Optimizar el manejo de datos comprimidos**: Considera implementar un buffer o una estructura que gestione los datos comprimidos de manera más eficiente durante la búsqueda.
   
2. **Implementar pruebas unitarias**: Desarrollar pruebas unitarias para cubrir los diferentes casos de compresión y descompresión.
   
3. **Monitorear el rendimiento**: Monitorea el impacto en el rendimiento de la compresión, especialmente cuando se realizan muchas operaciones de búsqueda o inserción.
