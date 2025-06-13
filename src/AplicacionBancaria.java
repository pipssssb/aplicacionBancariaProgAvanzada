import entidades.*;
import gestores.*;
import modelo.*;
import notificaciones.*;
import procesamiento.*;
import java.util.ArrayList;
import java.util.Scanner;

public class AplicacionBancaria {
    private static Scanner scanner = new Scanner(System.in);
    private static CuentaBancaria cuenta;
    private static Validador validador = new ValidadorOperacionesImpl();
    private static Procesador<CuentaBancaria> notificador = new NotificadorUnificado();
    private static Procesador<CuentaBancaria> procesadorOperaciones = new ProcesadorOperacionesUnificado();

    public static void main(String[] args) {
        System.out.println("=== SISTEMA BANCARIO ===");

        configurarSistema();
        cuenta = new CuentaBancaria("123456789", "Juan Pérez", 1000.0, "1234");

        Gestor<ContactoFavorito> gestorFav = cuenta.getGestorFavoritos();
        gestorFav.agregar(new ContactoFavorito("María García", "987654321", "maria", "Banco Galicia"));
        gestorFav.agregar(new ContactoFavorito("Carlos López", "456789123", "carlos", "Banco Nacion"));

        notificador.procesar(cuenta, "INICIO_SESION", "Bienvenido al sistema bancario");

        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            int opcion = scanner.nextInt();
            scanner.nextLine();

            switch (opcion) {
                case 1:
                    realizarDeposito();
                    break;
                case 2:
                    realizarRetiro();
                    break;
                case 3:
                    transferirAFavorito();
                    break;
                case 4:
                    gestionarFavoritos();
                    break;
                case 5:
                    consultarHistorial();
                    break;
                case 6:
                    mostrarResumenGastos();
                    break;
                case 7:
                    cambiarPin();
                    break;
                case 8:
                    consultarSaldo();
                    break;
                case 0:
                    notificador.procesar(cuenta, "CIERRE_SESION", "Gracias por usar nuestro sistema");
                    continuar = false;
                    System.out.println("¡Gracias por usar nuestro sistema bancario!");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        }
    }

    private static void configurarSistema() {
        notificador.configurar("CONSOLE");

        procesadorOperaciones.configurar(0.0);
    }

    private static void mostrarMenu() {
        System.out.println("\n=== MENÚ PRINCIPAL ===");
        System.out.println("1. Depositar");
        System.out.println("2. Retirar");
        System.out.println("3. Transferir a favorito");
        System.out.println("4. Gestionar favoritos");
        System.out.println("5. Consultar historial");
        System.out.println("6. Resumen de gastos por categoría");
        System.out.println("7. Cambiar PIN");
        System.out.println("8. Consultar saldo");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opción: ");
    }

    private static void realizarDeposito() {
        System.out.print("Ingrese el monto a depositar: $");
        double monto = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Ingrese su PIN: ");
        String pin = scanner.nextLine();

        if (!validador.validar("DEPOSITO", monto)) {
            System.out.println("Error: " + validador.obtenerMensajeError());
            return;
        }

        if (!validador.validar("HORARIO")) {
            System.out.println("Error: " + validador.obtenerMensajeError());
            return;
        }

        if (cuenta.autenticar(pin)) {
            procesadorOperaciones.procesar(cuenta, "DEPOSITAR", monto);

            notificador.procesar(cuenta, "DEPOSITO",
                    "Depósito realizado por $" + monto + ". Saldo actual: $" + cuenta.getSaldo());
        } else {
            System.out.println("PIN incorrecto");
        }
    }

    private static void realizarRetiro() {
        System.out.print("Ingrese el monto a retirar: $");
        double monto = scanner.nextDouble();
        scanner.nextLine();

        System.out.println("\n=== SELECCIONAR CATEGORÍA ===");
        Gestor<CategoriaGasto> gestorCat = cuenta.getGestorCategorias();
        ArrayList<CategoriaGasto> categorias = gestorCat.listar();

        System.out.println("0. Sin categoría");
        for (int i = 0; i < categorias.size(); i++) {
            CategoriaGasto cat = categorias.get(i);
            System.out.println((i + 1) + ". " + cat.getNombre() +
                    " (Límite: $" + cat.getLimiteMaximo() + ")");
        }

        System.out.print("Seleccione una categoría (0-" + categorias.size() + "): ");
        int opcionCategoria = scanner.nextInt();
        scanner.nextLine();

        String categoria = null;
        if (opcionCategoria > 0 && opcionCategoria <= categorias.size()) {
            categoria = categorias.get(opcionCategoria - 1).getNombre();
        }

        System.out.print("Ingrese su PIN: ");
        String pin = scanner.nextLine();

        if (!validador.validar("RETIRO", monto, cuenta.getSaldo())) {
            System.out.println("Error: " + validador.obtenerMensajeError());
            return;
        }

        if (cuenta.retirar(monto, pin, categoria)) {
            String mensaje = categoria != null ?
                    "Retiro de $" + monto + " en categoría " + categoria :
                    "Retiro de $" + monto;
            notificador.procesar(cuenta, "RETIRO", mensaje + ". Saldo actual: $" + cuenta.getSaldo());
        }
    }

