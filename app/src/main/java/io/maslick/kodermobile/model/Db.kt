package io.maslick.kodermobile.model

import android.arch.persistence.room.*
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: Item)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(users: List<Item>)
}