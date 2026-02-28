package com.makebleja.services


import org.jetbrains.exposed.v1.core.less
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.Instant

class OtpCleanupService {
    fun cleanOtpCodes() = transaction {
        OtpCodes.deleteWhere{ expiresAt less Instant.now() }
    }
}