package com.aistudio.promptforge.abcd.api.provider

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ProviderType {
    GEMINI_DIRECT,
    BACKEND_PROXY,
    LOCAL_AUTONOMOUS
}

class ProviderManager(
    val geminiDirectProvider: GeminiDirectProvider = GeminiDirectProvider(),
    val backendProxyProvider: BackendProxyProvider = BackendProxyProvider(),
    val localAutonomousProvider: LocalAutonomousProvider = LocalAutonomousProvider()
) {
    private val _activeProviderType = MutableStateFlow(ProviderType.GEMINI_DIRECT)
    val activeProviderType: StateFlow<ProviderType> = _activeProviderType.asStateFlow()

    fun getActiveProvider(): AiProvider {
        return when (_activeProviderType.value) {
            ProviderType.GEMINI_DIRECT -> geminiDirectProvider
            ProviderType.BACKEND_PROXY -> backendProxyProvider
            ProviderType.LOCAL_AUTONOMOUS -> localAutonomousProvider
        }
    }

    fun setProviderType(type: ProviderType) {
        _activeProviderType.value = type
    }

    fun configureProxy(url: String, token: String) {
        backendProxyProvider.updateProxyConfig(url, token)
    }

    fun setCustomApiKey(key: String) {
        geminiDirectProvider.setCustomApiKey(key)
    }
}
