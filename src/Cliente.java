/*
 * Clase que implementa los clientes del servidor. Se establece la conexion mediante sockets
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

// Client class
public class Cliente{
	
	public static void main(String[] args) throws IOException{
		try	{
			Scanner sc = new Scanner(System.in);
			
			String host;
			String puerto;
			if (args.length < 2) {
				System.out.println ("Debe indicar la direccion del servidor y el puerto");
				System.out.println ("$./Cliente nombre_servidor puerto_servidor");
				System.exit(-1);
			}
			host = args[0];
			puerto = args[1];
				
			// Se establece la conexion con el servidor y con el puerto indicado por el cliente
			Socket s = new Socket(host, Integer.parseInt(puerto));

	
			// obtaining input and out streams
			DataInputStream dataInput = new DataInputStream(s.getInputStream());
			DataOutputStream datOutput = new DataOutputStream(s.getOutputStream());
	
			//Intercambio de informacion entre Cliente y ClientHandler
			while (true){
				System.out.println(dataInput.readUTF());
				String tosend = sc.nextLine();
				datOutput.writeUTF(tosend);
				
				// Si cliente escribe Exit se cierra la conexion y se hace break para salir del bucle
				if(tosend.equals("Salir")){
					System.out.println("Cerrando conexion: " + s);
					s.close();
					System.out.println("Conexion terminada.");
					break;
				}
				
				//Se imprime solucion a operacion del cliente
				String recibida = dataInput.readUTF();
				System.out.println(recibida);
			}
			
			//Cierre de scanner y streams utilizados
			sc.close();
			dataInput.close();
			datOutput.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
