package uk.co.nickhowes

import org.hibernate.ScrollableResults
import org.hibernate.internal.AbstractScrollableResults
import java.sql.ResultSet

object ResultSetInspector {
    private val resultSetField = AbstractScrollableResults::class.java.getPrivateField("resultSet")

    private fun Class<*>.getPrivateField(name: String): java.lang.reflect.Field =
        getDeclaredField(name).apply { isAccessible = true }

    /**
     * Returns the number of rows returned in memory, or -1 if
     * the result has a cursor.
     * Used to determine whether a query is using a cursor.
     * This is useful for debugging, but should not be used in production
     * because reflection can be slow.
     */
    fun getNonCursorRowCount(scrollable: ScrollableResults): Int {
        val rs = resultSetField.get(scrollable) as ResultSet
        return getNonCursorRowCount(rs)
    }

    fun getNonCursorRowCount(rs: ResultSet): Int {
        // These fields are specific to PgResultSet, but we can't reference that class directly
        val pgResultSetClass = rs.javaClass
        val rowsField = pgResultSetClass.getPrivateField("rows")
        val cursorField = pgResultSetClass.getPrivateField("cursor")
        val rows = rowsField.get(rs) as List<*>
        val cursor = cursorField.get(rs) 
        return if (cursor == null) rows.size else -1
    }
}