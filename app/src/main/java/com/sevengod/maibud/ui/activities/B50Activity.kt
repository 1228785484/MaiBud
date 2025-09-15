package com.sevengod.maibud.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.sevengod.maibud.data.entities.SongEntity
import com.sevengod.maibud.data.model.Song
import com.sevengod.maibud.data.viewmodels.B50ViewModel
import com.sevengod.maibud.data.viewmodels.B50ViewModelFactory
import com.sevengod.maibud.data.viewmodels.LoginUiState
import com.sevengod.maibud.data.viewmodels.QRCodeUiState
import com.sevengod.maibud.repository.RecordRepository
import com.sevengod.maibud.ui.theme.MaiBudTheme
import com.sevengod.maibud.utils.SongUtil

class B50Activity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaiBudTheme {
                val viewModel: B50ViewModel = viewModel(
                    factory = B50ViewModelFactory(RecordRepository, this@B50Activity)
                )
                B50Display(viewModel)
            }
        }
    }
}

//TODO B50展示界面
@Composable
fun B50Display(viewModel: B50ViewModel) {
    val recordData by viewModel.getB50RecordDataFlow(LocalContext.current)
        .collectAsStateWithLifecycle(initialValue = null);
//    //卡片展示
//    recordData.let { data ->
//        data?.forEach { it ->
//            Log.i("info", it.title)
//        }
//    }
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
                text = "TotalSize:" + recordData?.size,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )


        }
    }
}
//TODO 获取Song+Record
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun songDisplay(song: SongEntity) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            GlideImage(
                model = { SongUtil.getCoverId(song.id) },
                modifier = Modifier.size(200.dp),
                contentDescription = song.title,
            )
        }
    }
}
@Preview(showBackground = false)
@Composable
fun SongDisplayPreview() {
    MaiBudTheme {
        val song = SongEntity(
            id = 1,
            title = 123.toString(),
            artist = 123.toString(),
            genre = 123.toString(),
            bpm = 123,
            from = 123.toString(),
            type = 123.toString(),
            isNew = true,
            buddy = 123.toString()
        )
        songDisplay(
            song
        )
    }
}