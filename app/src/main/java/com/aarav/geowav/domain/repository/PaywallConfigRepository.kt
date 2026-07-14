package com.aarav.geowav.domain.repository

import com.aarav.geowav.data.model.PaywallConfig
import kotlinx.coroutines.flow.StateFlow

interface PaywallConfigRepository {
    val paywallConfig: StateFlow<PaywallConfig>
    suspend fun refreshConfig(): Result<PaywallConfig>
}
