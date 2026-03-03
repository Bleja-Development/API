import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object OtpCodes : Table("otp_codes"){
    val email = varchar("email", 50)
    val code = varchar("code", 6)
    val expiresAt = timestamp("expires_at")
    val attempts = integer("attempts")
}