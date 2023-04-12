package mobi.sevenwinds.app.author

import kotlinx.coroutines.selects.select
import mobi.sevenwinds.app.budget.BudgetTable
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.select
import java.lang.reflect.TypeVariable

object AuthorTable : IntIdTable("author") {
    val full_name = text("full_name")
    val creationDate = date("creation_date")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable) {
    }


    var fullName by AuthorTable.full_name
    var creationDate by AuthorTable.creationDate

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullName)
    }
}