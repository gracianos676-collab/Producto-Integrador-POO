package PuntodVenta;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    private Repositorio<Producto> repoProductos;
    private Repositorio<Venta> repoVentas; 
    private Scanner scanner;
    private int contadorTickets = 1;

    public Main() {
        repoProductos = new Repositorio<>();
        repoVentas = new Repositorio<>(); 
        scanner = new Scanner(System.in);
        List<Producto> productosCargados = ManejadorArchivos.cargarProductosDesdeTxt("productos.txt");
        if(productosCargados.isEmpty()) {
            System.out.println("\n[Aviso] No se encontraron productos. Verifica que 'productos.txt' exista en la ruta correcta.");
        } else {
            for (Producto p : productosCargados) {
                repoProductos.agregar(p);
            }
            System.out.println("\n[Éxito] Se cargaron " + productosCargados.size() + " productos exitosamente.");
        }

        List<Venta> ventasGuardadas = ManejadorArchivos.cargarVentas();
        for (Venta v : ventasGuardadas) {
            repoVentas.agregar(v);
        }
        
        if (!ventasGuardadas.isEmpty()) {
            try {
                int ultimoId = Integer.parseInt(ventasGuardadas.get(ventasGuardadas.size() - 1).getId());
                this.contadorTickets = ultimoId + 1;
            } catch (NumberFormatException e) {
                this.contadorTickets = ventasGuardadas.size() + 1;
            }
        }
    }
    public void iniciar() {
        String opcion = "";
        do {
            System.out.println("\n--- Menu de Tienda de Abarrotes la Pequeña ---");
            System.out.println("1.- Productos");
            System.out.println("2.- Punto de Venta");
            System.out.println("3.- Inventario");
            System.out.println("4.- Historial de Ventas");
            System.out.println("5.- Salida");
            System.out.print("Que opcion deseas: ");
            opcion = scanner.nextLine();

            switch (opcion) {
                case "1": menuProductos(); break;
                case "2": menuPuntoVenta(); break;
                case "3": menuInventario(); break;
                case "4": consultarHistorialVentas(); break; 
                case "5": System.out.println("Saliendo del sistema..."); break;
                default: System.out.println("Opción incorrecta.");
            }
        } while (!opcion.equals("5"));
    }


    private void menuProductos() {
        String opcion = "";
        do {
            System.out.println("\n--- Opciones de Productos ---");
            System.out.println("1.- Modificar Precio");
            System.out.println("2.- Listado");
            System.out.println("3.- Salida");
            System.out.print("Que opcion deseas: ");
            opcion = scanner.nextLine();

            if (opcion.equals("1")) modificarPrecioProducto();
            else if (opcion.equals("2")) listarProductos();
        } while (!opcion.equals("3"));
    }

    private void modificarPrecioProducto() {
        listarProductos();
        System.out.print("Introduce el codigo del producto a modificar: ");
        String id = scanner.nextLine();
        Optional<Producto> optProd = repoProductos.obtenerPorId(id);
        if (optProd.isPresent()) {
            System.out.print("Introduce el nuevo precio: ");
            try {
                double nuevoPrecio = Double.parseDouble(scanner.nextLine());
                optProd.get().setPrecio(nuevoPrecio);
                System.out.println("Precio modificado exitosamente.");
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número válido.");
            }
        } else {
            System.out.println("No existe el codigo de producto.");
        }
    }

    private void listarProductos() {
        System.out.println("\nCód.  Producto                  Precio    Stock");
        System.out.println("-------------------------------------------------");
        repoProductos.obtenerTodos().forEach(System.out::println);
    }

    private void menuInventario() {
        String opcion = "";
        do {
            System.out.println("\n--- Gestión de Inventario ---");
            listarProductos();
            System.out.println("1.- Agregar Stock");
            System.out.println("2.- Quitar Stock");
            System.out.println("3.- Modificar Stock Total");
            System.out.println("4.- Eliminar Producto del sistema");
            System.out.println("5.- Volver al menú principal");
            System.out.print("Selecciona una opción: ");
            opcion = scanner.nextLine();

            if (opcion.equals("5")) break;

            System.out.print("Introduce el código del producto: ");
            String id = scanner.nextLine();
            Optional<Producto> optProd = repoProductos.obtenerPorId(id);

            if (optProd.isEmpty()) {
                System.out.println("Código no encontrado.");
                continue;
            }

            Producto p = optProd.get();
            try {
                switch (opcion) {
                    case "1":
                        System.out.print("Cantidad a agregar: ");
                        p.agregarStock(Integer.parseInt(scanner.nextLine()));
                        break;
                    case "2":
                        System.out.print("Cantidad a quitar: ");
                        p.reducirStock(Integer.parseInt(scanner.nextLine()));
                        break;
                    case "3":
                        System.out.print("Nuevo valor de stock total: ");
                        
                        p.setStock(Integer.parseInt(scanner.nextLine())); 
                        break;
                    case "4":
                        repoProductos.eliminarPorId(id);
                        System.out.println("Producto eliminado correctamente.");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes ingresar un número válido.");
            }
        } while (!opcion.equals("5"));
    }

    private void menuPuntoVenta() {
        List<ItemTicket> ticketActual = new ArrayList<>();
        String opcion = "";
        String idTicket = String.format("%03d", contadorTickets);
        
        do {
            System.out.println("\nTicket No " + idTicket);
            System.out.println("1.- Agregar Producto a Ticket");
            System.out.println("2.- Eliminar Producto del Ticket");
            System.out.println("3.- Listado de Productos en el Ticket");
            System.out.println("4.- Pagar");
            System.out.println("5.- Cancelar y Salir");
            System.out.print("Que opcion deseas: ");
            opcion = scanner.nextLine();

            try {
                switch (opcion) {
                    case "1": agregarATicket(ticketActual); break;
                    case "2": eliminarDeTicket(ticketActual); break;
                    case "3": mostrarTicket(ticketActual); break;
                    case "4": 
                        if (!ticketActual.isEmpty()) {
                            procesarPago(ticketActual, idTicket);
                            contadorTickets++;
                            opcion = "5"; 
                        } else {
                            System.out.println("El ticket está vacío.");
                        }
                        break;
                    case "5": 
                        devolverStock(ticketActual); 
                        System.out.println("Saliendo del ticket de venta.");
                        break;
                }
            } catch (ProductoNoEncontradoException | StockInsuficienteException e) {
                System.out.println(e.getMessage());
            }
        } while (!opcion.equals("5"));
    }

    private void agregarATicket(List<ItemTicket> ticket) throws ProductoNoEncontradoException, StockInsuficienteException {
        System.out.print("Introduce el codigo del producto: ");
        String id = scanner.nextLine();
        Producto prod = repoProductos.obtenerPorId(id)
            .orElseThrow(() -> new ProductoNoEncontradoException("El código no existe."));
            
        if (prod.getStock() <= 0) {
            throw new StockInsuficienteException("No hay stock suficiente.");
        }

        Optional<ItemTicket> itemExistente = ticket.stream()
            .filter(item -> item.getProducto().getId().equals(id))
            .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().incrementarCantidad(1);
        } else {
            ticket.add(new ItemTicket(prod, 1));
        }
        prod.reducirStock(1);
        System.out.println("Producto agregado.");
    }

    private void eliminarDeTicket(List<ItemTicket> ticket) {
        System.out.print("Introduce el codigo del producto a eliminar: ");
        String id = scanner.nextLine();
        boolean eliminado = ticket.removeIf(item -> {
            if (item.getProducto().getId().equals(id)) {
                item.getProducto().agregarStock(item.getCantidad());
                return true;
            }
            return false;
        });
        if (eliminado) System.out.println("Producto eliminado del ticket.");
        else System.out.println("El código no está en el ticket.");
    }

    private void devolverStock(List<ItemTicket> ticket) {
        for (ItemTicket item : ticket) {
            item.getProducto().agregarStock(item.getCantidad());
        }
    }

    private void mostrarTicket(List<ItemTicket> ticket) {
        if (ticket.isEmpty()) {
            System.out.println("Ticket vacío.");
            return;
        }
        for (ItemTicket item : ticket) {
            System.out.printf("%-20s Cant: %-3d Subtotal: $%.2f%n", 
                item.getProducto().getNombre(), item.getCantidad(), item.getSubtotal());
        }
    }

    private void procesarPago(List<ItemTicket> ticket, String idTicket) {
        double subtotal = ticket.stream().mapToDouble(ItemTicket::getSubtotal).sum();
        double iva = subtotal * 0.16; 
        double total = subtotal + iva;

        System.out.println("\n--- TICKET PAGADO #" + idTicket + " ---");
        mostrarTicket(ticket);
        System.out.printf("Subtotal: $%.2f%n", subtotal);
        System.out.printf("IVA 16%%:  $%.2f%n", iva);
        System.out.printf("Total:     $%.2f%n", total);
        
        String fechaActual = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        
        
        Venta nuevaVenta = new Venta(idTicket, fechaActual, subtotal, iva, total);
        repoVentas.agregar(nuevaVenta);
        ManejadorArchivos.guardarVenta(nuevaVenta);
        System.out.println("Venta registrada con éxito en el historial.");
    }

    private void consultarHistorialVentas() {
        System.out.println("\n--- HISTORIAL DE VENTAS REALIZADAS ---");
        List<Venta> listaVentas = repoVentas.obtenerTodos();
        
        if (listaVentas.isEmpty()) {
            System.out.println("No se han registrado ventas en esta sesión.");
            return;
        }

        System.out.println("----------------------------------------------------------------------");
        listaVentas.forEach(System.out::println);
        System.out.println("----------------------------------------------------------------------");
        
        double totalCaja = listaVentas.stream().mapToDouble(Venta::getTotal).sum();
        System.out.printf("TOTAL ACUMULADO EN CAJA: $%.2f%n", totalCaja);
    }

    public static void main(String[] args) {
        new Main().iniciar();
    }
}