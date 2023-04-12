package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mobi.sevenwinds.app.author.AuthorEntity
import mobi.sevenwinds.app.author.AuthorTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecordRequest): BudgetRecordResponse = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.author = body.authorId?.let { AuthorEntity.findById(it) }//should be in 3.3 commit
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {

         val query: org.jetbrains.exposed.sql.Query = if (param.authorName == null){
                (BudgetTable leftJoin      AuthorTable)
                    .select {( BudgetTable.year eq param.year) and (AuthorTable.full_name.isNull() )}
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
            }else{
                (BudgetTable innerJoin     AuthorTable)
                    .select {( BudgetTable.year eq param.year) and (AuthorTable.full_name eq param.authorName )}
                    .orderBy(BudgetTable.month to SortOrder.ASC, BudgetTable.amount to SortOrder.DESC)
            }

            val queryWithLimits = query.limit(param.limit, param.offset)

            val total = query.count()
            val data = BudgetEntity.wrapRows(queryWithLimits).map { it.toResponse() }
            val dataTotal = BudgetEntity.wrapRows(queryWithLimits).map { it.toResponse() }

            val sumByType = dataTotal.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = data
            )
        }
    }
}