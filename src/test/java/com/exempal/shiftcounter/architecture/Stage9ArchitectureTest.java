package com.exempal.shiftcounter.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

class Stage9ArchitectureTest {
    private final JavaClasses production = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.exempal.shiftcounter.features");

    @Test
    void domainIsIndependentFromApplicationAdaptersAndFrameworks() {
        noClasses().that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..", "..adapter..", "jakarta.persistence..",
                        "org.springframework..")
                .check(production);
    }

    @Test
    void applicationDoesNotDependOnAdaptersOrPersistenceFrameworks() {
        noClasses().that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..adapter..", "jakarta.persistence..", "org.springframework.data..",
                        "org.springframework.web..")
                .check(production);
    }

    @Test
    void webAdaptersUseApplicationBoundariesInsteadOfRepositoriesAndEntities() {
        classes().that().areAnnotatedWith(Controller.class)
                .or().areAnnotatedWith(RestController.class)
                .should().onlyDependOnClassesThat().resideOutsideOfPackages(
                        "..adapter.persistence..", "..adapter.jpa..")
                .check(production);
    }

    @Test
    void featureDependenciesHaveNoCycles() {
        slices().matching("com.exempal.shiftcounter.features.(*)..")
                .should().beFreeOfCycles()
                .check(production);
    }

    @Test
    void obsoletePackageShapesStayRemoved() {
        noClasses().should().resideInAnyPackage("..infrastructure..", "..api..")
                .check(production);
    }
}
