package helloworld;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.table.*;

/**
 * Handler for requests to Lambda function.
 */
public class App implements RequestHandler<Object, String> {
    public static int length;
    public String handleRequest(Object thing, Context context) {

        //hibernate connection
        SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
        org.hibernate.Session sss = sessionFactory.openSession();

        ArrayList<HibernateCoin> coins = new ArrayList<>();
        ArrayList<Double> targetPrices = new ArrayList<>();
        ArrayList<String> emailBody = new ArrayList<>();

        targetPrices.add(35000.0); targetPrices.add(2000.0); targetPrices.add(.8);
        targetPrices.add(220.0); targetPrices.add(.5); targetPrices.add(55.0);
        targetPrices.add(.8);

        int ct = 0;
        getLength();

        //iterates through SQL table to compare prices in DB with threshold values stored in arraylist
        for (int i = 0 ; i <length; i ++){

            HibernateCoin coin = sss.get(HibernateCoin.class, i+1);
            coins.add(coin);
            double price = Double.parseDouble(String.valueOf(coins.get(i).getPrice()));

            if (price > targetPrices.get(i)){

                ct ++;
                String name = coins.get(i).getName();
                String message = ("The price for "+name+" is now "+price+System.lineSeparator()+
                "This is above the notification threshold of "+targetPrices.get(i)+System.lineSeparator()
                +System.lineSeparator());
                emailBody.add(message);
            }
        }
        sss.close();
        sessionFactory.close();
        if (ct > 0) {
            String b1 = emailBody.toString();
            String b2 = b1.replace(",","")
                .replace("[","")
                .replace("]","");
            sendEmail(b2);
        }
        return null;
    }

    public void getLength(){
        String userName = "***";
        String passWord = "***";
        String sqlUrl = "jdbc:mysql://database-3.***.us-east-1.rds.amazonaws.com:3306/***";
        String query = "Select COUNT(*) FROM currency";

        try{
            Connection connection = DriverManager.getConnection(sqlUrl, userName, passWord);
            PreparedStatement statement =  connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            resultSet.next();
            length = resultSet.getInt(1);
            statement.close();
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendEmail(String body){
        String host="smtp.gmail.com";
        final String user="***@gmail.com";  //gmail address
//      this comes from gmail setttings
        final String password="***";

        String to="***.com"; //hotmail address

        //Get the session object
        Properties props = new Properties();
        props.put("mail.smtp.host",host);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug.auth", "true");


        javax.mail.Authenticator jma = new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user,password);
            }
        };
        Session session = Session.getDefaultInstance(props, jma);
        //Compose the message
        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.addRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject("aws program notice");
            message.setText(body);
            Transport.send(message);
            System.out.println("message sent successfully...");
        } catch (MessagingException e) {e.printStackTrace();}
    }
}




