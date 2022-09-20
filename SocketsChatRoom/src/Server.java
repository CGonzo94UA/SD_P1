
/*
 * Clase que implementa el lado del Servidor, contiene dos clases: Servidor y ClientHandler
 * El servidor establece la conexion a traves del socket y queda en espera aceptando conexiones, obtiene los inputs
 * procedentes de las peticiones realizadas por los clientes, crea los objetos ClientHandler y crea los hilos
 *
 */

import java.io.*;
import java.util.*;
import java.net.*;

// Server class
public class Server{

    //Clase especifica de Java para sockets de servidores
    private ServerSocket serverSocket;

    public Server(ServerSocket sSocket) {
        this.serverSocket = sSocket;
    }

    //Funcion para iniciar el servidor y que se mantenga escuchando para aceptar nuevos clientes
    public void startServer(){
        try {
            //Mientras no cerremos el socket del servidor, se aceptaran nuevos clientes
            while(!serverSocket.isClosed()) {
                Socket nClienteSocket = serverSocket.accept();
                System.out.println("Se ha conectado un nuevo cliente: " + nClienteSocket);

                // Se crea un ClientHandler para el nuevo cliente
                ClientHandler nClientHand = new ClientHandler(nClienteSocket);

                // Se crea nuevo hilo
                Thread t = new Thread(nClientHand);
                //Se inicia el hilo
                t.start();

            }
        }catch (IOException e){
            this.closeServer();
        }
    }

    public void closeServer() {
        try {
            if(serverSocket != null) {
                serverSocket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException{

        //Puerto de escucha
        String puerto="";

        try{

            if (args.length < 1) {
                System.out.println("Debe indicar el puerto de escucha del servidor");
                System.out.println("$./Servidor puerto_servidor");
                System.exit (1);
            }
            puerto = args[0];
            ServerSocket serverSocket = new ServerSocket(Integer.parseInt(puerto));
            Server servidor = new Server(serverSocket);
            System.out.println("Servidor escucha el puerto " + puerto);
            servidor.startServer();

        }catch(Exception e){
            System.out.println("Error: " + e.toString());
        }

    }
}

// ClientHandler class
class ClientHandler implements Runnable{

    // Vector de clientes activos. Se utiliza estructura vector porque es sincronizada y
    //solo un hilo puede acceder cada vez
    static Vector<ClientHandler> activeClients = new Vector<ClientHandler>();

    private String clientUsername;
    private Socket s;
    //Para leer mensajes
    private BufferedReader bReader;
    //Para enviar mensajes
    private BufferedWriter bWriter;


    // Constructor
    public ClientHandler(Socket s){
        try {
            this.s = s;

            //Como se quieren enviar mensajes de caracteres se usa StreamWriter
            this.bWriter = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));

            this.bReader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.clientUsername = bReader.readLine();

            ClientHandler.activeClients.add(this);
            this.broadcastMessage("SERVIDOR: "+ this.clientUsername + " se ha conectado al chat!");

        }catch(IOException e) {
            this.closeConnections(s, bReader, bWriter);
        }

    }

    /*
     * Se usa un hilo para recibir los mensajes y otro para seguir escuchando al chat, asi no
     * se para la app esperando la recepcion de mensajes
     */
    @Override
    public void run(){

        String mensaje;
        //Mientras el cliente siga conectado se sigue esperando mensajes
        while (s.isConnected()){

            try{

                //Se realiza aqui para evitar que la app este parada esperando
                mensaje = bReader.readLine();
                //Se envia el mensaje al resto de clientes
                this.broadcastMessage(mensaje);

            } catch (IOException e) {
                this.closeConnections(s, bReader, bWriter);
                //para salir del bucle
                break;
            }
        }

    }

    public void broadcastMessage(String mensaje) {

        //Se itera por todo el vector que almacena los clientes
        for(ClientHandler clientHand : activeClients){
            try{
                //Si el nombre de usuario del cliente es diferente al actual se envia mensaje, si no no
                if(!this.clientUsername.equals(clientHand.clientUsername)){
                    clientHand.bWriter.write(mensaje);
                    //el mensaje acaba con intro
                    clientHand.bWriter.newLine();
                    clientHand.bWriter.flush();
                }
            }catch(IOException e) {
                this.closeConnections(s, bReader, bWriter);
            }
        }
    }

    public void eliminaClientHandler(){
        ClientHandler.activeClients.remove(this);
        this.broadcastMessage("SERVIDOR: "+ this.clientUsername + " se ha desconectado del chat!");
    }

    public void closeConnections(Socket s, BufferedReader bReader, BufferedWriter bWriter) {
        this.eliminaClientHandler();
        try {
            if(s != null) {
                s.close();
            }

            if(bReader != null) {
                bReader.close();
            }

            if(bWriter != null) {
                bWriter.close();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

}
