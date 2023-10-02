package com.android.walletscan.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.walletscan.R
import com.android.walletscan.documents.WalletHistInfoTemp
import com.android.walletscan.supporting.DateAndTime
import com.android.walletscan.supporting.WalletScanConstants
import java.util.Locale

class WalletDetailHistoryAdapter(
    private var con: Context,
    private var walletInfoTempList: MutableList<WalletHistInfoTemp>
) : RecyclerView.Adapter<WalletDetailHistoryAdapter.ItemViewHolder>(), Filterable {
    var filteredList = walletInfoTempList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.wallets_detail_history_single_row, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtDate: TextView = view.findViewById(R.id.txtDate)
        var txtStartBalance: TextView = view.findViewById(R.id.txtStartBalance)
        var txtAmount: TextView = view.findViewById(R.id.txtAmount)
        var txtTransType: TextView = view.findViewById(R.id.txtTransType)
        var txtPurpose: TextView = view.findViewById(R.id.txtPurpose)
        var txtEndBalance: TextView = view.findViewById(R.id.txtEndBalance)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val walletHistInfoTemp = filteredList[position]
        val walletHistInfo = walletHistInfoTemp.walletHistInfo
        holder.txtDate.text = "${DateAndTime.getDateTime(walletHistInfo.transactionDoneOn!!)}"
        if (walletHistInfo.transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            holder.txtAmount.text = ": Rs.${walletHistInfo.amtAdded}/-"
            holder.txtAmount.setTextColor(
                ContextCompat.getColor(
                    con, android.R.color.holo_green_dark
                )
            )
            holder.txtTransType.text = con.getString(R.string.amount_added)
        } else if (walletHistInfo.transactionType == WalletScanConstants.TRANSACTION_TYPE_DEDUCT) {
            holder.txtAmount.text = ": Rs.${walletHistInfo.amtDeducted}/-"
            holder.txtAmount.setTextColor(
                ContextCompat.getColor(
                    con, android.R.color.holo_red_dark
                )
            )
            holder.txtTransType.text = con.getString(R.string.amount_deducted)
        }
        holder.txtStartBalance.text =
            ": Rs.${WalletScanConstants.decFormat.format(walletHistInfoTemp.startingBalance)}/-"
        holder.txtEndBalance.text =
            ": Rs.${WalletScanConstants.decFormat.format(walletHistInfoTemp.endingBalance)}/-"
        holder.txtPurpose.text = ": ${walletHistInfo.purpose}"
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredList = if (charSearch.isEmpty()) {
                    walletInfoTempList
                } else {
                    val resultList = mutableListOf<WalletHistInfoTemp>()
                    walletInfoTempList.forEach {
                        if (it.walletHistInfo.purpose.lowercase(Locale.ROOT)
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
                filteredList = results?.values as MutableList<WalletHistInfoTemp>
                notifyDataSetChanged()
            }

        }
    }
}