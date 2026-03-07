package com.makebleja.services

import com.typesafe.config.ConfigFactory
import io.ktor.server.config.HoconApplicationConfig
import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class EmailService() {
    private val config = HoconApplicationConfig(ConfigFactory.load())

    private val username = config.propertyOrNull("email.user")?.getString() ?: ""
    private val password = config.propertyOrNull("email.password")?.getString() ?: ""

    fun sendOtpCode(toEmail: String, code: String) {
        val smtpProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "false")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "465")
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.socketFactory.port", "465")
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
        }

        val session = Session.getInstance(smtpProps, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                subject = "Your MakeBleja Verification Code"
                setText("Your 6-digit verification code is: $code\n\nThis code expires in 5 minutes.")
            }

            Transport.send(message)
            println("INFO: Email sent successfully to $toEmail")
        } catch (e: MessagingException) {
            e.printStackTrace()
            throw Exception("Failed to send email: ${e.message}")
        }
    }
}