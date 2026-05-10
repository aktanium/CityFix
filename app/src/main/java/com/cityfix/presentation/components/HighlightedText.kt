package com.cityfix.presentation.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import com.cityfix.presentation.theme.BrandPrimary

/**
 * Renders [text] and bolds/highlights every case-insensitive occurrence of [query].
 * Falls through to a plain Text when the query is blank so callers can use the
 * same composable regardless of search state.
 */
@Composable
fun HighlightedText(
    text: String,
    query: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip
) {
    if (query.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow
        )
        return
    }
    val annotated = buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var lastIndex = 0
        var index = lowerText.indexOf(lowerQuery)
        while (index != -1) {
            append(text.substring(lastIndex, index))
            withStyle(
                SpanStyle(
                    background = BrandPrimary.copy(alpha = 0.2f),
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            lastIndex = index + query.length
            index = lowerText.indexOf(lowerQuery, lastIndex)
        }
        append(text.substring(lastIndex))
    }
    Text(
        text = annotated,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow
    )
}
