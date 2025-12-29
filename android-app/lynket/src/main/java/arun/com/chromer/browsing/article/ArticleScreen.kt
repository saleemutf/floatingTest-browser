package arun.com.chromer.browsing.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arun.com.chromer.data.Result
import arun.com.chromer.data.webarticle.model.WebArticle
import arun.com.chromer.util.ColorUtil
import arun.com.chromer.util.HtmlCompat
import coil.compose.AsyncImage
import org.jsoup.nodes.Element

@Composable
fun ArticleScreen(
    articleResult: Result<WebArticle>,
    accentColor: Color,
    textSizeIncrementSp: Int,
    onKeywordClick: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (articleResult) {
            is Result.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            is Result.Success -> {
                val article = articleResult.data
                if (article != null) {
                    ArticleContent(
                        article = article,
                        accentColor = accentColor,
                        textSizeIncrementSp = textSizeIncrementSp,
                        onKeywordClick = onKeywordClick,
                        onImageClick = onImageClick
                    )
                }
            }
            is Result.Failure -> {
                // Handled by Activity (finish and open in normal mode)
            }
        }
    }
}

@Composable
fun ArticleContent(
    article: WebArticle,
    accentColor: Color,
    textSizeIncrementSp: Int,
    onKeywordClick: (String) -> Unit,
    onImageClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 56.dp)
    ) {
        // Header Image
        if (!article.imageUrl.isNullOrEmpty()) {
            item {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clickable { onImageClick(article.imageUrl) },
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Title and Site Name
        if (!article.title.isNullOrEmpty() || !article.siteName.isNullOrEmpty()) {
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!article.title.isNullOrEmpty()) {
                        Text(
                            text = article.title,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            fontSize = (24 + textSizeIncrementSp).sp
                        )
                    }
                    if (!article.siteName.isNullOrEmpty()) {
                        Text(
                            text = article.siteName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            fontSize = (14 + textSizeIncrementSp).sp
                        )
                    }
                }
            }
        }

        // Keywords
        if (article.keywords != null && article.keywords.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(article.keywords) { keyword ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = accentColor,
                            modifier = Modifier.clickable { onKeywordClick(keyword) }
                        ) {
                            Text(
                                text = keyword,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Color.White, // Simplified for now
                                fontSize = (12 + textSizeIncrementSp).sp
                            )
                        }
                    }
                }
            }
        }

        // Article Elements
        if (article.elements != null) {
            items(article.elements.toList()) { element ->
                ArticleElement(
                    element = element,
                    accentColor = accentColor,
                    textSizeIncrementSp = textSizeIncrementSp,
                    onImageClick = onImageClick
                )
            }
        }
    }
}

@Composable
fun ArticleElement(
    element: Element,
    accentColor: Color,
    textSizeIncrementSp: Int,
    onImageClick: (String) -> Unit
) {
    val tag = element.tagName()
    val text = element.outerHtml()
    val annotatedText = HtmlCompat.fromHtml(text).toString() // Simplified, should ideally use a proper HTML to AnnotatedString converter

    when (tag) {
        "p" -> {
            Text(
                text = annotatedText,
                modifier = Modifier.padding(16.dp, 8.dp),
                fontSize = (16 + textSizeIncrementSp).sp,
                lineHeight = (24 + textSizeIncrementSp).sp
            )
        }
        "h1", "h2", "h3", "h4", "h5", "h6" -> {
            val fontSize = when(tag) {
                "h1" -> 22
                "h2" -> 20
                "h3" -> 18
                else -> 16
            }
            Text(
                text = annotatedText,
                modifier = Modifier.padding(16.dp, 16.dp, 16.dp, 8.dp),
                fontWeight = FontWeight.Bold,
                fontSize = (fontSize + textSizeIncrementSp).sp
            )
        }
        "img" -> {
            val src = element.attr("src")
            if (src.isNotEmpty()) {
                AsyncImage(
                    model = src,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { onImageClick(src) },
                    contentScale = ContentScale.Fit
                )
            }
        }
        "blockquote" -> {
            Text(
                text = annotatedText,
                modifier = Modifier
                    .padding(16.dp, 8.dp)
                    .background(Color.LightGray.copy(alpha = 0.2f))
                    .padding(8.dp),
                color = accentColor,
                fontStyle = FontStyle.Italic,
                fontSize = (16 + textSizeIncrementSp).sp
            )
        }
        else -> {
            // Fallback for other elements
            Text(
                text = annotatedText,
                modifier = Modifier.padding(16.dp, 4.dp),
                fontSize = (14 + textSizeIncrementSp).sp
            )
        }
    }
}

