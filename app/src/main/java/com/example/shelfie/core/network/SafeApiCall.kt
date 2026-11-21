package com.example.shelfie.core.network

import com.example.shelfie.core.common.AppError
import com.example.shelfie.core.common.AppResult
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.HttpException

/** Runs [block] and maps every failure to an [AppError] instead of throwing. */
suspend fun <T> safeApiCall(block: suspend () -> T): AppResult<T> = try {
    AppResult.Success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: HttpException) {
    AppResult.Failure(AppError.Http(e.code(), e.message()))
} catch (e: IOException) {
    AppResult.Failure(AppError.NoNetwork)
} catch (e: Exception) {
    AppResult.Failure(AppError.Unexpected(e))
}
