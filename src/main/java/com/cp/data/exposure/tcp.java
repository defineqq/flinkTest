package com.cp.data.exposure;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class tcp {

    public static void main(String[] args) throws UnknownHostException, IOException {
        // TODO Auto-generated method stub
        try {
            Socket socket=new Socket("127.0.0.1",49838);
            System.out.println("soucket");
            OutputStream outputStream=socket.getOutputStream();
            Scanner in = new Scanner(System.in);        //类似于声明，真正执行在下面。

            while (true) {

                String msg=in.nextLine();

                outputStream.write(msg.getBytes());

//                System.out.println("ͻ˷Ϣ");

                InputStream inputStream=socket.getInputStream();

                BufferedReader br=new BufferedReader(new InputStreamReader(inputStream));

                System.out.println(br.readLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());

            // TODO: handle exception
        }
    }

}