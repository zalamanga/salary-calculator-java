package calculator;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Pattern;

public final class SalaryCalculator {
    private static final BigDecimal HRA_RATE = new BigDecimal("0.50");
    private static final BigDecimal SPECIAL_ALLOWANCE_RATE = new BigDecimal("0.75");
    private static final BigDecimal PF_RATE = new BigDecimal("0.12");
    private static final Pattern SALARY = Pattern.compile("[0-9]{1,18}(?:\\.[0-9]{1,2})?");

    private SalaryCalculator() {
    }

    public static void main(String[] args) {
        run(new Scanner(System.in, StandardCharsets.UTF_8), System.out);
    }

    public static SalaryBreakdown calculate(BigDecimal basicSalary) {
        if (basicSalary == null || basicSalary.signum() < 0) {
            throw new IllegalArgumentException("Gaji pokok harus berupa angka nonnegatif.");
        }
        BigDecimal hra = basicSalary.multiply(HRA_RATE);
        BigDecimal allowance = basicSalary.multiply(SPECIAL_ALLOWANCE_RATE);
        BigDecimal pf = basicSalary.multiply(PF_RATE);
        BigDecimal net = basicSalary.add(hra).add(allowance).subtract(pf);
        return new SalaryBreakdown(basicSalary, hra, allowance, pf, net);
    }

    public static void run(Scanner input, PrintStream output) {
        Objects.requireNonNull(input, "input");
        Objects.requireNonNull(output, "output");
        output.println("SALARY CALCULATOR | Skema latihan");
        output.println("Tarif tetap: HRA 50%, tunjangan khusus 75%, PF 12%.");
        while (true) {
            output.println("Masukkan gaji pokok (maksimal dua desimal):");
            if (!input.hasNextLine()) {
                return;
            }
            String value = input.nextLine().strip();
            if (!SALARY.matcher(value).matches()) {
                output.println("Masukan tidak valid. Gunakan 0 sampai 999999999999999999.99 dengan titik desimal.");
                continue;
            }
            SalaryBreakdown result = calculate(new BigDecimal(value));
            output.println("Gaji pokok: " + format(result.basicSalary()));
            output.println("HRA (50%): " + format(result.hra()));
            output.println("Tunjangan khusus (75%): " + format(result.specialAllowance()));
            output.println("PF (12%): " + format(result.pf()));
            output.println("Gaji bersih latihan: " + format(result.netPayableSalary()));
            output.println("Nilai dihitung tepat; tidak ada pembulatan komponen.");
            return;
        }
    }

    private static String format(BigDecimal value) {
        BigDecimal compact = value.stripTrailingZeros();
        return (compact.scale() < 2 ? compact.setScale(2) : compact).toPlainString();
    }
}
