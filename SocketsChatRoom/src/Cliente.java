
/*
 * Clase que implementa los clientes del servidor. Se establece la conexion mediante sockets
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

// Client class
public class Cliente{

    private Socket socket;
    private BufferedReader bReader;
    private BufferedWriter bWriter;

    private String username;

    DateFormat fordate = new SimpleDateFormat("dd/MM/yyyy");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");

    // Constructor
    public Cliente(Socket socket, String username){
        try {
            this.socket = socket;

            //Como se quieren enviar mensajes de caracteres se usa StreamWriter
            this.bWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            this.bReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = username;

        }catch(IOException e) {
            this.closeConnections(socket, bReader, bWriter);
        }

    }

    public void enviaMensaje(){
        try{
            this.bWriter.write(this.username);
            this.bWriter.newLine();
            this.bWriter.flush();

            Scanner sc = new Scanner(System.in);

            while(this.socket.isConnected()) {
                StringBuilder trazaFechaHora = new StringBuilder();

                Date date = new Date();
                trazaFechaHora.append(fordate.format(date) + "\t");
                trazaFechaHora.append(fortime.format(date)+"  ||  ");
                this.bWriter.write(trazaFechaHora.toString());

                String mensaje = sc.nextLine();

                this.bWriter.write(this.username + ": "+ mensaje);
                this.bWriter.newLine();
                this.bWriter.flush();
            }

            sc.close();

        }catch(IOException e) {
            this.closeConnections(socket, bReader, bWriter);
        }
    }

    public void recibeMensajes(){
        //Se utiliza hilos para no tener bloqueada a la app

        new Thread(new Runnable() {
            @Override
            public void run() {
                String mensajeChat;

                while(socket.isConnected()) {
                    try {
                        mensajeChat = bReader.readLine();
                        System.out.println(mensajeChat);
                    }catch(IOException e) {
                        closeConnections(socket, bReader, bWriter);
                    }
                }
            }
        }).start();;
    }

    public void closeConnections(Socket socket, BufferedReader bReader, BufferedWriter bWriter){

        try {
            if(this.socket != null) {
                socket.close();
            }

            if(this.bReader != null) {
                bReader.close();
            }

            if(this.bWriter != null) {
                bWriter.close();
            }
        }catch(IOException e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) throws IOException{
        try	{

            String host;
            String puerto;
            if (args.length < 2) {
                System.out.println ("Debe indicar la direccion del servidor y el puerto");
                System.out.println ("$./Cliente nombre_servidor puerto_servidor");
                System.exit(-1);
            }
            host = args[0];
            puerto = args[1];

            Scanner sc = new Scanner(System.in);
            System.out.println("Introduce tu nombre de usuario: ");
            String username = sc.nextLine();

            // Se establece la conexion con el servidor y con el puerto indicado por el cliente
            Socket s = new Socket(host, Integer.parseInt(puerto));

            Cliente cliente = new Cliente(s, username);
            //Se ejecutan como hilos separados, por eso no se bloquean
            cliente.recibeMensajes();
            cliente.enviaMensaje();

            sc.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
