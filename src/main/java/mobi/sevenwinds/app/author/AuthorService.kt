package mobi.sevenwinds.app.author

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

object AuthorService {
    suspend fun addRecord(body: AuthorRecord): AuthorResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = AuthorEntity.new {
                this.fullName = body.fullName
                this.dateCreation = Instant.now().epochSecond
            }

            return@transaction entity.toResponse()
        }
    }
}