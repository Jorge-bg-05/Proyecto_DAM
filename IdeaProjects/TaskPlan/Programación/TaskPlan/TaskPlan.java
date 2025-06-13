import java.io.*;
import java.text.*;
import java.util.*;


public class TaskPlan {
    private static Scanner sc = new Scanner(System.in);
    private static final String ARCHIVO_USUARIOS = "usuarios.dat";
    private static Hashtable<String, Usuarios> usuarios = new Hashtable<>();
    private static Usuarios usuarioActual;


    public static void main(String[] args) {
        cargarUsuarios();
        iniciarSesion();
        notificarTareasUrgentes();
        while (true)
            menu();
    }

    private static void menu() {
        System.out.println("\nOpciones:\n1. Crear tarea\n2. Listar tareas\n3. Eliminar tarea\n4. Ordenar por prioridad\n5. Filtrar por proximidad de entrega(próximos x dias)\n6. Tareas urgentes(próximos 3 días)\n7. Modo Estudio 100%\n8. Salir");
        int opcion = sc.nextInt();
        sc.nextLine();
        switch (opcion) {
            case 1:
                System.out.println("\nCreando tarea...");
                crearTarea();
                break;
            case 2:
                System.out.println("\nListando tareas...");
                listarTareas();
                break;
            case 3:
                System.out.println("\nEliminando tarea...");
                eliminarTarea();
                break;
            case 4:
                System.out.println("\nOrdenando tareas por prioridad...");
                ordenarPorPrioridad();
                break;
            case 5:
                System.out.println("\nFiltrando tareas por proximidad de entrega...");
                verTareasProximosDias();
                break;
            case 6:
                System.out.println("\nMostrando tareas urgentes...");
                notificarTareasUrgentes();
                break;
            case 7:
                System.out.println("\nIniciando modo de estudio 100%...");
                modoEstudio();
                break;
            case 8:
                System.out.println("Saliendo...");
                System.exit(0);
        }
    }

