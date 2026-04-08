/*
 * Copyright (c) 2020-2026 Governikus GmbH & Co. KG, Germany
 */

package com.governikus.ausweisapp.tester.wrapper.card.ui.accessrights

import android.app.Application
import androidx.lifecycle.map
import com.governikus.ausweisapp.sdkwrapper.card.core.AccessRight
import com.governikus.ausweisapp.tester.wrapper.R
import com.governikus.ausweisapp.tester.wrapper.card.ui.WorkflowViewModel
import com.governikus.ausweisapp.tester.wrapper.card.ui.util.WorkflowFragmentViewModel

data class AccessRightsStatus(
    val accessRight: AccessRight,
    var enabled: Boolean,
    val editable: Boolean,
) {
    fun getAccessRightPrettyName(): Int =
        when (accessRight) {
            AccessRight.ADDRESS -> R.string.access_right_address
            AccessRight.BIRTH_NAME -> R.string.access_right_birth_name
            AccessRight.FAMILY_NAME -> R.string.access_right_family_name
            AccessRight.GIVEN_NAMES -> R.string.access_right_given_name
            AccessRight.PLACE_OF_BIRTH -> R.string.access_right_place_of_birth
            AccessRight.DATE_OF_BIRTH -> R.string.access_right_date_of_birth
            AccessRight.DOCTORAL_DEGREE -> R.string.access_right_doctoral_degree
            AccessRight.ARTISTIC_NAME -> R.string.access_right_artistic_name
            AccessRight.PSEUDONYM -> R.string.access_right_pseudonym
            AccessRight.VALID_UNTIL -> R.string.access_right_valid_until
            AccessRight.NATIONALITY -> R.string.access_right_nationality
            AccessRight.ISSUING_COUNTRY -> R.string.access_right_issuing_country
            AccessRight.DOCUMENT_TYPE -> R.string.access_right_document_type
            AccessRight.RESIDENCE_PERMIT_I -> R.string.access_right_residence_permit_1
            AccessRight.RESIDENCE_PERMIT_II -> R.string.access_right_residence_permit_2
            AccessRight.COMMUNITY_ID -> R.string.access_right_community_id
            AccessRight.ADDRESS_VERIFICATION -> R.string.access_right_address_verification
            AccessRight.AGE_VERIFICATION -> R.string.access_right_age_verification
            AccessRight.WRITE_ADDRESS -> R.string.access_right_write_address
            AccessRight.WRITE_COMMUNITY_ID -> R.string.access_right_write_community_id
            AccessRight.WRITE_RESIDENCE_PERMIT_I -> R.string.access_right_write_residence_permit_1
            AccessRight.WRITE_RESIDENCE_PERMIT_II -> R.string.access_right_write_residence_permit_2
            AccessRight.CAN_ALLOWED -> R.string.access_right_can_allowed
            AccessRight.PIN_MANAGEMENT -> R.string.access_right_pin_management
        }
}

internal class AccessRightsFragmentViewModel(
    workflowViewModel: WorkflowViewModel,
    application: Application,
) : WorkflowFragmentViewModel(workflowViewModel, application) {
    val requiredRightsAdapter: AccessRightsStatusAdapter = AccessRightsStatusAdapter()
    val optionalRightsAdapter: AccessRightsStatusAdapter = AccessRightsStatusAdapter()

    val requiredRightsStatus =
        workflowViewModel.accessRights.map { accessRights ->
            accessRights?.requiredRights?.map { accessRight ->
                AccessRightsStatus(
                    accessRight,
                    true,
                    false,
                )
            }
        }
    val hasRequiredRights =
        requiredRightsStatus.map { accessRights ->
            accessRights?.isNotEmpty() == true
        }

    val optionalRightsStatus =
        workflowViewModel.accessRights.map { accessRights ->
            accessRights?.optionalRights?.map { accessRight ->
                AccessRightsStatus(
                    accessRight,
                    accessRights.effectiveRights.contains(accessRight),
                    true,
                )
            }
        }
    val hasOptionalRights =
        optionalRightsStatus.map { accessRights ->
            accessRights?.isNotEmpty() == true
        }

    val hasCertificateDescription = workflowViewModel.certificateDescription.map { it != null }

    val certificateSubjectName = workflowViewModel.certificateDescription.map { it?.subjectName }

    val certificatePurpose = workflowViewModel.certificateDescription.map { it?.purpose }

    private fun checkedOptionalAccessRights() = optionalRightsStatus.value?.filter { it.enabled }?.map { it.accessRight } ?: emptyList()

    fun showCertificate() {
        workflowViewModel.showCertificate()
    }

    fun accept() {
        workflowViewModel.acceptAccessRights(checkedOptionalAccessRights())
    }
}
