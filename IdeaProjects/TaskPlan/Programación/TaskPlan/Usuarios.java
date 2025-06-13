import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Usuarios implements Serializable{
    private String nombre;
    private String archivoTareas;
    private String contra;
    private Date ultimoAcceso;

    public Usuarios(String nombre, String contra){
        this.nombre = nombre.toLowerCase();
        this.contra = contra;
        this.archivoTareas = generarNombreArchivo(this.nombre);
        this.ultimoAcceso = new Date();
    }

    private String generarNombreArchivo(String nombre) {
        return nombre + "_tareas.dat";
    }

    public String getNombre() {
        return nombre;
    }

    public String getArchivoTareas() {
        return archivoTareas;
    }

    public String getContra() {
        return contra;
    }

    public Date getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void actualizarUltimoAcceso() {
        this.ultimoAcceso = new Date();
    }

    public String getUltimoAccesoFormateado() {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ultimoAcceso);
    }
}
