/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servidor;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 *
 * @author Luis
 */
public class Servidor {

    public static final int MAX_SIZE = 10000;
    
    private DatagramSocket socket;
    private DatagramPacket packet; //para receber os pedidos e enviar as respostas
    
    public static void main(String[] args) {

    }
    
}
