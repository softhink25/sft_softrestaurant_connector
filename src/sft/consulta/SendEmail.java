package sft.consulta;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.json.JSONObject;

public class SendEmail {

    private HashMap<String, String> lstCadenas = null;
    private String vTemplate = null;
    private Properties props;

    public SendEmail(InputStream input) throws Exception {

        try {
            this.props = new Properties();

            props.load(input);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Verifica que exista tu archivo correo.properties", ex);
        } catch (IOException ex) {
            Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, null, ex);
            throw new Exception("Error al cargar el archivo Properties.", ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(SendEmail.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void EnviarEmail(String sMensaje,
            String sTitulo,
            String sTo,
            String sFrom) throws FileNotFoundException, IOException, AddressException, MessagingException {

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
            }
        });

        MimeMessage message = new MimeMessage(session);

        message.setFrom(new InternetAddress(sFrom, sFrom));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(sTo));
        message.setSubject(sTitulo, "ISO-8859-1");
        message.setContent(sMensaje, "text/html;charset= " + "ISO-8859-1");
        String languages[] = new String[1];
        languages[0] = "es_MX";
        message.setContentLanguage(languages);
        //System.out.println("Antes");
        Transport.send(message);
        //System.out.println("Despues");
    }

    public void EnviarEmail(String sMensaje, String sTitulo, String sTo) throws FileNotFoundException, IOException, AddressException, MessagingException {
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(props.getProperty("mail.from"), props.getProperty("mail.from")));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(sTo));
        message.setSubject(sTitulo, "ISO-8859-1");
        message.setContent(sMensaje, "text/html; " + "ISO-8859-1");
        String languages[] = new String[1];
        languages[0] = "es_MX";
        message.setContentLanguage(languages);
        //System.out.println("Antes");
        Transport.send(message);
        //System.out.println("Despues");
    }

    public void EnviarEmail(Exception e, JSONObject pro) throws FileNotFoundException, IOException, AddressException, MessagingException {
        String sMensaje = "";
        if (pro != null) {
            sMensaje += "\n Procesando:\n" + pro != null ? "" : pro.toString();
        } else {
            sMensaje += "\n Procesando json nulo:\n";
        }
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(props.getProperty("mail.smtp.user"), props.getProperty("mail.smtp.password"));
            }
        });
        sMensaje += "StackTrace:" + Arrays.toString(e.getStackTrace());

        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(props.getProperty("mail.from"), props.getProperty("mail.from"), "ISO-8859-1"));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress("gustavo@sft.com.mx"));
        message.setSubject("Error de procesamiento en servicios Robot Soriana ", "ISO-8859-1");
        message.setContent(sMensaje, "text/html; " + "ISO-8859-1");
        message.setText(sMensaje, "ISO-8859-1");
        String languages[] = new String[1];
        languages[0] = "es_MX";
        message.setContentLanguage(languages);
        //System.out.println("Antes");

        Transport.send(message);
        //System.out.println("Despues");
    }

    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            
        } finally {
            try {
                is.close();
            } catch (IOException e) {
            }
        }
        return sb.toString();
    }

    public String getvTemplate() {
        return vTemplate;
    }

    public void setvTemplate(String vTemplate) {
        this.vTemplate = vTemplate;
    }

    public HashMap<String, String> getLstCadenas() {
        return lstCadenas;
    }

    public void setLstCadenas(HashMap<String, String> lstCadenas) {
        this.lstCadenas = lstCadenas;
    }

}
