package Backend;

public interface ISimulable {
    String reaccionarACondiciones(CondicionAmbiental condicion);
    CicloSimulacion ejecutarCiclo(int numeroCiclo, CondicionAmbiental condicion);
}