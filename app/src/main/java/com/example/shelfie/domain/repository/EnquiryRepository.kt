package com.example.shelfie.domain.repository

import com.example.shelfie.core.common.AppResult
import com.example.shelfie.domain.model.Enquiry

interface EnquiryRepository {
    suspend fun submit(enquiry: Enquiry): AppResult<Unit>
}
