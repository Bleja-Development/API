import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp

object Users : Table("users") {
    val id = javaUUID("id")
    val email = varchar("email", 50)
    val password = varchar("password", 100)
    val name = varchar("name", 30)
    val surname = varchar("surname", 30)
    val nickname = varchar("nickname", 20)
    val dateOfBirth = date("date_of_birth")
    val verified = bool("verified")
    val homeAddress = varchar("home_address", 60)
    val phoneNumber = varchar("phone_number", 20)
    val updatedAt = timestamp("updated_at")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}