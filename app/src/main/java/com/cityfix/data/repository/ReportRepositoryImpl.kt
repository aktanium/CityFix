package com.cityfix.data.repository

import android.net.Uri
import com.cityfix.data.local.dao.ReportDao
import com.cityfix.domain.model.Report
import com.cityfix.domain.repository.ReportRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val reportDao: ReportDao,
    private val auth: FirebaseAuth
) : ReportRepository {

    override fun getAllReports(): Flow<List<Report>> = callbackFlow {
        val sub = firestore.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Report::class.java)?.copy(id = it.id)
                } ?: emptyList()
                trySend(list)
                // cache locally
                GlobalScope.launch { reportDao.insertAll(list) }
            }
        awaitClose { sub.remove() }
    }.catch {
        emitAll(reportDao.getAllReports()) // fallback offline
    }

    override fun getReportById(id: String): Flow<Report?> =
        reportDao.getReportById(id)

    override fun getFilteredReports(category: String?, status: String?): Flow<List<Report>> =
        reportDao.getFilteredReports(category, status)

    override fun getReportsByUser(userId: String): Flow<List<Report>> =
        reportDao.getReportsByUser(userId)

    override fun getReportsCountByStatus(status: String): Flow<Int> =
        reportDao.getReportsCountByStatus(status)

    override suspend fun createReport(report: Report, imageUri: Uri?): String {
        val imageUrl = imageUri?.let { uploadImage(it, report.id) } ?: ""
        val final = report.copy(
            userId = auth.currentUser?.uid ?: "",
            authorEmail = auth.currentUser?.email ?: "Anonymous",
            imageUri = imageUrl
        )
        firestore.collection("reports").document(final.id).set(final).await()
        reportDao.insert(final)
        return final.id
    }

    private suspend fun uploadImage(uri: Uri, reportId: String): String {
        val ref = storage.reference.child("reports/$reportId.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    override suspend fun updateReport(report: Report) {
        firestore.collection("reports").document(report.id).set(report).await()
        reportDao.updateReport(report)
    }

    override suspend fun deleteReport(id: String) {
        // According to rules, delete is not allowed, but we'll leave it in interface
        // firestore.collection("reports").document(id).delete().await()
        reportDao.deleteReportById(id)
    }
}
