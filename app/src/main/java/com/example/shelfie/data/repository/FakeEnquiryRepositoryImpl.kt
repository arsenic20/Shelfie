package com.example.shelfie.data.repository

import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Enquiry
import com.example.shelfie.domain.repository.EnquiryRepository
import javax.inject.Inject
import kotlinx.coroutines.delay

/**
 * Placeholder that simulates sending the enquiry to the sales inbox.
 *
 * A real email provider (SMTP, SendGrid, a backend endpoint, ...) replaces this by implementing
 * [EnquiryRepository] and changing the single binding in `RepositoryModule`; no UI code changes.
 */
class FakeEnquiryRepositoryImpl @Inject constructor() : EnquiryRepository {
    override suspend fun submit(enquiry: Enquiry): AppResult<Unit> {
        delay(SIMULATED_LATENCY_MS)
        return AppResult.Success(Unit)
    }

    private companion object {
        const val SIMULATED_LATENCY_MS = 1_000L
    }
}
