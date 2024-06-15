package com.ch4019.jdaassist.ui.screen.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.PackageInfoCompat.getLongVersionCode
import androidx.navigation.NavHostController
import com.ch4019.jdaassist.R
import com.ch4019.jdaassist.util.getPackageInfoCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutPage(
    navController: NavHostController,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("关于") },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.navigateUp() }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBackIosNew,
                            contentDescription = null
                        )
                    }
                },
            )
        },
        bottomBar = {

        }
    ) { paddingValues ->
        AboutView(modifier = Modifier.padding(paddingValues))
    }
}

@Composable
fun AboutView(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfoCompat(context.packageName, 0)
    val versionName = packageInfo.versionName
    val versionCode = getLongVersionCode(packageInfo)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            bitmap = ImageBitmap.imageResource(R.drawable.logo),
            contentDescription = "logo",
            Modifier.padding(top = 32.dp).size(72.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Jda Assist",
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(48.dp))
        Surface(
            shape = RoundedCornerShape(15.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
            ) {
                Text("当前版本")
                Spacer(Modifier.weight(1f))
                Text("$versionName($versionCode)")
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "纵然世间黑暗      仍有一点星光",
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(32.dp))
    }
}