    private static void crearTarea() {
        String nombreTarea;
        String asignatura;
        Date fechaEntrega;
        String dificultad;

        try (RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "rw")) {
            System.out.println("Ingrese la dificultad de la tarea (Alta/Media/Baja):");
            dificultad = sc.nextLine().toLowerCase();
            while (!dificultad.equals("alta") && !dificultad.equals("media") && !dificultad.equals("baja")) {
                System.out.println("Error: Ingrese una dificultad válida (Alta/Media/Baja):");
                dificultad = sc.nextLine().toLowerCase();
            }
            System.out.println("Ingrese el nombre de la asignatura:");
            asignatura = sc.nextLine();
            System.out.println("Ingrese el nombre de la tarea:");
            nombreTarea = sc.nextLine();
            System.out.println("Ingrese la fecha de entrega (dd/MM/yyyy HH:mm):");
            String fecha;
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            sdf.setLenient(false);
            do {
                fecha = sc.nextLine();
                try {
                    fechaEntrega = sdf.parse(fecha);
                    if (fechaEntrega.before(new Date())) {
                        System.out.println("Error: La fecha debe ser actual o futura, el plazo de entrega ya ha acabado. Ingrese nuevamente:");
                        fechaEntrega = null;
                    }
                } catch (ParseException e) {
                    System.out.println("Error: Formato de fecha inválido. Use dd/MM/yyyy HH:mm");
                    fechaEntrega = null;
                }
            } while (fechaEntrega == null);

            raf.seek(raf.length());
            raf.writeUTF(asignatura);
            raf.writeUTF(nombreTarea);
            raf.writeUTF(dificultad);
            raf.writeUTF(sdf.format(fechaEntrega));

        } catch (IOException e) {
            System.err.println("Error al crear el archivo: " + e.getMessage());
        }
    }

    private static void listarTareas() {
        String nombreTarea;
        String asignatura;
        Date fechaEntrega;
        String dificultad;
        try (RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "r")) {
            while(raf.getFilePointer() < raf.length()){

                asignatura = raf.readUTF();
                nombreTarea = raf.readUTF();
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                sdf.setLenient(false);
                dificultad = raf.readUTF();
                fechaEntrega = sdf.parse(raf.readUTF());

                if (!asignatura.isEmpty()) {
                    System.out.println("Asignatura: " + asignatura);
                    System.out.println("Nombre de la tarea: " + nombreTarea);
                    System.out.println("Dificultad: " + dificultad);
                    System.out.println("Fecha de entrega: " + fechaEntrega);
                    System.out.println("--------------------");
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error al leer el archivo: " + e.getMessage());

        }
    }

    private static void eliminarTarea() {
        File originalFile = new File(usuarioActual.getArchivoTareas());
        File tempFile = new File(usuarioActual.getArchivoTareas().replace(".dat",".temp"));
        try(RandomAccessFile rafOriginal = new RandomAccessFile(originalFile, "r");
            RandomAccessFile rafTemp = new RandomAccessFile(tempFile, "rw");) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date fechaActual = new Date();
            boolean tareaEliminada = false;

            while (rafOriginal.getFilePointer() < rafOriginal.length()) {
                String asignatura = rafOriginal.readUTF();
                String nombreTarea = rafOriginal.readUTF();
                String dificultad = rafOriginal.readUTF();
                String fecha = rafOriginal.readUTF();
                Date fechaEntrega = sdf.parse(fecha);

                System.out.println("Tarea: " + nombreTarea);
                if (fechaEntrega.before(fechaActual)) {
                    System.out.println("Tarea eliminada: Fecha vencida\n");
                    tareaEliminada = true;
                    continue;
                }

                System.out.println("¿Ha completado esta tarea? (s/n):");
                String respuesta = sc.nextLine().toLowerCase();
                if (respuesta.equals("s")) {
                    System.out.println("Tarea eliminada: Completada\n");
                    tareaEliminada = true;
                    continue;
                }

                rafTemp.writeUTF(asignatura);
                rafTemp.writeUTF(nombreTarea);
                rafTemp.writeUTF(dificultad);
                rafTemp.writeUTF(sdf.format(fechaEntrega));
            }

            if (!originalFile.delete()) {
                throw new IOException("No se pudo eliminar el archivo original");
            }
            if (!tempFile.renameTo(originalFile)) {
                throw new IOException("No se pudo renombrar el archivo temporal");
            }

            if (!tareaEliminada) {
                System.out.println("No se encontraron tareas para eliminar");
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error al eliminar la tarea: " + e.getMessage());
        }

    }
    private static void ordenarPorPrioridad() {
        List<Tarea> tareas = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try (RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "r")) {
            while (raf.getFilePointer() < raf.length()) {
                String asignatura = raf.readUTF();
                String nombreTarea = raf.readUTF();
                String dificultad = raf.readUTF();
                String fechaStr = raf.readUTF();
                Date fechaEntrega = sdf.parse(fechaStr);

                Tarea tarea = new Tarea(asignatura, nombreTarea, dificultad, fechaEntrega);
                tareas.add(tarea);
            }

            tareas.sort((t1, t2) -> {
                long tiempoActual = new Date().getTime();
                long tiempo1 = t1.getFechaEntrega().getTime() - tiempoActual;
                long tiempo2 = t2.getFechaEntrega().getTime() - tiempoActual;

                int comparacionFecha = Long.compare(tiempo1, tiempo2);
                if (comparacionFecha != 0)
                    return comparacionFecha;

                return Integer.compare(
                        dificultadAPrioridad(t2.getDificultad()),
                        dificultadAPrioridad(t1.getDificultad())
                );
            });

            System.out.println("\n--- TAREAS ORDENADAS POR PRIORIDAD ---");
            for (Tarea tarea : tareas) {
                System.out.println("Asignatura: " + tarea.getAsignatura());
                System.out.println("Tarea: " + tarea.getNombreTarea());
                System.out.println("Dificultad: " + tarea.getDificultad());
                System.out.println("Fecha de entrega: " + sdf.format(tarea.getFechaEntrega()));
                System.out.println("--------------------------------------");
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error al leer las tareas: " + e.getMessage());
        }
    }

    private static int dificultadAPrioridad(Tarea.Dificultad d) {
        return switch (d) {
            case ALTA -> 3;
            case MEDIA -> 2;
            case BAJA -> 1;
        };
    }

    private static void verTareasProximosDias() {
        System.out.println("¿Cuántos días hacia adelante quieres ver las tareas?");
        int dias = sc.nextInt();
        sc.nextLine();

        Date fechaActual = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(fechaActual);
        cal.add(Calendar.DAY_OF_YEAR, dias);
        Date limite = cal.getTime();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        List<Tarea> tareasFiltradas = new ArrayList<>();

        try (RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "r")) {
            while (raf.getFilePointer() < raf.length()) {
                String asignatura = raf.readUTF();
                String nombreTarea = raf.readUTF();
                String dificultad = raf.readUTF();
                Date fechaEntrega = sdf.parse(raf.readUTF());

                if (!fechaEntrega.before(fechaActual) && !fechaEntrega.after(limite)) {
                    tareasFiltradas.add(new Tarea(asignatura, nombreTarea, dificultad, fechaEntrega));
                }
            }

            if (tareasFiltradas.isEmpty()) {
                System.out.println("No hay tareas en los próximos " + dias + " días.");
                return;
            }

            tareasFiltradas.sort((t1, t2) -> t1.getFechaEntrega().compareTo(t2.getFechaEntrega()));

            System.out.println("\n--- TAREAS EN LOS PRÓXIMOS " + dias + " DÍAS ---");
            for (Tarea tarea : tareasFiltradas) {
                System.out.println("Asignatura: " + tarea.getAsignatura());
                System.out.println("Tarea: " + tarea.getNombreTarea());
                System.out.println("Dificultad: " + tarea.getDificultad());
                System.out.println("Fecha de entrega: " + sdf.format(tarea.getFechaEntrega()));
                System.out.println("--------------------------------------");
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error al leer las tareas: " + e.getMessage());
        }
    }

    private static void notificarTareasUrgentes() {
        File archivo = new File(usuarioActual.getArchivoTareas());
        if (!archivo.exists()) {
            System.out.println("No hay tareas guardadas todavía.");
            return;
        }

        List<Tarea> tareasUrgentes = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Date ahora = new Date();

        try (RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "r")) {
            while (raf.getFilePointer() < raf.length()) {
                String asignatura = raf.readUTF();
                String nombreTarea = raf.readUTF();
                String dificultad = raf.readUTF();
                Date fechaEntrega = sdf.parse(raf.readUTF());

                long diferenciaMillis = fechaEntrega.getTime() - ahora.getTime();
                long diferenciaDias = diferenciaMillis / (1000 * 60 * 60 * 24);

                if (diferenciaDias >= 0 && diferenciaDias <= 3) {
                    tareasUrgentes.add(new Tarea(asignatura, nombreTarea, dificultad, fechaEntrega));
                }
            }

            if (tareasUrgentes.isEmpty()) {
                System.out.println("✅ No tienes tareas urgentes en los próximos 3 días.");
            } else {
                System.out.println("🚨 TAREAS URGENTES (menos de 3 días):");
                for (Tarea t : tareasUrgentes) {
                    System.out.println("Asignatura: " + t.getAsignatura());
                    System.out.println("Tarea: " + t.getNombreTarea());
                    System.out.println("Dificultad: " + t.getDificultad());
                    System.out.println("Fecha de entrega: " + sdf.format(t.getFechaEntrega()));
                    System.out.println("----------------------------------");
                }
            }

        } catch (IOException | ParseException e) {
            System.err.println("Error al leer tareas: " + e.getMessage());
        }
    }

    private static void modoEstudio(){
        List<MetodoDeTrabajo> tareasEstudio = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        try(RandomAccessFile raf = new RandomAccessFile(usuarioActual.getArchivoTareas(), "r")){
            while(raf.getFilePointer() < raf.length()){
                String asignatura = raf.readUTF();
                String nombreTarea = raf.readUTF();
                String dificultad = raf.readUTF();
                Date fechaEntrega = sdf.parse(raf.readUTF());

                if(nombreTarea.toLowerCase().contains("exámen") || nombreTarea.toLowerCase().contains("examen")){
                    System.out.printf("¿Cuantos puntos entran en el examen '%s'?%n", nombreTarea);
                    int puntos = sc.nextInt();
                    sc.nextLine();
                    tareasEstudio.add(new Examenes(asignatura,nombreTarea,dificultad,fechaEntrega,puntos));
                }else {
                    System.out.printf("¿Cuantos ejercicios tienes que hacer en la tarea '%s'?%n", nombreTarea);
                    int ejercicios = sc.nextInt();
                    sc.nextLine();
                    tareasEstudio.add(new Deberes(asignatura, nombreTarea, dificultad, fechaEntrega, ejercicios));
                }
            }

            if(tareasEstudio.isEmpty())
                System.out.println("No hay tareas a las que aplicar el modo estudio");
            else{
                System.out.println("\n--- MODO ESTUDIO 100% ---");
                for(MetodoDeTrabajo tarea : tareasEstudio){
                    tarea.aplicarMetodo();
                    System.out.println("------------------------------------------------------");
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error al procesar tareas de estudio: " + e.getMessage());
        }
    }

    private static void iniciarSesion() {
        System.out.print("Nombre de usuario: ");
        String nombre = sc.nextLine().toLowerCase(); // siempre en minúsculas

        if (usuarios.containsKey(nombre)) {
            System.out.print("Contraseña: ");
            String contra = sc.nextLine();

            Usuarios u = usuarios.get(nombre);
            if (u.getContra().equals(contra)) {
                usuarioActual = u;
                u.actualizarUltimoAcceso();
                System.out.println("✅ Sesión iniciada con éxito.");
                System.out.println("Último acceso: " + u.getUltimoAccesoFormateado());
            } else {
                System.out.println("❌ Contraseña incorrecta.");
                System.exit(0);
            }

        } else {
            System.out.print("Usuario nuevo. Crea una contraseña: ");
            String contra = sc.nextLine();
            usuarioActual = new Usuarios(nombre, contra);
            usuarios.put(nombre, usuarioActual);
            System.out.println("✅ Usuario registrado correctamente.");
        }

        guardarUsuarios();
    }

    //@SuppressWarnings("unchecked")
    private static void cargarUsuarios() {
        File f = new File(ARCHIVO_USUARIOS);
        if (!f.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            usuarios = (Hashtable<String, Usuarios>) ois.readObject();
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios: " + e.getMessage());
        }
    }

    private static void guardarUsuarios() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_USUARIOS))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            System.err.println("Error al guardar usuarios: " + e.getMessage());
        }
    }



}
