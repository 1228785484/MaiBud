package com.sevengod.maibud.data.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.room.Database
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.sevengod.maibud.data.entities.UserProfile
import com.sevengod.maibud.data.model.LoginResponse // Assuming LoginResponse is in data.model
import com.sevengod.maibud.instances.DataBaseInstance
import com.sevengod.maibud.repository.LoginRepository
import com.sevengod.maibud.utils.DSUtils
import kotlinx.coroutines.launch

// Sealed interface to represent the different states of the login UI
sealed interface LoginUiState {
    object Idle : LoginUiState
    object Loading : LoginUiState
    data class Success(val loginData: LoginResponse) : LoginUiState
    data class Error(val message: String) : LoginUiState
}

class LoginViewModel(
    private val loginRepository: LoginRepository, // Dependency passed via constructor
    private val context: Context // Context for database access
) : ViewModel() {

    var username by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var loginUiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()


    fun onUsernameChange(newUsername: String) {
        username = newUsername
        if (loginUiState is LoginUiState.Error) {
            loginUiState = LoginUiState.Idle
        }
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
        if (loginUiState is LoginUiState.Error) {
            loginUiState = LoginUiState.Idle
        }
    }

    fun attemptLogin() {
        if (username.isBlank() || password.isBlank()) {
            loginUiState = LoginUiState.Error("Username and password cannot be empty.")
            return
        }

        loginUiState = LoginUiState.Loading

        viewModelScope.launch {
            try {
                val result = loginRepository.getLoginToken(username, password)
                result.fold(
                    onSuccess = { loginResponse ->
                        val userProfile = UserProfile(
                            username = username,
                            jwtToken = loginResponse.jwt ?: "",
                            nickname = username
                        )

                        // 保存用户数据到数据库
                        saveUserToDatabase(loginResponse)

                        // 保存当前用户到DSUtils
                        saveCurrentUserToStorage(userProfile)

                        //保存当前用户值
                        _currentUser.value = userProfile

                        loginUiState = LoginUiState.Success(loginResponse)
                    },
                    onFailure = { exception ->
                        loginUiState = LoginUiState.Error(exception.message ?: "Login failed.")
                    }
                )
            } catch (e: Exception) {
                loginUiState = LoginUiState.Error("An unexpected error occurred: ${e.message}")
            }
        }
    }

    fun resetLoginState() {
        loginUiState = LoginUiState.Idle
    }

    /**
     * 保存用户数据到数据库
     */
    private suspend fun saveUserToDatabase(loginResponse: LoginResponse) {
        try {
            //修复错误
            val database = DataBaseInstance.getInstance(context)
            val userDao = database.userDao()

            // 创建用户资料对象
            val userProfile = UserProfile(
                // userId 使用默认值 0，Room 会自动生成自增ID
                username = username,
                jwtToken = loginResponse.jwt ?: "", // 如果 JWT 为空，使用空字符串
                nickname = username // nickname 默认等于 username
            )

            // 保存到数据库
            userDao.insertOrUpdateUserProfile(userProfile)
        } catch (e: Exception) {
            // 数据库保存失败不影响登录状态，只记录错误
            e.printStackTrace()
        }
    }

    /**
     * 登出当前用户
     */
    fun logout() {
        viewModelScope.launch {
            try {
                // 清除当前用户状态
                _currentUser.value = null

                // 清除DSUtils中的用户信息
                clearCurrentUserFromStorage()

                // 可选：清除数据库中的用户信息
                // val database = DBUtils.getDatabase(context)
                // val userDao = database.userDao()
                // userDao.deleteUser(currentUser.value?.username ?: "")

                // 重置登录状态
                loginUiState = LoginUiState.Idle
                username = ""
                password = ""

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    /**
     * 保存当前用户信息到DSUtils
     */
    private suspend fun saveCurrentUserToStorage(userProfile: UserProfile) {
        try {
            DSUtils.storeData(context, "current_user_username", userProfile.username)
            DSUtils.storeData(context, "current_user_jwt", userProfile.jwtToken)
            DSUtils.storeData(context, "current_user_nickname", userProfile.nickname?:"")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 从DSUtils加载当前用户信息
     */
    fun loadCurrentUserFromStorage() {
        viewModelScope.launch {
            try {
                val username = DSUtils.getData(context, "current_user_username")
                val jwt = DSUtils.getData(context, "current_user_jwt")
                val nickname = DSUtils.getData(context, "current_user_nickname")

                if (username != null && jwt != null && nickname != null) {
                    val userProfile = UserProfile(
                        username = username,
                        jwtToken = jwt,
                        nickname = nickname
                    )
                    _currentUser.value = userProfile
                    this@LoginViewModel.username = username
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 清除DSUtils中的用户信息
     */
    private suspend fun clearCurrentUserFromStorage() {
        try {
            DSUtils.removeData(context, "current_user_username")
            DSUtils.removeData(context, "current_user_jwt")
            DSUtils.removeData(context, "current_user_nickname")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


/**
 * Factory for creating LoginViewModel instances with a LoginRepository dependency.
 */
class LoginViewModelFactory(
    private val loginRepository: LoginRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(loginRepository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}