package com.yogaadmin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class YogaClass(
    val id: String,
    val dayOfWeek: String,
    val time: String,
    val capacity: Int,
    val duration: Int,
    val price: Double,
    val type: String,
    val description: String? = null,
    val location: String? = null,
    val difficulty: String? = null,
    val instructor: String? = null,
    val lastModified: String? = null,
    val cloudId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) : Parcelable

@Parcelize
data class ClassInstance(
    val id: String,
    val classId: String,
    val date: String,
    val teacher: String,
    val comments: String? = null,
    val attendees: Int? = null,
    val status: InstanceStatus = InstanceStatus.SCHEDULED,
    val lastModified: String? = null,
    val cloudId: String? = null,
    val syncStatus: SyncStatus = SyncStatus.PENDING
) : Parcelable

@Parcelize
data class Activity(
    val id: String,
    val type: String,
    val description: String,
    val timestamp: String,
    val relatedId: String? = null
) : Parcelable

@Parcelize
data class SyncHistory(
    val id: String,
    val timestamp: String,
    val status: SyncHistoryStatus,
    val type: SyncType,
    val trigger: SyncTrigger,
    val duration: Long? = null,
    val recordsProcessed: SyncRecordCount,
    val recordsUploaded: SyncRecordCount,
    val recordsSkipped: SyncRecordCount,
    val errors: List<SyncError> = emptyList(),
    val retryCount: Int = 0,
    val dataSize: Double, // in KB
    val networkSpeed: NetworkSpeed? = null,
    val deviceInfo: DeviceInfo? = null
) : Parcelable

@Parcelize
data class SyncRecordCount(
    val classes: Int,
    val instances: Int,
    val total: Int
) : Parcelable

@Parcelize
data class SyncError(
    val id: String,
    val type: SyncErrorType,
    val message: String,
    val code: String? = null,
    val recordId: String? = null,
    val recordType: RecordType? = null,
    val timestamp: String,
    val resolved: Boolean = false
) : Parcelable

@Parcelize
data class DeviceInfo(
    val battery: Int,
    val storage: String,
    val connection: String
) : Parcelable

@Parcelize
data class SyncStats(
    val totalSyncs: Int,
    val successfulSyncs: Int,
    val failedSyncs: Int,
    val lastSuccessfulSync: String? = null,
    val averageSyncTime: Long,
    val totalDataSynced: Double, // in KB
    val syncFrequency: SyncFrequency
) : Parcelable

@Parcelize
data class SyncFrequency(
    val daily: Int,
    val weekly: Int,
    val monthly: Int
) : Parcelable

enum class SyncStatus {
    SYNCED, PENDING, CONFLICT, ERROR
}

enum class InstanceStatus {
    SCHEDULED, COMPLETED, CANCELLED
}

enum class SyncHistoryStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, CANCELLED
}

enum class SyncType {
    FULL, INCREMENTAL, MANUAL, AUTO
}

enum class SyncTrigger {
    USER, SCHEDULE, DATA_CHANGE, RETRY
}

enum class NetworkSpeed {
    FAST, SLOW, OFFLINE
}

enum class SyncErrorType {
    NETWORK, VALIDATION, SERVER, AUTH, QUOTA, CONFLICT
}

enum class RecordType {
    CLASS, INSTANCE
}

object DaysOfWeek {
    val ALL = listOf(
        "Monday",
        "Tuesday",
        "Wednesday",
        "Thursday",
        "Friday",
        "Saturday",
        "Sunday"
    )
}

object ClassTypes {
    val ALL = listOf(
        "Flow Yoga",
        "Aerial Yoga",
        "Family Yoga",
        "Hot Yoga",
        "Restorative Yoga",
        "Vinyasa Yoga",
        "Hatha Yoga",
        "Yin Yoga"
    )
}

object DifficultyLevels {
    val ALL = listOf(
        "Beginner",
        "Intermediate",
        "Advanced",
        "All Levels"
    )
}

object ActivityTypes {
    const val CLASS_CREATED = "class_created"
    const val CLASS_UPDATED = "class_updated"
    const val CLASS_DELETED = "class_deleted"
    const val INSTANCE_CREATED = "instance_created"
    const val INSTANCE_UPDATED = "instance_updated"
    const val INSTANCE_DELETED = "instance_deleted"
    const val DATA_SYNCED = "data_synced"
    const val SYNC_FAILED = "sync_failed"
    const val SYNC_STARTED = "sync_started"
}