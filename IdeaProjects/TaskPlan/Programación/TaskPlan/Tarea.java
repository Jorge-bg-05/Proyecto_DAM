import java.util.*;
import java.util.concurrent.TimeUnit;
import java.text.*;

interface MetodoDeTrabajo{
    void aplicarMetodo();
}

public class Tarea{
    private String asignatura;
    private String nombreTarea;
    private Dificultad dificultad;
    private Date fechaEntrega;

    public enum Dificultad {
        ALTA,
        MEDIA,
        BAJA
    }

    public Tarea(String asignatura, String nombreTarea, String dificultad, Date fechaEntrega) {
        if(asignatura.length() > 100)
            this.asignatura = asignatura.substring(0, 100);
        else
            this.asignatura = asignatura;
        if(nombreTarea.length() > 100)
            this.nombreTarea = nombreTarea.substring(0, 100);
        else
            this.nombreTarea = nombreTarea;
        if(dificultad.length() > 100) {
            String entrada = dificultad.substring(0, 100);
            this.dificultad = Dificultad.valueOf(entrada.toUpperCase());
        }else
            this.dificultad = Dificultad.valueOf(dificultad.toUpperCase());
        this.fechaEntrega = fechaEntrega;
    }

    public String getAsignatura() {
        return asignatura;
    }

    public void setAsignatura(String asignatura) {
        this.asignatura = asignatura;
    }

    public String getNombreTarea() {
        return nombreTarea;
    }

    public void setNombreTarea(String nombreTarea) {
        this.nombreTarea = nombreTarea;
    }

    public Dificultad getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = Dificultad.valueOf(dificultad.toUpperCase());
    }

    public Date getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(Date fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }
}

class Deberes extends Tarea implements MetodoDeTrabajo{
    private int cantidadEjercicios;
    public Deberes(String asignatura, String nombreTarea, String dificultad, Date fechaEntrega, int cantidadEjercicios) {
        super(asignatura, nombreTarea, dificultad, fechaEntrega);
        this.cantidadEjercicios = cantidadEjercicios;
    }

    public int getCantidadEjercicios(){
        return cantidadEjercicios;
    }
    public void setCantidadEjercicios(int cantidadEjercicios){
        this.cantidadEjercicios = cantidadEjercicios;
    }

    @Override
    public void aplicarMetodo(){
        metodoTimeBlocking();
    }

    public void metodoTimeBlocking(){
        Date hoy = new Date();
        long diferenciaMilis = getFechaEntrega().getTime() - hoy.getTime();
        long diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenciaMilis);

        if(diasRestantes <= 0) {
            System.out.println("¡¡¡Atención!!!: la fecha de entrega es hoy o ya ha pasado, así que, ¡¡¡A TRABAJAR COMO NUNCA!!!");
            return;
        }
        String fechaEntrega = new SimpleDateFormat("dd/MM/yyyy").format(getFechaEntrega());
        double ejerciciosPorDia = (double) cantidadEjercicios / diasRestantes;
        System.out.printf("%nPara el deber '%s' de la asignatura '%s', con %d ejercicios, debes hacer aproximadamente %.1f ejercicios por día para cumplir con la fecha de entrega: %s.%n", getNombreTarea(), getAsignatura(), cantidadEjercicios, ejerciciosPorDia,fechaEntrega);
    }
}

class Examenes extends Tarea implements MetodoDeTrabajo{
    private int puntosExamen;
    public Examenes(String asignatura, String nombreTarea, String dificultad, Date fechaEntrega, int puntosExamen) {
        super(asignatura, nombreTarea, dificultad, fechaEntrega);
        this.puntosExamen = puntosExamen;
    }

    public int getPuntosExamen(){
        return puntosExamen;
    }

    public void getPuntosExamen(int puntosExamen){
        this.puntosExamen = puntosExamen;
    }

    @Override
    public void aplicarMetodo(){
        metodoPomodoro();
    }
    public void metodoPomodoro(){
        System.out.println("Método Pomodoro: Ideal para repasar teoría y mantener la concentración");
        System.out.println("Instrucciones:" +
                "\n1.- Estudia en intervalos de 25 minutos de concentración total." +
                "\n2.- Toma descansos de 5 minutos entre cada sesión." +
                "\n3.- Después de 4 ciclos de 25 minutos, toma un descanso largo de entre 15 y 30 minutos.");

        Date hoy = new Date();
        long diferenciaMilis = getFechaEntrega().getTime() - hoy.getTime();
        long diasRestantes = TimeUnit.MILLISECONDS.toDays(diferenciaMilis);

        if(diasRestantes <= 0)
            System.out.println("¡¡¡Atención!!!: el exámen es hoy o ya ha pasado, así que repasa todo lo que puedas para hacerlo o para hacer la recuperación");
        else{
            double puntosPorDia = (double) puntosExamen / diasRestantes;
            System.out.printf("%nQuedan %d días para el exámen.%n", diasRestantes);
            System.out.printf("Recomendación: Estudia mas o menos %.1f puntos por día.%n", puntosPorDia);
        }
    }
}

