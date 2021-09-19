package propraganda.praktikum.archTests;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import propraganda.praktikum.PraktikumApplication;
import propraganda.praktikum.logic.util.RoleInjector;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@AnalyzeClasses(packages = "propraganda.praktikum", importOptions = {ImportOption.DoNotIncludeTests.class, ImportOption.DoNotIncludeJars.class})
public class ArchitekturTest {

    @ArchTest
    static final ArchRule schichtenArchitekturEinhaltung =
            layeredArchitecture()
                    .layer("Controller").definedBy("..controller..")
                    .layer("Logic").definedBy("..logic..")
                    .layer("Persistence").definedBy("..persistence..")

                    .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Logic").mayOnlyBeAccessedByLayers("Controller", "Persistence")
                    .whereLayer("Persistence").mayOnlyBeAccessedByLayers("Logic")
                    .ignoreDependency(PraktikumApplication.class,RoleInjector.class);
}
