package com.cityfix.data.repository

import android.net.Uri
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.data.local.toDomain
import com.cityfix.data.local.toDto
import com.cityfix.data.local.toEntity
import com.cityfix.data.remote.ReportDto
import com.cityfix.di.AppCoroutineScope
import com.cityfix.domain.model.Report
import com.cityfix.domain.repository.AuthRepository
import com.cityfix.domain.repository.ReportRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val reportDao: ReportDao,
    private val authRepository: AuthRepository,
    @AppCoroutineScope private val appScope: CoroutineScope
) : ReportRepository {

    override fun getAllReports(): Flow<List<Report>> = callbackFlow {
        val sub = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(ReportDto::class.java)
                            ?.copy(id = doc.id)
                            ?.toDomain()
                    }
                    ?.filter { it.status != "DELETED" }
                    ?: emptyList()
                trySend(list)
                // Cache locally — outlives any single ViewModel, hence the app-wide scope.
                appScope.launch { reportDao.insertAll(list.map { it.toEntity() }) }
            }
        awaitClose { sub.remove() }
    }.catch {
        // Fallback offline: read from Room cache and map back to domain.
        emitAll(reportDao.getAllReports().map { rows -> rows.map { it.toDomain() } })
    }

    override fun getReportById(id: String): Flow<Report?> = flow {
        // If the list screen hasn't populated the cache yet, do a one-shot
        // Firestore fetch so the detail screen isn't permanently empty on cold open.
        if (reportDao.getReportByIdOnce(id) == null) {
            runCatching {
                val doc = firestore.collection("reports").document(id).get().await()
                if (doc.exists()) {
                    doc.toObject(ReportDto::class.java)
                        ?.copy(id = doc.id)
                        ?.toDomain()
                        ?.takeIf { it.status != "DELETED" }
                        ?.let { reportDao.insert(it.toEntity()) }
                }
            }
        }
        emitAll(reportDao.getReportById(id).map { it?.toDomain() })
    }

    override fun getFilteredReports(category: String?, status: String?): Flow<List<Report>> =
        reportDao.getFilteredReports(category, status)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getReportsByUser(userId: String): Flow<List<Report>> =
        reportDao.getReportsByUser(userId)
            .map { rows -> rows.map { it.toDomain() } }

    override fun getReportsCountByStatus(status: String): Flow<Int> =
        reportDao.getReportsCountByStatus(status)

    override suspend fun createReport(report: Report, imageUri: Uri?): String {
        // Firebase Storage requires a paid plan, so we don't upload the image.
        // The local content:// URI is persisted as-is — it will only resolve on the
        // device that created the report; other devices fall back to a placeholder.
        val firebaseUser = authRepository.currentUser.first()
        val final = report.copy(
            userId = authRepository.currentUserId.orEmpty(),
            authorEmail = firebaseUser?.email ?: "Anonymous",
            imageUri = imageUri?.toString() ?: report.imageUri
        )
        firestore.collection("reports").document(final.id).set(final.toDto()).await()
        reportDao.insert(final.toEntity())
        return final.id
    }

    override suspend fun updateReport(report: Report) {
        firestore.collection("reports").document(report.id).set(report.toDto()).await()
        reportDao.updateReport(report.toEntity())
    }

    override suspend fun deleteReport(id: String) {
        // Firestore security rules block hard delete; soft-delete via status flag instead.
        // The list-side query (Firestore + Room) filters out anything with status == "DELETED".
        firestore.collection("reports")
            .document(id)
            .update("status", "DELETED")
            .await()
        reportDao.deleteReportById(id)
    }

    override suspend fun refreshReports() {
        // Force-bypass the Firestore cache and pull from the server, then persist
        // to Room so the realtime listener and offline fallback both see fresh data.
        val snap = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get(Source.SERVER)
            .await()

        val fresh = snap.documents
            .mapNotNull { doc ->
                doc.toObject(ReportDto::class.java)
                    ?.copy(id = doc.id)
                    ?.toDomain()
            }
            .filter { it.status != "DELETED" }

        reportDao.insertAll(fresh.map { it.toEntity() })
    }
}
