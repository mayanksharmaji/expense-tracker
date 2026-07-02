package com.expensetracker.data.remote

import com.expensetracker.data.remote.dto.*
import retrofit2.http.*

interface ApiService {

    @GET("api/plan")
    suspend fun getPlan(): PlanDto?

    @POST("api/plan")
    suspend fun savePlan(@Body plan: Map<String, Any>): StatusResponse

    @DELETE("api/plan")
    suspend fun deletePlan(): StatusResponse

    @GET("api/expenses")
    suspend fun getExpenses(): List<ExpenseDto>

    @POST("api/expenses")
    suspend fun addExpense(@Body expense: Map<String, Any>): StatusResponse

    @DELETE("api/expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: Long): StatusResponse

    @GET("api/stats")
    suspend fun getStats(): StatsDto
}
