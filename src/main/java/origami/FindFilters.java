package origami;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.List;
import java.util.stream.Collectors;

public class FindFilters {
    public static List<String> findFilters() {
        try (
//                ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages("origami")
//                .scan())
                ScanResult scanResult = new ClassGraph().enableAllInfo().scan())
        {
            ClassInfoList widgetClasses = scanResult.getClassesImplementing("origami.Filter");
            return widgetClasses.getNames().stream().filter(k -> {
                try {
                    return Class.forName(k).getConstructor()!=null;
                } catch (Exception e) {
                    return false;
                }
            }).collect(Collectors.toList());
        }
    }
    public static void main(String... args) {
        for(String s : findFilters())  System.out.println(s);
    }
}
