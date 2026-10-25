package calculator;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import org.junit.jupiter.api.Test;

class SalaryCalculatorTest {
    private String run(String input) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        SalaryCalculator.run(new Scanner(input), new PrintStream(bytes, true, StandardCharsets.UTF_8));
        return bytes.toString(StandardCharsets.UTF_8);
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }

    @Test
    void appliesAllThreeOriginalRates() {
        SalaryBreakdown result = SalaryCalculator.calculate(new BigDecimal("100"));
        assertAmount("100", result.basicSalary());
        assertAmount("50", result.hra());
        assertAmount("75", result.specialAllowance());
        assertAmount("12", result.pf());
        assertAmount("213", result.netPayableSalary());
    }

    @Test
    void preservesFractionalResultsWithoutIntegerTruncationOrRounding() {
        SalaryBreakdown result = SalaryCalculator.calculate(new BigDecimal("0.01"));
        assertAmount("0.005", result.hra());
        assertAmount("0.0075", result.specialAllowance());
        assertAmount("0.0012", result.pf());
        assertAmount("0.0213", result.netPayableSalary());
        assertTrue(run("0.01\n").contains("Gaji bersih latihan: 0.0213"));
    }

    @Test
    void handlesZeroAndValuesBeyondIntegerRangeExactly() {
        assertAmount("0", SalaryCalculator.calculate(BigDecimal.ZERO).netPayableSalary());
        assertAmount("2129999999999999999.9787",
                SalaryCalculator.calculate(new BigDecimal("999999999999999999.99")).netPayableSalary());
        assertTrue(run("999999999999999999.99\n").contains("2129999999999999999.9787"));
        assertTrue(run("0\n").contains("Gaji bersih latihan: 0.00"));
    }

    @Test
    void rejectsNegativeOrMissingApiInput() {
        assertThrows(IllegalArgumentException.class, () -> SalaryCalculator.calculate(null));
        assertThrows(IllegalArgumentException.class, () -> SalaryCalculator.calculate(new BigDecimal("-0.01")));
    }

    @Test
    void consoleRetriesInvalidFormatsThenProducesOneReport() {
        List<String> invalid = List.of("", "-1", "+1", "abc", "NaN", "Infinity", "1e3", "1,00", "0.001", "1000000000000000000");
        String text = run(String.join("\n", invalid) + "\n 1000000.50 \n");
        assertEquals(invalid.size(), text.split("Masukan tidak valid", -1).length - 1);
        assertTrue(text.contains("HRA (50%): 500000.25"));
        assertTrue(text.contains("Tunjangan khusus (75%): 750000.375"));
        assertTrue(text.contains("PF (12%): 120000.06"));
        assertTrue(text.contains("Gaji bersih latihan: 2130001.065"));
        assertEquals(1, text.split("Gaji bersih latihan:", -1).length - 1);
    }

    @Test
    void eofAtFirstPromptOrAfterInvalidInputDoesNotPrintAResult() {
        for (String input : List.of("", "bad\n", "-1\n")) {
            String text = assertTimeoutPreemptively(Duration.ofSeconds(2), () -> run(input));
            assertFalse(text.contains("Gaji bersih latihan:"));
        }
    }
}
