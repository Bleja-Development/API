package com.makebleja.services

import jakarta.mail.Authenticator
import jakarta.mail.Message
import jakarta.mail.MessagingException
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import java.util.Properties

class EmailService(private val props: Properties) {
    private val username = props.getProperty("email.user")
    private val password = props.getProperty("email.password")

    fun sendOtpCode(toEmail: String, code: String) {
        val smtpProps = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
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