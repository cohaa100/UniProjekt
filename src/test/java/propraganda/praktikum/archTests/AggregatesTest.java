package propraganda.praktikum.archTests;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.stereotype.Repository;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatEntity;
import propraganda.praktikum.logic.aggregate.sterotypes.AggregatRoot;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

@AnalyzeClasses(packages = "propraganda.praktikum.logic.aggregate", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class AggregatesTest {

//    @ArchTest
//    static final ArchRule onlyAggregateRootsShouldBePublic = classes()
//            .that()
//            .areNotAnnotatedWith(AggregatRoot.class)
//            .and().areNotAnnotatedWith(Repository.class)
//            .and().areNotInterfaces()
//            .and().areNotEnums()
//            .should()
//            .notBePublic();
//
//    @ArchTest
//    static final ArchRule aggregateEntitysShouldBePackagePrivate = classes()
//            .that()
//            .areAnnotatedWith(AggregatEntity.class)
//            .and().areNotInterfaces()
//            .and().areNotEnums()
//            .should()
//            .bePackagePrivate();


}
