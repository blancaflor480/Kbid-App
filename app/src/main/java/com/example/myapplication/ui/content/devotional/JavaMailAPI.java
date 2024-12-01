package com.example.myapplication.ui.content.devotional;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class JavaMailAPI extends AsyncTask<Void, Void, Void> {
    private Context context;
    private String email;
    private String subject;
    private String message;

    public JavaMailAPI(Context context, String email, String subject, String message) {
        this.context = context;
        this.email = email;
        this.subject = subject;
        this.message = message;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Set up properties for the SMTP server
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com"); // Your SMTP server
        properties.put("mail.smtp.port", "587"); // Port for TLS
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true"); // Enable TLS

        // Create a session with an authenticator
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("kidsbiblediscovery@gmail.com", "bbtzbeesfcoskord"); // Your email and password
            }
        });

        try {
            // Create a MimeMessage
            Message mimeMessage = new MimeMessage(session);
            mimeMessage.setFrom(new InternetAddress("kidsbiblediscovery@gmail.com")); // Use the same email for 'From'
            mimeMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email)); // Recipient email
            mimeMessage.setSubject(subject); // Email subject
            mimeMessage.setContent(message, "text/html; charset=utf-8"); // Email body

            // Send the email
            Transport.send(mimeMessage);
            Log.d("JavaMailAPI", "Email sent successfully to " + email);
        } catch (MessagingException e) {
            Log.e("JavaMailAPI", "Error sending email", e);
            // Optionally notify the user of the error
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Handle any UI updates after sending the email, if necessary
    }
}
