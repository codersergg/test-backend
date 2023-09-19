package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse =
        withContext(Dispatchers.IO) {
            transaction {
                val query = BudgetTable
                    .leftJoin(
                        AuthorTable,
                        { authorId },
                        { AuthorTable.id }
                    )
                    .select { BudgetTable.year eq param.year }

                val total = query.count()

                val totalByType = query
                    .asSequence()
                    .filter {
                        if (param.name != null && it[BudgetTable.authorId] != null) {
                            it[AuthorTable.fullName].contains(
                                param.name, ignoreCase = true
                            )
                        } else param.name == null
                    }
                    .map { mapBudgetResponse(it) }
                    .sortedWith(compareBy(BudgetResponse::month).thenByDescending(BudgetResponse::amount))
                    .toList()


                val sumByType = totalByType
                    .groupBy { it.type.name }
                    .mapValues { it.value.sumOf { v -> v.amount } }

                val items = totalByType
                    .drop(param.offset)
                    .take(param.limit)

                return@transaction BudgetYearStatsResponse(
                    total = total,
                    totalByType = sumByType,
                    items = items
                )
            }
        }

    private fun mapBudgetResponse(it: ResultRow): BudgetResponse {
        return if (it[BudgetTable.authorId] != null) {
            BudgetResponseFullResponse(
                year = it[BudgetTable.year],
                month = it[BudgetTable.month],
                amount = it[BudgetTable.amount],
                type = it[BudgetTable.type],
                authorFullName = it[AuthorTable.fullName],
                dateCreation = LocalDateTime.ofEpochSecond(
                    it[AuthorTable.dateCreation], 0,
                    ZoneOffset.UTC
                ).toString()
            )
        } else
            BudgetResponse(
                year = it[BudgetTable.year],
                month = it[BudgetTable.month],
                amount = it[BudgetTable.amount],
                type = it[BudgetTable.type]
            )
    }
}