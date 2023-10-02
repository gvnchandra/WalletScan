package com.android.walletscan.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.walletscan.R
import com.android.walletscan.activities.WalletHistoryActivity
import com.android.walletscan.documents.WalletInfo
import com.android.walletscan.supporting.WalletScanConstants
import java.util.*

class CreatedWalletsAdapter(var con: Context, private var walletInfoList: MutableList<WalletInfo>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    var filteredList = walletInfoList
    val TYPE_HEADER = 0
    val TYPE_ITEM = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER)
            HeaderViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.header_item, parent, false)
            )
        else
            ItemViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.created_wallets_single_row, parent, false)
            )
    }

    override fun getItemCount(): Int {
        return filteredList.size + 1
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtCategory: TextView = view.findViewById(R.id.txtCategory)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtWalletName: TextView = view.findViewById(R.id.txtName)
        var txtBalance: TextView = view.findViewById(R.id.txtBalance)
        var singleRow: LinearLayout = view.findViewById(R.id.singleRowLayout)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.txtCategory.text = con.getString(R.string.my_wallets)
        } else if (holder is ItemViewHolder) {
            val walletInfo = filteredList[position - 1]
            holder.txtWalletName.text = walletInfo.name
            holder.txtBalance.text = String.format("Balance: Rs.${WalletScanConstants.decFormat.format(walletInfo.currentBalance)}/-")
            holder.singleRow.setOnClickListener {
                val intent = Intent(con, WalletHistoryActivity::class.java)
                intent.putExtra(WalletScanConstants.INTENT_PARAM_WALLET_INFO, walletInfo)
                con.startActivity(intent)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredList = if (charSearch.isEmpty()) {
                    walletInfoList
                } else {
                    val resultList = mutableListOf<WalletInfo>()
                    walletInfoList.forEach {
                        if (it.name.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(it)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as MutableList<WalletInfo>
                notifyDataSetChanged()
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_HEADER
        return TYPE_ITEM
    }
}