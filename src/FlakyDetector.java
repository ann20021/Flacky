import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FlakyDetector {

    public Set<String> failed(List<String> run) {
        return filterByStatus(run, "FAILED");
    }

    public Set<String> passed(List<String> run) {
        return filterByStatus(run, "PASSED");
    }

    //чтобы не писать дважды (отдельно для упавших и отдельно для прошедших)
    private Set<String> filterByStatus(List<String> run, String targetStatus) {
        if (run == null) {
            return Set.of(); //если вместо списка передали пустоту, вернем пустое множество
        }
        return run.stream()
                .filter(line -> line != null && !line.isBlank())
                .map(line -> line.split("\\|", 2))
                .filter(parts -> parts.length == 2 && parts[1].trim().equals(targetStatus))
                .map(parts -> parts[0].trim())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }


    public List<String> flaky(List<String> run1, List<String> run2) {
        Set<String> candidates = new LinkedHashSet<>(failed(run1));
        Set<String> passedInSecond = passed(run2);

        // Оставляем в candidates только те имена, которые также есть в passedInSecond (пересечение множеств).
        // CommentTest не войдет, т.к. во втором прогоне он в FAILED, а не в PASSED.
        candidates.retainAll(passedInSecond);

        return new ArrayList<>(candidates);
    }

    public Map<String, Long> statusSummary(List<String> run) {
        if (run == null) {
            return Map.of();
        }
        return run.stream()
                .filter(line -> line != null && !line.isBlank())
                .map(line -> line.split("\\|", 2))
                .filter(parts -> parts.length == 2)
                .map(parts -> parts[1].trim())
                .collect(Collectors.groupingBy(
                        status -> status,
                        LinkedHashMap::new,
                        Collectors.counting()
                ));
    }

    public static void main(String[] args) {
        List<String> run1 = List.of(
                "LoginTest|PASSED",
                "CreateTaskTest|FAILED",
                "CommentTest|FAILED",
                "LogoutTest|PASSED"
        );

        List<String> run2 = List.of(
                "LoginTest|PASSED",
                "CreateTaskTest|PASSED",
                "CommentTest|FAILED",
                "StatsTest|PASSED"
        );

        FlakyDetector detector = new FlakyDetector();

        List<String> flakyCandidates = detector.flaky(run1, run2);
        Map<String, Long> run2Summary = detector.statusSummary(run2);

        System.out.println("Flaky-кандидаты: " + flakyCandidates);
        System.out.println("Прогон 2, сводка: " + run2Summary);
    }
}