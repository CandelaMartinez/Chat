package chat;

import java.awt.BorderLayout;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.swing.*;

public class Servidor {

	public static void main(String[] args) {
		MarcoServidor mimarco = new MarcoServidor();
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mimarco.setVisible(true);

	}

}

//........................................................ MARCO THREAD ..........................................................

class MarcoServidor extends JFrame implements Runnable {

	private JTextArea areatexto;

	public MarcoServidor() {

		setBounds(1200, 300, 280, 350);
		JPanel milamina = new JPanel();
		milamina.setLayout(new BorderLayout());

		areatexto = new JTextArea();
		milamina.add(areatexto, BorderLayout.CENTER);

		add(milamina);

		// creo el hilo y lo inicio
		Thread mihilo = new Thread(this);
		mihilo.start();

	}

	@Override
	public void run() {

		try {

			//........................................SOCKET SERVIDOR ABIERTO puerto 9999 ...............................................
			// construye un socket de servidor abierto en el puerto especificado
			ServerSocket servidor = new ServerSocket(9999);

			String nick, ip, mensaje;

			ArrayList<String> listaIp = new ArrayList<String>();

			PaqueteEnvio paqueteRecibido;

			while (true) {

				// acepta todas las conexiones
				Socket misocket = servidor.accept();

				// crear un flujo de datos de entrada de objetos
				ObjectInputStream paqueteDatos = new ObjectInputStream(misocket.getInputStream());

				// recibo paquete y lo leo
				paqueteRecibido = (PaqueteEnvio) paqueteDatos.readObject();

				nick = paqueteRecibido.getNick();
				ip = paqueteRecibido.getIp();
				mensaje = paqueteRecibido.getMensaje();

				// si el usuario ya se encuentra online ejecuta directamente este codigo
				if (!mensaje.equals("online")) {

					// poner toda esa informacion en el area de texto
					areatexto.append("\n" + nick + ": " + mensaje + " para " + ip);

					// reenviar la informacion recibida al destinatario por puerto 9090
					Socket enviaDestino = new Socket(ip, 9090);

					ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestino.getOutputStream());

					paqueteReenvio.writeObject(paqueteRecibido);

					paqueteReenvio.close();

					enviaDestino.close();

					misocket.close();

				} else {

					//..................................... DETECTA ONLINE ......................................................
					
					// getInnetAddress de la clase Socket devuelve un objeto de la clase InnetAddress
					InetAddress localizacion = misocket.getInetAddress();
					
					// crear String para sacarle a localizacion el address
					String ipRemota = localizacion.getHostAddress();
					
					System.out.println("***Online: " + ipRemota);
					
					//..............................................LLENAR ARRAYLIST CON IP ONLINE ...................................................

					
					listaIp.add(ipRemota);
					
					for (String string : listaIp) {
						System.out.println("****************"+string);
					}

					//...........................................ENVIAR A LOS CLIENTES EL ARRAYLIST ...................................................
					// meter este arrayList en el paquete que envio
					
					paqueteRecibido.setIps(listaIp);

					// enviarle a cada uno de los clientes que se vayan conectando el arrayList

					for (String s : listaIp) {

						Socket enviaDestino = new Socket(s, 9090);

						ObjectOutputStream paqueteReenvio = new ObjectOutputStream(enviaDestino.getOutputStream());

						paqueteReenvio.writeObject(listaIp);

						paqueteReenvio.close();

						enviaDestino.close();

						misocket.close();

					}

				}

			}

		} catch (IOException | ClassNotFoundException e) {

			e.printStackTrace();
		}

	}
}
