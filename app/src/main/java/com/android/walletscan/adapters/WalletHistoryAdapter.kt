package com.android.walletscan.adapters

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.walletscan.R
import com.android.walletscan.documents.WalletHistInfo
import com.android.walletscan.interfaces.TransactionEditDeleteListener
import com.android.walletscan.network.ConnectionManager
import com.android.walletscan.supporting.DateAndTime
import com.android.walletscan.supporting.WalletScanConstants
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class WalletHistoryAdapter(
    private var con: Context,
    private var walletInfoList: MutableList<WalletHistInfo>,
    private var listener: TransactionEditDeleteListener
) : RecyclerView.Adapter<WalletHistoryAdapter.ItemViewHolder>(), Filterable {
    var filteredList = walletInfoList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.wallets_history_single_row, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var txtDate: TextView = view.findViewById(R.id.txtDate)
        var txtAmount: TextView = view.findViewById(R.id.txtAmount)
        var txtTransType: TextView = view.findViewById(R.id.txtTransType)
        var txtPurpose: TextView = view.findViewById(R.id.txtPurpose)
        var imgToRetOrRec: ImageView = view.findViewById(R.id.imgToRetOrRec)
        var imgEdit: ImageView = view.findViewById(R.id.imgEdit)
        var imgDelete: ImageView = view.findViewById(R.id.imgDelete)
        var singleRow: LinearLayout = view.findViewById(R.id.singleRowLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val walletHistInfo = filteredList[position]
        holder.txtDate.text = "${DateAndTime.getDateTime(walletHistInfo.transactionDoneOn!!)}"
        holder.imgToRetOrRec.setOnClickListener {
            showRecoverOrReturnDialog(walletHistInfo)
        }
        if (walletHistInfo.transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            if (walletHistInfo.toRecoverOrReturn != null &&
                walletHistInfo.toRecoverOrReturn!!
            ) {
                holder.imgToRetOrRec.visibility = View.VISIBLE
                holder.imgToRetOrRec.setImageResource(R.drawable.arrow_out)
            } else {
                holder.imgToRetOrRec.visibility = View.GONE
            }
            holder.txtAmount.text = ": Rs.${walletHistInfo.amtAdded}/-"
            holder.txtAmount.setTextColor(
                ContextCompat.getColor(
                    con, android.R.color.holo_green_dark
                )
            )
            holder.txtTransType.text = con.getString(R.string.amount_added)
            holder.imgEdit.setOnClickListener {
                showEditDialog(walletHistInfo)
            }
        } else if (walletHistInfo.transactionType == WalletScanConstants.TRANSACTION_TYPE_DEDUCT) {
            if (walletHistInfo.toRecoverOrReturn != null &&
                walletHistInfo.toRecoverOrReturn!!
            ) {
                holder.imgToRetOrRec.visibility = View.VISIBLE
                holder.imgToRetOrRec.setImageResource(R.drawable.arrow_in)
            } else {
                holder.imgToRetOrRec.visibility = View.GONE
            }
            holder.txtAmount.text = ": Rs.${walletHistInfo.amtDeducted}/-"
            holder.txtAmount.setTextColor(
                ContextCompat.getColor(
                    con, android.R.color.holo_red_dark
                )
            )
            holder.txtTransType.text = con.getString(R.string.amount_deducted)
            holder.imgEdit.setOnClickListener {
                showEditDialog(walletHistInfo)
            }
        }
        holder.txtPurpose.text = ": ${walletHistInfo.purpose}"
        holder.imgDelete.setOnClickListener {
            listener.deleteTransaction(walletHistInfo)
        }
    }

    private fun showRecoverOrReturnDialog(walletHistInfo: WalletHistInfo) {
        val dialog = Dialog(con)
        val layout =
            LayoutInflater.from(con).inflate(R.layout.wallet_rec_ret_dialog, null, false)
        dialog.setContentView(layout)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
        val txtTitle: TextView = layout.findViewById(R.id.txtTitle)
        val btnYes: Button = layout.findViewById(R.id.btnYes)
        val btnCancel: Button = layout.findViewById(R.id.btnCancel)

        if (walletHistInfo.transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            txtTitle.text = "Money returned?"
        } else {
            txtTitle.text = "Money recovered?"
        }
        btnYes.setOnClickListener {
            if (ConnectionManager.isNetworkAvailable(con)) {
                listener.saveStatus(
                    walletHistInfo.walletId,
                    walletHistInfo.id,
                )
            } else {
                Toast.makeText(con, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun showEditDialog(walletHistInfo: WalletHistInfo) {
        val transactionType = walletHistInfo.transactionType
        val origPurpose = walletHistInfo.purpose
        var origAmt = 0.0

        val editDialog = Dialog(con)
        val histUpdateLayout =
            LayoutInflater.from(con).inflate(R.layout.wallet_hist_update_dialog, null, false)
        editDialog.setContentView(histUpdateLayout)
        editDialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        editDialog.show()
        val rel: RelativeLayout = histUpdateLayout.findViewById(R.id.rel)
        val tilAmount: TextInputLayout = histUpdateLayout.findViewById(R.id.tilAmount)
        val tilPurpose: TextInputLayout = histUpdateLayout.findViewById(R.id.tilPurpose)
        val txtAmount: TextView = histUpdateLayout.findViewById(R.id.txtAmt)
        val btnSave: Button = histUpdateLayout.findViewById(R.id.btnSave)
        val btnCancel: Button = histUpdateLayout.findViewById(R.id.btnCancel)
        val schToRetOrRec: SwitchCompat = histUpdateLayout.findViewById(R.id.schToRetOrRec)
        if ((walletHistInfo.isRecoveredOrReturned != null &&
                    walletHistInfo.isRecoveredOrReturned!!)
        ) {
            rel.visibility = View.GONE
        } else if (walletHistInfo.toRecoverOrReturn != null &&
            walletHistInfo.toRecoverOrReturn!!
        ) {
            schToRetOrRec.isChecked = true
        }
        val txtToRetOrRec: TextView = histUpdateLayout.findViewById(R.id.txtToRetOrRec)
        if (transactionType == WalletScanConstants.TRANSACTION_TYPE_ADD) {
            txtAmount.text = "Amount to be added"
            txtToRetOrRec.text = "Money to be returned?"
            origAmt = walletHistInfo.amtAdded!!
        } else {
            txtAmount.text = "Amount to be deducted"
            txtToRetOrRec.text = "Money to be recovered?"
            origAmt = walletHistInfo.amtDeducted!!
        }

        val tiePurpose = tilPurpose.editText
        tiePurpose?.setText(origPurpose)
        val tieAmount = tilAmount.editText
        tieAmount?.setText(origAmt.toString())

        btnSave.setOnClickListener {
            val strPurpose = tiePurpose?.text.toString().trim()
            val strAmount = tieAmount?.text.toString().trim()
            if (strAmount.isEmpty()) {
                tilAmount.error = "Please enter the amount"
                return@setOnClickListener
            }
            try {
                strAmount.toDouble()
            } catch (e: NumberFormatException) {
                tilAmount.error = "Please enter the valid amount"
                return@setOnClickListener
            }
            if (strPurpose == origPurpose && strAmount.toDouble() == origAmt
                && walletHistInfo.toRecoverOrReturn != null
                && schToRetOrRec.isChecked == walletHistInfo.toRecoverOrReturn!!
            ) {
                editDialog.dismiss()
                return@setOnClickListener
            }
            if (ConnectionManager.isNetworkAvailable(con)) {
                listener.editTransaction(
                    walletHistInfo.walletId,
                    walletHistInfo.id,
                    walletHistInfo.transactionType,
                    origAmt,
                    strAmount.toDouble(),
                    strPurpose,
                    schToRetOrRec.isChecked
                )
            } else {
                Toast.makeText(con, WalletScanConstants.NO_INTERNET_CONNECTON, Toast.LENGTH_SHORT)
                    .show()
            }
            editDialog.dismiss()
        }
        btnCancel.setOnClickListener {
            editDialog.dismiss()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(filter: CharSequence?): FilterResults {
                val charSearch = filter.toString()
                filteredList = if (charSearch == con.getString(R.string.clear_filter)) {
                    walletInfoList
                } else {
                    val transType=if (charSearch==con.getString(R.string.not_recovered)) WalletScanConstants.TRANSACTION_TYPE_DEDUCT
                        else WalletScanConstants.TRANSACTION_TYPE_ADD
                    val resultList = walletInfoList.filter { it.toRecoverOrReturn==true && it.transactionType==transType}.toMutableList()
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as MutableList<WalletHistInfo>
                notifyDataSetChanged()
            }

        }
    }
}