/*
 * Copyright (c) 2023-2026 Governikus Service GmbH, Germany
 */

package com.governikus.ausweisapp.sdkwrapper.card.core.util

internal typealias WorkflowSimulator = com.governikus.ausweisapp.sdkwrapper.card.core.Simulator
internal typealias WorkflowSimulatorFile = com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorFile
internal typealias WorkflowSimulatorKey = com.governikus.ausweisapp.sdkwrapper.card.core.SimulatorKey

internal typealias CommandSimulator = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.Simulator
internal typealias CommandSimulatorFile = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SimulatorFile
internal typealias CommandSimulatorKey = com.governikus.ausweisapp.sdkwrapper.card.core.ausweisapp.protocol.SimulatorKey

internal fun workflowSimulatorToCommandSimulator(simulator: WorkflowSimulator?): CommandSimulator? {
    simulator ?: return null
    return CommandSimulator(
        files = simulator.files.map { workflowSimulatorFileToCommandSimulatorFile(file = it) },
        keys = simulator.keys?.map { workflowSimulatorKeyToCommandSimulatorKey(key = it) },
    )
}

internal fun workflowSimulatorFileToCommandSimulatorFile(file: WorkflowSimulatorFile): CommandSimulatorFile =
    CommandSimulatorFile(
        fileId = file.fileId,
        shortFileId = file.shortFileId,
        content = file.content,
    )

internal fun workflowSimulatorKeyToCommandSimulatorKey(key: WorkflowSimulatorKey): CommandSimulatorKey =
    CommandSimulatorKey(
        id = key.id,
        content = key.content,
    )
