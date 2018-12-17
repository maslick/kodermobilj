package io.maslick.kodermobile.mvp.listItems

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.kennyc.bottomsheet.BottomSheet
import com.kennyc.bottomsheet.BottomSheetListener
import io.maslick.kodermobile.R
import io.maslick.kodermobile.di.Item
import io.maslick.kodermobile.di.Properties.EDIT_ITEM_ID
import io.maslick.kodermobile.helper.Helper.showSnackBar
import io.maslick.kodermobile.mvp.addEditItem.AddEditItemActivity
import org.koin.android.ext.android.inject
import org.koin.android.ext.android.setProperty

class ItemsFragment : Fragment(), ItemsContract.View {

    override val presenter by inject<ItemsContract.Presenter>()
    override var isActive: Boolean = false
        get() = isAdded

    private val lineAdapter = LineAdapter(object : ItemListener {
        override fun onShowItem(item: Item) { presenter.openItemDetail(item)}
        override fun onEditItem(item: Item) { presenter.editItem(item)}
        override fun onDeleteItem(item: Item) { presenter.removeItem(item)}
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter.view = this
        presenter.start()
    }

    override fun onPause() {
        presenter.stop()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        presenter.result(requestCode, resultCode)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val root = inflater.inflate(R.layout.items_frag, container, false)

        with(root) {
            findViewById<RecyclerView>(R.id.recyclerFragment).apply {
                layoutManager = LinearLayoutManager(activity)
                addItemDecoration(DividerItemDecoration(activity, DividerItemDecoration.VERTICAL))
                adapter = lineAdapter
            }

            findViewById<SwipeRefreshLayout>(R.id.swiper).apply {
                setOnRefreshListener { presenter.loadItems() }
            }

            activity!!.findViewById<FloatingActionButton>(R.id.fab_add_item).apply {
                setImageResource(R.drawable.ic_add)
                setOnClickListener { presenter.addNewItem() }
            }
        }
        return root
    }

    override fun showItems(items: List<Item>) {
        lineAdapter.items = items.sortedByDescending { it.id }
    }

    override fun showLoadingItemsError() {
        view?.showSnackBar("Error while loading items", Snackbar.LENGTH_LONG)
    }

    override fun showNoItems() {
        view?.showSnackBar("No items...", Snackbar.LENGTH_LONG)
    }

    class LineAdapter(val callbacks: ItemListener) : RecyclerView.Adapter<LineHolder>() {
        var items = listOf<Item>()
            set(items) {
                field = items
                notifyDataSetChanged()
            }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LineHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_item, parent, false)
            return LineHolder(view)
        }

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: LineHolder, position: Int) {
            holder.bind(items[position], callbacks)
        }
    }

    class LineHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.textTitle)
        val description: TextView = itemView.findViewById(R.id.textDescription)
        val code: TextView = itemView.findViewById(R.id.textCode)
        val quantity: TextView = itemView.findViewById(R.id.textQuantity)

        @SuppressLint("SetTextI18n")
        fun bind(item: Item, callbacks: ItemListener) {
            quantity.text = item.quantity.toString()
            title.text = item.title
            description.text = item.description
            code.text = item.barcode
            itemView.setOnClickListener { callbacks.onShowItem(item) }
            itemView.setOnLongClickListener {
                BottomSheet.Builder(itemView.context)
                    .setSheet(R.menu.dropup_menu)
                    .setListener(object : BottomSheetListener {
                        override fun onSheetItemSelected(sheet: BottomSheet, menuItem: MenuItem, obj: Any?) {
                            when (menuItem.title) {
                                "Show" -> callbacks.onShowItem(item)
                                "Edit" -> callbacks.onEditItem(item)
                                "Remove" -> callbacks.onDeleteItem(item)
                            }
                        }
                        override fun onSheetDismissed(p0: BottomSheet, p1: Any?, p2: Int) {}
                        override fun onSheetShown(p0: BottomSheet, p1: Any?) {}
                    })
                    .show()
                true
            }
        }
    }

    override fun showAddItem() {
        val intent = Intent(context, AddEditItemActivity::class.java)
        setProperty(EDIT_ITEM_ID, Item())
        startActivityForResult(intent, AddEditItemActivity.REQUEST_ADD_ITEM)
    }

    override fun showItem(item: Item) {
        val intent = Intent(context, AddEditItemActivity::class.java)
        setProperty(EDIT_ITEM_ID, item)
        startActivityForResult(intent, AddEditItemActivity.REQUEST_ADD_ITEM)
    }

    override fun setLoadingIndicator(active: Boolean) {
        view?.findViewById<SwipeRefreshLayout>(R.id.swiper)?.apply {
            this.isRefreshing = active
        }
    }

    override fun showItemDetailUi(item: Item) {
        // TODO
    }

    override fun showSuccessfullySavedItem() {
        view?.showSnackBar("Item saved", Snackbar.LENGTH_LONG)
        presenter.loadItems()
    }

    interface ItemListener {
        fun onShowItem(item: Item)
        fun onEditItem(item: Item)
        fun onDeleteItem(item: Item)
    }
}