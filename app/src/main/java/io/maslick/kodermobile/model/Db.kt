package io.maslick.kodermobile.model

import android.arch.persistence.room.*
import io.reactivex.Maybe
import io.reactivex.Single

@Entity(tableName = "items")
data class Item(
    @PrimaryKey var id: Int? = null,
    var title: String? = null,
    var category: String? = null,
    var description: String? = null,
    var barcode: String? = null,
    var quantity: Int? = null
)

@Database(entities = [Item::class], version = 1)
abstract class ItemDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    fun getItems(): Single<List<Item>>

    @Query("SELECT * FROM items WHERE id == :id")
    fun getItemById(id: Int): Maybe<Item>

    @Query("SELECT * FROM items WHERE barcode == :barcode")
    fun getItemByBarcode(barcode: String): Maybe<Item>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Item>)

    @Query("DELETE FROM items")
    fun deleteAll()
}