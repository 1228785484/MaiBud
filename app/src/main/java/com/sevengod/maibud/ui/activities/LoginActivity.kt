package com.sevengod.maibud.ui.activities

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sevengod.maibud.data.viewmodels.LoginViewModel
import com.sevengod.maibud.data.viewmodels.LoginViewModelFactory
import com.sevengod.maibud.data.viewmodels.LoginUiState
import com.sevengod.maibud.repository.LoginRepository
import com.sevengod.maibud.ui.theme.MaiBudTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiBudTheme {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModelFactory(LoginRepository, this@LoginActivity)
                )
                
                LoginScreen(
                    loginViewModel = loginViewModel,
                    onLoginSuccess = {
                        // 登录成功后的操作，比如跳转到主界面
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
) {
    LoginScreenStateless(
        username = loginViewModel.username,
        password = loginViewModel.password,
        uiState = loginViewModel.loginUiState,
        onUsernameChange = loginViewModel::onUsernameChange,
        onPasswordChange = loginViewModel::onPasswordChange,
        onLoginClick = { loginViewModel.attemptLogin() },
        onLoginSuccess = onLoginSuccess
    )
}

@Preview
@Composable
fun LoginScreenPreview() {
    MaiBudTheme {
        // 为 Preview 创建一个简化的 UI 版本
        LoginScreenStateless(
            username = "预览用户名",
            password = "预览密码",
            uiState = LoginUiState.Idle,
            onUsernameChange = {},
            onPasswordChange = {},
            onLoginClick = {},
            onLoginSuccess = {}
        )
    }
}

@Composable
fun LoginScreenStateless(
    username: String,
    password: String,
    uiState: LoginUiState,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginSuccess: () -> Unit,
) {
    val context = LocalContext.current
    
    // 监听登录成功状态
    LaunchedEffect(uiState) {
        if (uiState is LoginUiState.Success) {
            // 显示 JWT token 的 Toast
            val jwt = uiState.loginData.jwt
            if (jwt != null) {
                Toast.makeText(
                    context,
                    "登录成功！JWT Token: $jwt",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    context,
                    "登录成功！",
                    Toast.LENGTH_SHORT
                ).show()
            }
            onLoginSuccess()
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp), // Add some overall padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "请输入查分器里的账号",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChange,
                label = { Text("用户名") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                enabled = uiState !is LoginUiState.Loading
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                enabled = uiState !is LoginUiState.Loading
            )
            
            // 显示错误信息
            if (uiState is LoginUiState.Error) {
                Text(
                    text = uiState.message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            Button(
                onClick = onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is LoginUiState.Loading
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
                Text("登录")
            }
        }
    }
}