    private static void transferirAFavorito() {
        Gestor<ContactoFavorito> gestorFav = cuenta.getGestorFavoritos();
        ArrayList<ContactoFavorito> favoritos = gestorFav.listar();

        if (favoritos.isEmpty()) {
            System.out.println("No hay contactos favoritos");
            return;
        }

        System.out.println("\n=== CONTACTOS FAVORITOS ===");
        for (int i = 0; i < favoritos.size(); i++) {
            System.out.println((i + 1) + ". " + favoritos.get(i));
        }

        System.out.print("Ingrese el alias del contacto: ");
        String alias = scanner.nextLine();

        System.out.print("Ingrese el monto a transferir: $");
        double monto = scanner.nextDouble();
        scanner.nextLine();

        System.out.print("Ingrese su PIN: ");
        String pin = scanner.nextLine();

        ContactoFavorito favorito = gestorFav.buscar(alias);
        if (favorito != null && cuenta.transferirAFavorito(alias, monto, pin)) {
            notificador.procesar(cuenta, "TRANSFERENCIA",
                    "Transferencia de $" + monto + " a " + favorito.getNombre() +
                            ". Saldo actual: $" + cuenta.getSaldo());
        }
    }

    private static void gestionarFavoritos() {
        System.out.println("\n=== GESTIÓN DE FAVORITOS ===");
        System.out.println("1. Listar favoritos");
        System.out.println("2. Agregar favorito");
        System.out.println("3. Eliminar favorito");
        System.out.println("4. Buscar favorito");
        System.out.print("Seleccione una opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        Gestor<ContactoFavorito> gestorFav = cuenta.getGestorFavoritos();

        switch (opcion) {
            case 1:
                ArrayList<ContactoFavorito> favoritos = gestorFav.listar();
                if (favoritos.isEmpty()) {
                    System.out.println("No hay contactos favoritos");
                } else {
                    System.out.println("\n=== CONTACTOS FAVORITOS ===");
                    for (int i = 0; i < favoritos.size(); i++) {
                        System.out.println((i + 1) + ". " + favoritos.get(i));
                    }
                }
                break;
            case 2:
                System.out.print("Nombre: ");
                String nombre = scanner.nextLine();
                System.out.print("Número de cuenta: ");
                String numeroCuenta = scanner.nextLine();
                System.out.print("Alias: ");
                String alias = scanner.nextLine();
                System.out.print("Banco: ");
                String banco = scanner.nextLine();

                ContactoFavorito nuevo = new ContactoFavorito(nombre, numeroCuenta, alias, banco);
                if (gestorFav.agregar(nuevo)) {
                    System.out.println("Contacto agregado exitosamente");
                } else {
                    System.out.println("Error: Ya existe un contacto con ese alias");
                }
                break;
            case 3:
                System.out.print("Alias a eliminar: ");
                String aliasEliminar = scanner.nextLine();
                if (gestorFav.eliminar(aliasEliminar)) {
                    System.out.println("Contacto eliminado exitosamente");
                } else {
                    System.out.println("No se encontró un contacto con ese alias");
                }
                break;
            case 4:
                System.out.print("Alias a buscar: ");
                String aliasBuscar = scanner.nextLine();
                ContactoFavorito encontrado = gestorFav.buscar(aliasBuscar);
                if (encontrado != null) {
                    System.out.println("Contacto encontrado: " + encontrado);
                } else {
                    System.out.println("No se encontró el contacto");
                }
                break;
        }
    }

    private static void consultarHistorial() {
        System.out.println("\n=== CONSULTAR HISTORIAL ===");
        System.out.println("1. Mostrar todo el historial");
        System.out.println("2. Filtrar por tipo de operación");
        System.out.print("Seleccione una opción: ");

        int opcion = scanner.nextInt();
        scanner.nextLine();

        switch (opcion) {
            case 1:
                cuenta.mostrarHistorial();
                break;
            case 2:
                System.out.print("Ingrese el tipo (DEPOSITO, RETIRO, TRANSFERENCIA): ");
                String tipo = scanner.nextLine();
                cuenta.mostrarHistorialPorTipo(tipo);
                break;
        }
    }

    private static void mostrarResumenGastos() {
        Gestor<CategoriaGasto> gestorCat = cuenta.getGestorCategorias();
        if (gestorCat instanceof GestorCategorias) {
            ((GestorCategorias) gestorCat).mostrarResumenGastos();
        }
    }

    private static void cambiarPin() {
        System.out.print("Ingrese su PIN actual: ");
        String pinActual = scanner.nextLine();
        System.out.print("Ingrese su nuevo PIN (4 dígitos): ");
        String pinNuevo = scanner.nextLine();

        cuenta.getUsuario().cambiarPin(pinActual, pinNuevo);
    }

    private static void consultarSaldo() {
        System.out.print("Ingrese su PIN: ");
        String pin = scanner.nextLine();

        if (cuenta.autenticar(pin)) {
            System.out.println("Saldo actual: $" + cuenta.getSaldo());
        }
    }

}