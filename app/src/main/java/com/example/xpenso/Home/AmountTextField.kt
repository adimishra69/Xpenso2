import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    label: String = "Amount"
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // This new logic allows only digits and a single decimal point.
            // It prevents invalid input like "123.45.67".
            if (newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                onValueChange(newValue)
            }
        },
        label = { Text("Amount") },
        placeholder = { Text("0.00") },
        modifier = Modifier.fillMaxWidth(),
        // Use a keyboard that allows decimal input.
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal
        ),
        isError = isError
        // The visualTransformation has been removed to disable comma formatting.
    )
}
