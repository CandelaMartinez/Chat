package chat;

import java.awt.event.ActionEvent;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.*;
import java.net.*;
import java.util.ArrayList;

public class Cliente {

	public static void main(String[] args) {
		MarcoCliente mimarco = new MarcoCliente();
		mimarco.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

}

//............................................... MARCO .....................................................................
class MarcoCliente extends JFrame {
	public MarcoCliente() {

		setBounds(600, 300, 280, 350);

		LaminaMarcoCliente milamina = new LaminaMarcoCliente();
		add(milamina);
		setVisible(true);

		// que se ejecute el metodo que salta cuando se abre la ventana
		addWindowListener(new EnvioOnline());

	}
}

//.............................................. AVISO ONLINE ...............................................................

class EnvioOnline extends WindowAdapter {

	// crear un socket que envie la informacion de estar conectado al abrir el frame
	
	public void windowOpened(WindowEvent e) {

		try {
			
			//puerto del servidor que esta a la escucha
			Socket misocket = new Socket("192.168.1.46", 9999);

			// paquete que reciba el servidor, reuso la clase
			PaqueteEnvio datos = new PaqueteEnvio();

			// agrego a la variable encapsulada mensaje, del objeto paqueteEnvio
			datos.setMensaje("online");
			
		

			ObjectOutputStream paqueteDatos = new ObjectOutputStream(misocket.getOutputStream());

			paqueteDatos.writeObject(datos);
			
			paqueteDatos.close();

			misocket.close();

		} catch (Exception e2) {

			System.out.println(e2.getMessage());
		}

	}

}

//................................................. LAMINA HILO ...................................................................
//clase a la escucha con un hilo que se este ejecutando siempre, para poder recibir del servidor los mensajes

class LaminaMarcoCliente extends JPanel implements Runnable {

	private JTextField campo1;
	private JLabel nickC;
	private JButton miBoton;
	private JComboBox<String> ipC;
	private JTextArea campoChat;

	public LaminaMarcoCliente() {

		// pregunto al usuario su nick y lo almaceno en String
		String nickUsuario = JOptionPane.showInputDialog("Nick: ");

		// label nick
		JLabel nNick = new JLabel("Nick: ");
		add(nNick);

		// label llenada por usuario con su nick
		nickC = new JLabel();
		nickC.setText(nickUsuario);
		add(nickC);

		// titulo
		JLabel texto = new JLabel("online:");
		add(texto);

		// desplegable que tendran las direcciones ip de los usuarios que se iran
		// conectando
		ipC = new JComboBox();

		ipC.addItem("192.168.1.46");
		ipC.addItem("192.168.1.45");

		add(ipC);

		// area de texto donde aparece la conversacion
		campoChat = new JTextArea(12, 20);
		add(campoChat);

		campo1 = new JTextField(20);
		add(campo1);

		miBoton = new JButton("enviar");

		Enviatexto mievento = new Enviatexto();
		miBoton.addActionListener(mievento);

		add(miBoton);

		// pongo en funcionamiento el hilo: this porque es la propia clase la que tiene el hilo
		Thread mihilo = new Thread(this);
		mihilo.start();

	}

	// .................................................EVENTO DE BOTON ENVIAR ........................................................
	private class Enviatexto implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {

			// para que aparezca lo que escribo en el cliente
			campoChat.append("\n" + campo1.getText());

			try {
				// IP servidor y puerto
				Socket misocket = new Socket("192.168.1.46", 9999);

				PaqueteEnvio datos = new PaqueteEnvio();

				datos.setNick(nickC.getText());
				datos.setIp(ipC.getSelectedItem().toString());
				datos.setMensaje(campo1.getText());

				// flujo de datos de salida hacia el servidor
				ObjectOutputStream paqueteDatos = new ObjectOutputStream(misocket.getOutputStream());

				paqueteDatos.writeObject(datos);

				misocket.close();

			} catch (UnknownHostException e1) {

				e1.printStackTrace();

			} catch (IOException e1) {

				System.out.println(e1.getMessage());
				e1.printStackTrace();
			}

		}

	}

	@Override
	public void run() {

		try {

			// .......................................PONER A LA ESCUCHA DE LO QUE ENVIE EL SERVIDOR ..............................................
			// poner a la escucha a la app por el puerto 9099
			ServerSocket servidor_cliente = new ServerSocket(9090);

			// creamos un socket por el que va a recibir el paquete
			Socket cliente;

			// variable paquetenenvio que almacene el paquete recibido
			PaqueteEnvio paqueteRecibido;

			while (true) {

				//socket tiene que aceptar las conexiones del exterior
				cliente = servidor_cliente.accept();

				// crear un flujo de datos de entrada capaz de transportar objetos
				ObjectInputStream flujoEntrada = new ObjectInputStream(cliente.getInputStream());

				// leer lo que hay en su interior, cast porque readObject me devuelve un Object
				paqueteRecibido = (PaqueteEnvio) flujoEntrada.readObject();
				
				
			
				//................................CONTROLO QUE SE EJECUTA CUANDO SE CONECTA .........................................

				
				if (!paqueteRecibido.getMensaje().equals("online")) {

					// escribirla en el area de texto
					campoChat.append("\n" + paqueteRecibido.getNick() + ": " + paqueteRecibido.getMensaje());
					
					
					

				} else {

					
					//campoChat.append("\n"+ paqueteRecibido.getIps().toString());
					
					ArrayList<String> IpsMenu = new ArrayList<String>();
					IpsMenu = paqueteRecibido.getIps();

					// vacio el JComboBax
					ipC.removeAllItems();

					for (String s : IpsMenu) {

						System.out.println("cada elemento del array"+s);
						ipC.addItem(s);

					}

					
				
				}
			}

		} catch (Exception e) {

			System.out.println(e.getMessage());
		}

	}

}

//....................................EMPAQUETA/SERIALIZA  ..............................................................................

class PaqueteEnvio implements Serializable {
	

	private String nick, ip, mensaje;

	private ArrayList<String> ips;

	public ArrayList<String> getIps() {
		return ips;
	}

	public void setIps(ArrayList<String> ips) {
		this.ips = ips;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMensaje() {
		return mensaje;
	}

	public void setMensaje(String mensaje) {
		this.mensaje = mensaje;
	}

}
