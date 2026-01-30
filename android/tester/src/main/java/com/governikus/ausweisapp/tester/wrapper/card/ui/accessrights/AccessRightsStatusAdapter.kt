/**
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.accessrights

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.governikus.ausweisapp.tester.wrapper.databinding.ViewAccessRightBinding

internal class AccessRightsStatusViewHolder(
    val viewBinding: ViewAccessRightBinding,
) : RecyclerView.ViewHolder(viewBinding.root) {
    fun bindTo(
        status: AccessRightsStatus,
        onAcceptedChanged: (checked: Boolean) -> Unit,
    ) {
        viewBinding.accessRightName.setText(status.getAccessRightPrettyName())

        viewBinding.accessRightSwitch.setOnCheckedChangeListener { _, checked ->
            onAcceptedChanged(
                checked,
            )
        }
        viewBinding.accessRightSwitch.setChecked(status.enabled)
        viewBinding.accessRightSwitch.setEnabled(status.editable)
        viewBinding.accessRightSwitch.setVisibility(if (status.editable) View.VISIBLE else View.GONE)

        viewBinding.accessRightSwitch.jumpDrawablesToCurrentState()
    }
}

internal class AccessRightsStatusDiffCallback : DiffUtil.ItemCallback<AccessRightsStatus>() {
    override fun areItemsTheSame(
        oldItem: AccessRightsStatus,
        newItem: AccessRightsStatus,
    ): Boolean = oldItem.accessRight == newItem.accessRight

    override fun areContentsTheSame(
        oldItem: AccessRightsStatus,
        newItem: AccessRightsStatus,
    ): Boolean = oldItem == newItem
}

internal class AccessRightsStatusAdapter : ListAdapter<AccessRightsStatus, AccessRightsStatusViewHolder>(AccessRightsStatusDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): AccessRightsStatusViewHolder {
        val viewBinding = ViewAccessRightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccessRightsStatusViewHolder(viewBinding)
    }

    override fun onBindViewHolder(
        holder: AccessRightsStatusViewHolder,
        position: Int,
    ) {
        val status = getItem(position)
        holder.bindTo(status) { checked ->
            status.enabled = checked
        }
    }
}
