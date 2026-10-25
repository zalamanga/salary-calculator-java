package calculator;

import java.math.BigDecimal;

public record SalaryBreakdown(
        BigDecimal basicSalary,
        BigDecimal hra,
        BigDecimal specialAllowance,
        BigDecimal pf,
        BigDecimal netPayableSalary) {
}
