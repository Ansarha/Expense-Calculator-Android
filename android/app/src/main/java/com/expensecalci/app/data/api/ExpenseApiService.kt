package com.expensecalci.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ExpenseApiService {

    // 0. Health
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, Any>>

    // 1. Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<TokenResponse>>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<TokenResponse>>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    // 2. Rooms
    @GET("rooms")
    suspend fun getRooms(): Response<ApiResponse<List<RoomDto>>>

    @POST("rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<ApiResponse<RoomDto>>

    @GET("rooms/{roomId}")
    suspend fun getRoomDetails(@Path("roomId") roomId: String): Response<ApiResponse<RoomDto>>

    @POST("rooms/{roomId}/invitations")
    suspend fun generateInvitation(@Path("roomId") roomId: String): Response<ApiResponse<RoomInvitationDto>>

    @POST("rooms/join")
    suspend fun joinRoom(@Query("invite_code") inviteCode: String): Response<ApiResponse<RoomDto>>

    // 3. Members
    @GET("rooms/{roomId}/members")
    suspend fun getRoomMembers(@Path("roomId") roomId: String): Response<ApiResponse<List<MemberDto>>>

    @POST("rooms/{roomId}/members")
    suspend fun addMember(
        @Path("roomId") roomId: String,
        @Body request: AddMemberRequest
    ): Response<ApiResponse<MemberDto>>

    @DELETE("rooms/{roomId}/members/{memberId}")
    suspend fun removeMember(
        @Path("roomId") roomId: String,
        @Path("memberId") memberId: String
    ): Response<ApiResponse<Map<String, Any>>>

    // 4. Expenses
    @GET("rooms/{roomId}/expenses")
    suspend fun getRoomExpenses(
        @Path("roomId") roomId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<List<ExpenseDto>>>

    @POST("rooms/{roomId}/expenses")
    suspend fun createExpense(
        @Path("roomId") roomId: String,
        @Body request: CreateExpenseRequest
    ): Response<ApiResponse<ExpenseDto>>

    @DELETE("rooms/{roomId}/expenses/{expenseId}")
    suspend fun deleteExpense(
        @Path("roomId") roomId: String,
        @Path("expenseId") expenseId: String
    ): Response<ApiResponse<Map<String, Any>>>

    // 5. Balances & Settlements
    @GET("rooms/{roomId}/summary")
    suspend fun getRoomSummary(
        @Path("roomId") roomId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<RoomSummaryDto>>

    @GET("rooms/{roomId}/balances")
    suspend fun getRoomBalances(
        @Path("roomId") roomId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<List<MemberBalanceDto>>>

    @GET("rooms/{roomId}/settlements")
    suspend fun getSettlements(
        @Path("roomId") roomId: String,
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<List<SettlementDto>>>

    @POST("rooms/{roomId}/settlements/{settlementId}/mark-paid")
    suspend fun markSettlementPaid(
        @Path("roomId") roomId: String,
        @Path("settlementId") settlementId: String,
        @Body request: MarkPaidRequest = MarkPaidRequest()
    ): Response<ApiResponse<SettlementDto>>

    @POST("rooms/{roomId}/settlements/{settlementId}/confirm")
    suspend fun confirmSettlement(
        @Path("roomId") roomId: String,
        @Path("settlementId") settlementId: String
    ): Response<ApiResponse<SettlementDto>>

    // 6. Stability & Dashboard
    @GET("personal/stability")
    suspend fun getStabilityScore(
        @Query("month") month: Int? = null,
        @Query("year") year: Int? = null
    ): Response<ApiResponse<StabilityScoreDto>>
}
