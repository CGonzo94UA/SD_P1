
/*
 * Clase que implementa el lado del Servidor, contiene dos clases: Servidor y ClientHandler
 * El servidor establece la conexion a traves del socket y queda en espera aceptando conexiones, obtiene los inputs
 * procedentes de las peticiones realizadas por los clientes, crea los objetos ClientHandler y crea los hilos
 * 
 */

import java.io.*;
import java.text.*;
import java.util.*;
import java.net.*;

// Server class
public class Server{
	
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
			ServerSocket skServidor = new ServerSocket(Integer.parseInt(puerto));
		    System.out.println("Escucho el puerto " + puerto);
	
			/*
			* Se mantiene abierta comunicacion con cliente esperando a que se conecte
			*/	
			while(true){
				/*
				* Se espera un cliente que quiera conectarse
				*/
				Socket skCliente = skServidor.accept(); // Crea objeto
				System.out.println("Se ha conectado un nuevo cliente : " + skCliente);
				DataInputStream dataInput = new DataInputStream(skCliente.getInputStream());
				DataOutputStream dataOutput = new DataOutputStream(skCliente.getOutputStream());

		        Thread t = new ClientHandler(skCliente, dataInput, dataOutput);
		        t.start();
			}
		}catch(Exception e){
			System.out.println("Error: " + e.toString());
		}
		
	}
}

// ClientHandler class
class ClientHandler extends Thread{
	
	DateFormat fordate = new SimpleDateFormat("dd/MM/yyyy");
	DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
	final DataInputStream dis;
	final DataOutputStream dos;
	final Socket s;
	

	// Constructor
	public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos){
		this.s = s;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run(){
		
		String solicitud;
		StringBuilder respuesta = new StringBuilder();
		while (true){
			
			try {

				// Se solicita operacion a realizar
				dos.writeUTF("Por favor, introduzca la operacion a realizar..\n"+
							"Escriba Salir para cerrar comunicacion.");
				
				// Se recibe operacion introducida por cliente
				solicitud = dis.readUTF();
				
				if(solicitud.equals("Salir")){
					
					System.out.println("Cliente " + this.s + " solicita cerrar comunicacion y salir...");
					System.out.println("Cerrando conexion...");
					this.s.close();
					System.out.println("Conexion terminada.");
					break;
				}
				
				// creating Date object
				Date date = new Date();
				
				// Escribe resultado de la operacion solicitada por el cliente
				respuesta.append("El resultado de la operacion es: \n");
				respuesta.append(ClientHandler.eval(solicitud));
				
				//Se pone traza de fecha y hora de la operacion
				respuesta.append("\nOperacion realizada:\n");
				respuesta.append(fordate.format(date)+"\n");
				respuesta.append(fortime.format(date)+"\n");
				dos.writeUTF(respuesta.toString());
						
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try{
			// closing resources
			this.dis.close();
			this.dos.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	//Metodo importado de https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form
	public static double eval(final String str) {
		    return new Object() {
		        int pos = -1, ch;

		        void nextChar() {
		            ch = (++pos < str.length()) ? str.charAt(pos) : -1;
		        }

		        boolean eat(int charToEat) {
		            while (ch == ' ') nextChar();
		            if (ch == charToEat) {
		                nextChar();
		                return true;
		            }
		            return false;
		        }

		        double parse() {
		            nextChar();
		            double x = parseExpression();
		            if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char)ch);
		            return x;
		        }

		        // Grammar:
		        // expression = term | expression `+` term | expression `-` term
		        // term = factor | term `*` factor | term `/` factor
		        // factor = `+` factor | `-` factor | `(` expression `)`
		        //        | number | functionName factor | factor `^` factor

		        double parseExpression() {
		            double x = parseTerm();
		            for (;;) {
		                if      (eat('+')) x += parseTerm(); // addition
		                else if (eat('-')) x -= parseTerm(); // subtraction
		                else return x;
		            }
		        }

		        double parseTerm() {
		            double x = parseFactor();
		            for (;;) {
		                if      (eat('*')) x *= parseFactor(); // multiplication
		                else if (eat('/')) x /= parseFactor(); // division
		                else return x;
		            }
		        }

		        double parseFactor() {
		            if (eat('+')) return parseFactor(); // unary plus
		            if (eat('-')) return -parseFactor(); // unary minus

		            double x;
		            int startPos = this.pos;
		            if (eat('(')) { // parentheses
		                x = parseExpression();
		                eat(')');
		            } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
		                while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
		                x = Double.parseDouble(str.substring(startPos, this.pos));
		            } else if (ch >= 'a' && ch <= 'z') { // functions
		                while (ch >= 'a' && ch <= 'z') nextChar();
		                String func = str.substring(startPos, this.pos);
		                x = parseFactor();
		                if (func.equals("sqrt")) x = Math.sqrt(x);
		                else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
		                else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
		                else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
		                else throw new RuntimeException("Unknown function: " + func);
		            } else {
		                throw new RuntimeException("Unexpected: " + (char)ch);
		            }

		            if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

		            return x;
		        }
		    }.parse();
		}
}
