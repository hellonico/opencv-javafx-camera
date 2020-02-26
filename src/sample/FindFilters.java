package sample;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;

import java.util.List;

public class FindFilters {
    public static List<String> findFilters() {
        try (
//                ScanResult scanResult = new ClassGraph().enableAllInfo().whitelistPackages("origami")
//                .scan())
                ScanResult scanResult = new ClassGraph().enableAllInfo().scan())
        {
            ClassInfoList widgetClasses = scanResult.getClassesImplementing("origami.Filter");
            return widgetClasses.getNames();
        }
    }
    public static void main(String... args) {
        for(String s : findFilters())  System.out.println(s);
    }
}
